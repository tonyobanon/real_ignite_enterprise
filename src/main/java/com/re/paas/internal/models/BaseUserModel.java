package com.re.paas.internal.models;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;
import com.re.paas.internal.base.api.event_streams.Article;
import com.re.paas.internal.base.api.event_streams.CustomPredicate;
import com.re.paas.internal.base.api.event_streams.ObjectEntity;
import com.re.paas.internal.base.api.event_streams.ObjectType;
import com.re.paas.internal.base.api.event_streams.Preposition;
import com.re.paas.internal.base.api.event_streams.Sentence;
import com.re.paas.internal.base.api.event_streams.SubjectEntity;
import com.re.paas.internal.base.api.event_streams.SubjectType;
import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.ClientResources;
import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.Gender;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.base.core.Note;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.base.core.PlatformInternal;
import com.re.paas.internal.base.core.ResourceException;
import com.re.paas.internal.base.core.Todo;
import com.re.paas.internal.core.fusion.Unexposed;
import com.re.paas.internal.core.keys.ConfigKeys;
import com.re.paas.internal.core.keys.MetricKeys;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.core.users.UserProfileSpec;
import com.re.paas.internal.entites.BaseUserEntity;
import com.re.paas.internal.entites.UserFormValueEntity;
import com.re.paas.internal.errors.UserAccountError;
import com.re.paas.internal.models.helpers.Dates;
import com.re.paas.internal.models.helpers.EntityHelper;

@Todo("stop storing passwords as plain text, hash it instead")
@Model(dependencies = RoleModel.class)
public class BaseUserModel implements BaseModel {

	@Override
	public String path() {
		return "core/base-user";
	}

	@Override
	public void preInstall() {

		ConfigModel.put(ConfigKeys.USER_COUNT_ARCHIVE, 0);
		ConfigModel.put(ConfigKeys.USER_COUNT_CURRENT, 0);
	}

	@Override
	public void install(InstallOptions options) {

		for (UserProfileSpec spec : options.getAdmins()) {
			registerUser(spec, RoleModel.getDefaultRole(RoleRealm.ADMIN), -1l);
		}
	}

	private static Long nextKey() {
		Long current = Long.parseLong(ConfigModel.get(ConfigKeys.USER_COUNT_ARCHIVE));
		Long next = current + 1;
		ConfigModel.put(ConfigKeys.USER_COUNT_ARCHIVE, next);
		return next;
	}

	protected static void deleteUser(Long id) {

		// Delete form values
		deleteFieldValuesForUser(id);

		// Delete entity
		ofy().delete().key(Key.create(BaseUserEntity.class, id)).now();

		ConfigModel.put(ConfigKeys.USER_COUNT_CURRENT,
				Integer.parseInt(ConfigModel.get(ConfigKeys.USER_COUNT_CURRENT)) - 1);

		// Update cached list index
		SearchModel.removeCachedListKey(IndexedNameType.USER, id);
	}

	public static Long getUserId(String email) {
		BaseUserEntity e = ofy().load().type(BaseUserEntity.class).filter("email = ", email).first().now();
		if (e != null) {
			return e.getId();
		} else {
			throw new NullPointerException();
		}
	}

	@ModelMethod(functionality = Functionality.EMAIL_LOGIN_USER)
	public static Long loginByEmail(String email, String password) {

		if (!doesEmailExist(email)) {
			// Incorrect email
			throw new PlatformException(UserAccountError.EMAIL_DOES_NOT_EXIST);
		}

		BaseUserEntity e = ofy().load().type(BaseUserEntity.class).filter("email = ", email).first().now();
		if (e.getPassword().equals(password)) {
			return e.getId();
		} else {
			// Wrong password
			throw new PlatformException(UserAccountError.INCORRECT_PASSWORD);
		}
	}

	@Note("The phone index has been commented out in the entity")
	@BlockerTodo("Phone Index is currently not used on the frontend. Indexes are expensive remember")
	@ModelMethod(functionality = Functionality.PHONE_LOGIN_USER)
	public static Long loginByPhone(Long phone, String password) {

		if (!doesPhoneExist(phone)) {
			// Incorrect phone
			throw new PlatformException(UserAccountError.PHONE_DOES_NOT_EXIST);
		}

		BaseUserEntity e = ofy().load().type(BaseUserEntity.class).filter("phone = ", phone.toString()).first().now();
		if (e.getPassword().equals(password)) {
			return e.getId();
		} else {
			// Wrong password
			throw new PlatformException(UserAccountError.INCORRECT_PASSWORD);
		}
	}

	@Todo("Validate user's phone number, and other info properly, make method protected")
	public static Long registerUser(UserProfileSpec spec, String role, Long principal) {

		BaseUserEntity e = EntityHelper.fromObjectModel(role, principal, spec);

		if (doesEmailExist(e.getEmail())) {
			throw new PlatformException(UserAccountError.EMAIL_ALREADY_EXISTS);
		}

		if (doesPhoneExist(e.getPhone())) {
			throw new PlatformException(UserAccountError.PHONE_ALREADY_EXISTS);
		}

		e.setId(nextKey());

		ofy().save().entity(e).now();

		ConfigModel.put(ConfigKeys.USER_COUNT_CURRENT,
				Integer.parseInt(ConfigModel.get(ConfigKeys.USER_COUNT_CURRENT)) + 1);
		MetricsModel.increment(MetricKeys.USERS_COUNT);

		// Update cached list index
		SearchModel.addCachedListKey(IndexedNameType.USER, e.getId());

		return e.getId();
	}

	private static boolean doesEmailExist(String email) {
		return ofy().load().type(BaseUserEntity.class).filter("email = ", email).first().now() != null;
	}

	private static boolean doesPhoneExist(Long phone) {
		return ofy().load().type(BaseUserEntity.class).filter("phone = ", phone.toString()).first().now() != null;
	}

	@PlatformInternal
	public static BaseUserEntity get(Long userId) {
		return ofy().load().type(BaseUserEntity.class).id(userId).safe();
	}

	@ModelMethod(functionality = { Functionality.VIEW_OWN_PROFILE, Functionality.MANAGE_USER_ACCOUNTS })
	public static String getAvatar(Long userId) {
		return userId.equals(-1l) ? null : get(userId).getImage();
	}

	protected static Boolean isMaleUser(Long userId) {
		return Gender.from(get(userId).getGender()).equals(Gender.MALE);
	}

	@ModelMethod(functionality = Functionality.GET_USER_PROFILE)
	public static boolean canAccessUserProfile(Long principal, Long userId) {

		RoleRealm principalRealm = RoleModel.getRealm(getRole(principal));
		RoleRealm userRealm = RoleModel.getRealm(getRole(userId));

		return principalRealm.getAuthority() >= userRealm.getAuthority();
	}

	@ModelMethod(functionality = Functionality.GET_USER_PROFILE)
	public static UserProfileSpec getProfile(Long principal, Long userId) {

		if (!canAccessUserProfile(principal, userId)) {
			throw new ResourceException(ResourceException.ACCESS_NOT_ALLOWED);
		}

		return EntityHelper.toObjectModel(get(userId));
	}

	@ModelMethod(functionality = Functionality.VIEW_OWN_PROFILE)
	public static UserProfileSpec getProfile(Long userId) {
		return EntityHelper.toObjectModel(get(userId));
	}

	@ModelMethod(functionality = { Functionality.VIEW_OWN_PROFILE, Functionality.MANAGE_USER_ACCOUNTS })
	public static String getRole(Long userId) {
		return get(userId).getRole();
	}

	public static String getPreferredLocale(Long userId) {
		return get(userId).getPreferredLocale();
	}

	@ModelMethod(functionality = { Functionality.MANAGE_OWN_PROFILE, Functionality.MANAGE_USER_ACCOUNTS })
	public static void updateEmail(Long principal, Long userId, String email) {

		if (doesEmailExist(email)) {
			throw new PlatformException(UserAccountError.EMAIL_ALREADY_EXISTS);
		}

		BaseUserEntity e = get(userId).setEmail(email).setDateUpdated(Dates.now());
		;
		ofy().save().entity(e).now();

		// Add to activity stream

		Sentence activity = Sentence.newInstance()
				.setSubject(SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(userId)))
				.setPredicate(CustomPredicate.UPDATED)
				.setObject(
						ObjectEntity.get(ObjectType.EMAIL).setArticle(isMaleUser(userId) ? Article.HIS : Article.HER))
				.withPreposition(Preposition.TO, ClientRBRef.get(email));

		if (principal != null) {
			activity.withPreposition(Preposition.THROUGH,
					SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(principal)));
		}

		ActivityStreamModel.newActivity(activity);
	}

	@ModelMethod(functionality = { Functionality.MANAGE_OWN_PROFILE, Functionality.MANAGE_USER_ACCOUNTS })
	public static void updatePhone(Long principal, Long userId, Long phone) {

		if (doesPhoneExist(phone)) {
			throw new PlatformException(UserAccountError.PHONE_ALREADY_EXISTS);
		}

		BaseUserEntity e = get(userId).setPhone(phone).setDateUpdated(Dates.now());
		;
		ofy().save().entity(e).now();

		// Add to activity stream

		Sentence activity = Sentence.newInstance()
				.setSubject(SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(userId)))
				.setPredicate(CustomPredicate.UPDATED)
				.setObject(ObjectEntity.get(ObjectType.PHONE_NUMBER)
						.setArticle(isMaleUser(userId) ? Article.HIS : Article.HER))
				.withPreposition(Preposition.TO, ClientRBRef.get(phone));

		if (principal != null) {
			activity.withPreposition(Preposition.THROUGH,
					SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(principal)));
		}

		ActivityStreamModel.newActivity(activity);
	}

	@ModelMethod(functionality = { Functionality.MANAGE_OWN_PROFILE, Functionality.MANAGE_USER_ACCOUNTS })
	public static void updatePassword(Long principal, Long userId, String currentPassword, String newPassword) {
		BaseUserEntity e = get(userId);

		if (e.getPassword().equals(currentPassword)) {
			e.setPassword(newPassword).setDateUpdated(Dates.now());
		} else {
			throw new PlatformException(UserAccountError.PASSWORDS_MISMATCH);
		}

		ofy().save().entity(e).now();

		// Add to activity stream

		Sentence activity = Sentence.newInstance()
				.setSubject(SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(userId)))
				.setPredicate(CustomPredicate.UPDATED).setObject(ObjectEntity.get(ObjectType.PASSWORD)
						.setArticle(isMaleUser(userId) ? Article.HIS : Article.HER));

		if (principal != null) {
			activity.withPreposition(Preposition.THROUGH,
					SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(principal)));
		}

		ActivityStreamModel.newActivity(activity);
	}

	@ModelMethod(functionality = { Functionality.MANAGE_OWN_PROFILE, Functionality.MANAGE_USER_ACCOUNTS })
	public static void updateAvatar(Long principal, Long userId, String blobId) {
		BaseUserEntity e = get(userId).setImage(blobId).setDateUpdated(Dates.now());
		ofy().save().entity(e).now();

		// Add to activity stream

		Sentence activity = Sentence.newInstance()
				.setSubject(SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(userId)))
				.setPredicate(CustomPredicate.UPDATED).setObject(
						ObjectEntity.get(ObjectType.IMAGE).setArticle(isMaleUser(userId) ? Article.HIS : Article.HER));

		if (principal != null) {
			activity.withPreposition(Preposition.THROUGH,
					SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(principal)));
		}

		ActivityStreamModel.newActivity(activity);
	}

	@BlockerTodo("Based on user role, consolidate all other entities that belong to this user")
	@ModelMethod(functionality = Functionality.MANAGE_USER_ACCOUNTS)
	public static void updateRole(Long principal, Long userId, String role) {

		BaseUserEntity e = get(userId).setRole(role).setDateUpdated(Dates.now());
		ofy().save().entity(e).now();

		// Add to activity stream

		Sentence activity = Sentence.newInstance()
				.setSubject(SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(principal)))
				.setPredicate(CustomPredicate.UPDATED).setObject(ObjectEntity.get(ObjectType.USER_ROLE))
				.withPreposition(Preposition.OF,
						SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(userId)))
				.withPreposition(Preposition.TO,
						ObjectEntity.get(ObjectType.SYSTEM_ROLE).setIdentifiers(FluentArrayList.asList(role)));

		ActivityStreamModel.newActivity(activity);
	}

	protected static void deleteFieldValues(String fieldId) {

		List<Key<UserFormValueEntity>> keys = new FluentArrayList<>();

		ofy().load().type(UserFormValueEntity.class).filter("fieldId = ", fieldId).forEach(e -> {
			keys.add(Key.create(UserFormValueEntity.class, e.getId()));
		});

		ofy().delete().keys(keys).now();
	}

	private static void deleteFieldValuesForUser(Long userId) {

		List<Key<UserFormValueEntity>> keys = new FluentArrayList<>();

		ofy().load().type(UserFormValueEntity.class).filter("userId = ", userId).forEach(e -> {
			keys.add(Key.create(UserFormValueEntity.class, e.getId()));
		});

		ofy().delete().keys(keys).now();
	}

	@Unexposed
	@ModelMethod(functionality = Functionality.MANAGE_USER_ACCOUNTS)
	public static Map<Long, String> getFieldValues(Long userId, Collection<Long> fieldIds) {

		Map<Long, String> result = new FluentHashMap<>();

		fieldIds.forEach(fieldId -> {

			UserFormValueEntity e = ofy().load().type(UserFormValueEntity.class).filter("fieldId = ", fieldId)
					.filter("userId = ", userId).first().now();

			result.put(fieldId, e != null ? e.getValue() : null);
		});
		return result;
	}

	@ModelMethod(functionality = Functionality.GET_PERSON_NAMES)
	public static Map<Long, String> getPersonNames(List<Long> ids) {
		Map<Long, String> names = new FluentHashMap<>();
		ofy().load().type(BaseUserEntity.class).ids(ids).forEach((k, v) -> {
			names.put(k, v.getFirstName() + " " + v.getMiddleName() + " " + v.getLastName());
		});
		return names;
	}

	@Unexposed
	@ModelMethod(functionality = Functionality.GET_PERSON_NAMES)
	public static Object getPersonName(Long id, boolean full) {

		if (id.equals(-1l)) {
			return ClientRBRef.get("guest");
		}
		BaseUserEntity v = ofy().load().type(BaseUserEntity.class).id(id).safe();
		return (full
				? v.getFirstName() + ClientResources.HtmlCharacterEntities.SPACE + v.getMiddleName()
						+ ClientResources.HtmlCharacterEntities.SPACE + v.getLastName()
				: v.getFirstName() + ClientResources.HtmlCharacterEntities.SPACE + v.getLastName());
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unInstall() {
		// TODO Auto-generated method stub
		
	}

}
