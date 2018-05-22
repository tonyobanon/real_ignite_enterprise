package com.re.paas.internal.models;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.objectify.Key;
import com.re.paas.internal.base.AppDelegate;
import com.re.paas.internal.base.api.event_streams.Article;
import com.re.paas.internal.base.api.event_streams.CustomPredicate;
import com.re.paas.internal.base.api.event_streams.ObjectEntity;
import com.re.paas.internal.base.api.event_streams.ObjectType;
import com.re.paas.internal.base.api.event_streams.Sentence;
import com.re.paas.internal.base.api.event_streams.SubjectEntity;
import com.re.paas.internal.base.api.event_streams.SubjectType;
import com.re.paas.internal.base.api.event_streams.SubordinatingConjuction;
import com.re.paas.internal.base.classes.ApplicationDeclineReason;
import com.re.paas.internal.base.classes.ApplicationStatus;
import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.FormSectionType;
import com.re.paas.internal.base.classes.Gender;
import com.re.paas.internal.base.classes.IndexedNameSpec;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.classes.spec.AgentOrganizationSpec;
import com.re.paas.internal.base.classes.spec.AgentSpec;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.base.core.ResourceException;
import com.re.paas.internal.base.core.Todo;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.core.keys.ConfigKeys;
import com.re.paas.internal.core.pdf.PDFForm;
import com.re.paas.internal.core.pdf.SizeSpec;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.core.users.UserProfileSpec;
import com.re.paas.internal.entites.ApplicationEntity;
import com.re.paas.internal.entites.ApplicationFormValueEntity;
import com.re.paas.internal.entites.DeclinedApplicationEntity;
import com.re.paas.internal.errors.RexError;
import com.re.paas.internal.models.helpers.Dates;
import com.re.paas.internal.models.helpers.FormFactory;
import com.re.paas.internal.models.helpers.FormFieldRepository;
import com.re.paas.internal.models.helpers.FormFieldRepository.FieldType;
import com.re.paas.internal.utils.FrontendObjectMarshaller;
import com.re.paas.internal.utils.Utils;

@Model(dependencies = { ConfigModel.class, FormModel.class })
public class ApplicationModel implements BaseModel {

	@Override
	public String path() {
		return "core/application";
	}

	@Override
	public void preInstall() {

		Logger.get().debug("Generating application questionnaires for all role realms");

		boolean b = false;

		// I need to write a new FontProvider to use the GAE Memcache Service to cache
		// fonts
		if (b) {

			for (RoleRealm realm : RoleRealm.values()) {

				String blobId = generatePDFQuestionnaire(realm);
				ConfigModel.put(ConfigKeys.$REALM_APPLICATION_FORM_BLOB_ID.replace("$REALM", realm.name()), blobId);

				Logger.get().debug("Generated Questionnaire for " + realm.toString() + " with blob-id: " + blobId);
			}
		}
	}

	@Override
	public void install(InstallOptions options) {

	}

	private static IndexedNameType getIndexedNameType(String role) {
		RoleRealm realm = RoleModel.getRealm(role);

		switch (realm) {
		case ADMIN:
			return IndexedNameType.ADMIN_APPLICATION;
		case AGENT:
			return IndexedNameType.AGENT_APPLICATION;
		case ORGANIZATION_ADMIN:
			return IndexedNameType.AGENT_ORGANIZATION_APPLICATION;
		}

		return null;
	}

	@Todo("Add to activity stream")
	@ModelMethod(functionality = Functionality.CREATE_APPLICATION)
	public static Long newApplication(String role) {

		// Create application with status of CREATED

		Long applicationId = ofy().save().entity(new ApplicationEntity().setRole(role)
				.setStatus(ApplicationStatus.CREATED.getValue()).setDateCreated(Dates.now())).now().getId();

		// Update cached list index

		SearchModel.addCachedListKey(getIndexedNameType(role),
				FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.CREATED.getValue())),
				applicationId);

		return applicationId;
	}

	@ModelMethod(functionality = Functionality.UPDATE_APPLICATION)
	public static void updateApplication(Long applicationId, Map<String, String> values) {

		ApplicationStatus status = getApplicationStatus(applicationId);

		String role = getApplicationRole(applicationId);
		RoleRealm realm = RoleModel.getRealm(role);

		if (!(status.equals(ApplicationStatus.CREATED) || status.equals(ApplicationStatus.OPEN))) {
			throw new ResourceException(ResourceException.UPDATE_NOT_ALLOWED,
					"Application cannot be updated. It may have already been submitted");
		}

		ApplicationFormValueEntity e = new ApplicationFormValueEntity()
				.setApplicationId(applicationId)
				.setFieldId(FormFieldRepository.getFieldId(realm, FieldType.PREFERRED_LOCALE))
				.setValue(LocaleModel.getUserLocale())
				.setDateUpdated(Dates.now());

		ofy().save().entity(e);
		
		// validateApplicationValues(applicationId, values);

		// Delete old values
		deleteFieldValuesForApplication(applicationId);

		// Save values
		List<ApplicationFormValueEntity> entries = new FluentArrayList<>();
		values.forEach((k, v) -> {
			entries.add(new ApplicationFormValueEntity().setApplicationId(applicationId).setFieldId(k)
					.setValue(v.toString()).setDateUpdated(new Date()));
		});

		ofy().save().entities(entries).now();

		// Update Application ref, if necessary
		String refField = FormFieldRepository.getRefField(realm);
		String refValue = null;

		String currentRefValue = getApplicationRef(applicationId);

		if (refField != null) {
			refValue = values.get(refField);
			updateApplicationRef(applicationId, refValue);
		}

		if (status.equals(ApplicationStatus.CREATED)) {

			// Update status
			updateApplicationStatus(applicationId, ApplicationStatus.OPEN);

			// Update cached list index

			SearchModel.removeCachedListKey(getIndexedNameType(role),
					FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.CREATED.getValue())),
					applicationId);

			SearchModel.addCachedListKey(getIndexedNameType(role),
					FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.OPEN.getValue())),
					applicationId);

			if (refValue != null) {
				SearchModel.addCachedListKey(getIndexedNameType(role),
						new FluentArrayList<ListingFilter>()
								.with(new ListingFilter("status", ApplicationStatus.OPEN.getValue()))
								.with(new ListingFilter("ref", refValue)),
						applicationId);
			}

		} else if (status.equals(ApplicationStatus.OPEN)) {

			// Update CachedList index, if the ref field was updated

			if (refValue != null) {

				if (currentRefValue != null) {
					SearchModel.removeCachedListKey(getIndexedNameType(role),
							new FluentArrayList<ListingFilter>()
									.with(new ListingFilter("status", ApplicationStatus.OPEN.getValue()))
									.with(new ListingFilter("ref", currentRefValue)),
							applicationId);
				}

				SearchModel.addCachedListKey(getIndexedNameType(role),
						new FluentArrayList<ListingFilter>()
								.with(new ListingFilter("status", ApplicationStatus.OPEN.getValue()))
								.with(new ListingFilter("ref", refValue)),
						applicationId);
			}
		}

		// Add to activity stream

		Sentence activity = Sentence.newInstance().setSubject(getNameSpec(applicationId, realm))
				.setPredicate(CustomPredicate.UPDATED).setObject(
						ObjectEntity.get(ObjectType.APPLICATION).setIdentifiers(FluentArrayList.asList(applicationId))
								.setArticle(isMaleApplicant(applicationId, realm) ? Article.HIS : Article.HER));

		ActivityStreamModel.newActivity(activity);
	}

	@ModelMethod(functionality = Functionality.SUBMIT_APPLICATION)
	@BlockerTodo("Fix validateApplicationValues(..)")
	public static Long submitApplication(Long applicationId) {

		if (!getApplicationStatus(applicationId).equals(ApplicationStatus.OPEN)) {
			throw new ResourceException(ResourceException.UPDATE_NOT_ALLOWED);
		}

		String role = getApplicationRole(applicationId);
		RoleRealm realm = RoleModel.getRealm(role);

		Map<String, String> values = getFieldValues(applicationId);

		// validateApplicationValues(applicationId, values);

		// Update status
		updateApplicationStatus(applicationId, ApplicationStatus.PENDING);

		
		String refValue = getApplicationRef(applicationId);

		// Update cached list index

		SearchModel.removeCachedListKey(getIndexedNameType(role),
				FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.OPEN.getValue())), applicationId);

		if (refValue != null) {
			SearchModel.removeCachedListKey(getIndexedNameType(role),
					new FluentArrayList<ListingFilter>()
							.with(new ListingFilter("status", ApplicationStatus.OPEN.getValue()))
							.with(new ListingFilter("ref", refValue)),
					applicationId);
		}

		SearchModel.addCachedListKey(getIndexedNameType(role),
				FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.PENDING.getValue())),
				applicationId);

		if (refValue != null) {
			SearchModel.addCachedListKey(getIndexedNameType(role),
					new FluentArrayList<ListingFilter>()
							.with(new ListingFilter("status", ApplicationStatus.PENDING.getValue()))
							.with(new ListingFilter("ref", refValue)),
					applicationId);
		}

		// Add to search index

		IndexedNameSpec nameSpec = getNameSpec(applicationId, realm, values);
		SearchModel.addIndexedName(nameSpec, getIndexedNameType(role));

		// Add to activity stream

		Sentence activity = Sentence.newInstance().setSubject(getNameSpec(applicationId, realm))
				.setPredicate(CustomPredicate.SUBMITTED).setObject(
						ObjectEntity.get(ObjectType.APPLICATION).setIdentifiers(FluentArrayList.asList(applicationId))
								.setArticle(isMaleApplicant(applicationId, realm) ? Article.HIS : Article.HER));

		ActivityStreamModel.newActivity(activity);

		return applicationId;
	}

	private static void validateApplicationValues(Long applicationId, Map<String, String> values) {

		RoleRealm realm = RoleModel.getRealm(getApplicationRole(applicationId));

		// Scan for relevant fields, verify that proper values are provided for them

		for (Entry<String, Boolean> e : FormModel.listAllFieldKeys(FormSectionType.APPLICATION_FORM, realm)
				.entrySet()) {

			if (!e.getValue()) {
				continue;
			}

			String value = values.get(e.getKey());
			if (value == null || value.trim().equals("")) {
				throw new ResourceException(ResourceException.FAILED_VALIDATION, e.getKey());
			}
		}
		;
	}

	@ModelMethod(functionality = Functionality.DOWNLOAD_QUESTIONNAIRE)
	public static String getPDFQuestionnaire(String role) {

		RoleRealm realm = RoleModel.getRealm(role);

		String blobId = (String) ConfigModel
				.get(ConfigKeys.$REALM_APPLICATION_FORM_BLOB_ID.replace("$REALM", realm.toString()));

		if (blobId == null) {
			return generatePDFQuestionnaire(realm);
		}

		return blobId;
	}

	private static String generatePDFQuestionnaire(RoleRealm realm) {

		String orgenizationName = ConfigModel.get(ConfigKeys.ORGANIZATION_NAME);

		PDFForm form = new PDFForm().setLogoURL(ConfigModel.get(ConfigKeys.ORGANIZATION_LOGO_URL))
				.setSubtitleLeft(orgenizationName)
				.setTitle(Utils.prettify(realm.name().toLowerCase() + "  e-Registration"))
				.setSubtitleRight(AppDelegate.SOFTRWARE_VENDER_EMAIL);

		FormModel.listSections(FormSectionType.APPLICATION_FORM, realm).forEach((section) -> {

			section.withEntries(FormModel.getFields(FormSectionType.APPLICATION_FORM, section.getId()));

			form.withSection(section);
		});

		//Generate using default locale
		
		File tmp = FormFactory.toPDF(LocaleModel.defaultLocale(), new SizeSpec(4), new SizeSpec(5), new SizeSpec(3), form);

		Logger.get().debug("Saving questionairre form for " + realm.name() + " to " + tmp.toString());

		try {

			String blobId = BlobStoreModel.save(new FileInputStream(tmp));

			ConfigModel.put(ConfigKeys.$REALM_APPLICATION_FORM_BLOB_ID.replace("$REALM", realm.toString()), blobId);

			return blobId;

		} catch (IOException e) {
			Exceptions.throwRuntime(e);
			return null;
		}
	}

	public static Map<String, String> getFieldValues(Long applicationId, Collection<String> fieldIds) {

		Map<String, String> result = new FluentHashMap<>();

		fieldIds.forEach(fieldId -> {

			ApplicationFormValueEntity e = ofy().load().type(ApplicationFormValueEntity.class)
					.filter("fieldId = ", fieldId).filter("applicationId = ", applicationId.toString()).first().now();

			result.put(fieldId, e != null ? e.getValue() : null);
		});
		return result;
	}

	@ModelMethod(functionality = Functionality.VIEW_APPLICATION_FORM)
	public static String getApplicationRole(Long id) {
		ApplicationEntity e = ofy().load().type(ApplicationEntity.class).id(id).safe();
		return e.getRole();
	}

	private static String getApplicationRef(Long id) {
		ApplicationEntity e = ofy().load().type(ApplicationEntity.class).id(id).safe();
		return e.getRef();
	}

	private static ApplicationStatus getApplicationStatus(Long id) {
		ApplicationEntity e = ofy().load().type(ApplicationEntity.class).id(id).safe();
		return ApplicationStatus.from(e.getStatus());
	}

	@ModelMethod(functionality = Functionality.UPDATE_APPLICATION)
	public static Map<String, String> getFieldValues(Long applicationId) {

		Map<String, String> result = new FluentHashMap<>();

		RoleRealm realm = RoleModel.getRealm(getApplicationRole(applicationId));

		Collection<String> fieldIds = FormModel.listAllFieldKeys(FormSectionType.APPLICATION_FORM, realm).keySet();

		fieldIds.forEach(fieldId -> {

			ApplicationFormValueEntity e = ofy().load().type(ApplicationFormValueEntity.class)
					.filter("fieldId = ", fieldId).filter("applicationId = ", applicationId.toString()).first().now();

			result.put(fieldId, e != null ? e.getValue() : null);

		});
		return result;
	}

	@ModelMethod(functionality = Functionality.VIEW_AGENT_ORGANIZATION_APPLICATIONS)
	public static Map<String, String> getConsolidatedFieldValues(Long applicationId) {

		Map<String, String> result = new FluentHashMap<>();

		RoleRealm realm = RoleModel.getRealm(getApplicationRole(applicationId));

		Map<FieldType, String> fieldTypes = FormFieldRepository.getFieldIds(realm);
		Map<String, FieldType> invertedFieldTypes = new HashMap<>(fieldTypes.size());

		fieldTypes.forEach((k, v) -> {
			invertedFieldTypes.put(v, k);
		});

		Collection<String> fieldIds = FormModel.listAllFieldKeys(FormSectionType.APPLICATION_FORM, realm).keySet();

		fieldIds.forEach(fieldId -> {

			ApplicationFormValueEntity e = ofy().load().type(ApplicationFormValueEntity.class)
					.filter("fieldId = ", fieldId).filter("applicationId = ", applicationId.toString()).first().now();

			FieldType fieldType = invertedFieldTypes.get(fieldId);

			result.put(fieldId, e != null ? getConsolidatedFieldValue(fieldType, e.getValue()) : null);

		});
		return result;
	}

	private static String getConsolidatedFieldValue(FieldType type, String value) {
		switch (type) {
		case CITY:
		case ORGANIZATION_CITY:
			return LocationModel.getCityName(value);
		case COUNTRY:
		case ORGANIZATION_COUNTRY:
			return LocationModel.getCountryName(value);
		case GENDER:
			return Utils.prettify(Gender.from(Integer.parseInt(value)).name());
		case STATE:
		case ORGANIZATION_STATE:
			return LocationModel.getTerritoryName(value);

		case FACEBOOK_PROFILE:
			return "https://www.facebook.com/" + value;

		case TWITTER_PROFILE:
			return "https://www.twitter.com/" + value;

		case LINKEDIN_PROFILE:
			return "https://www.linkedin.com/" + value;

		case SKYPE_PROFILE:
			return "https://www.skype.com/" + value;
		default:
			return value;
		}
	}

	protected static void deleteFieldValues(String fieldId) {

		List<Key<ApplicationFormValueEntity>> keys = new FluentArrayList<>();

		ofy().load().type(ApplicationFormValueEntity.class).filter("fieldId = ", fieldId).forEach(e -> {
			keys.add(Key.create(ApplicationFormValueEntity.class, e.getId()));
		});

		ofy().delete().keys(keys).now();
	}

	private static void deleteFieldValuesForApplication(Long applicationId) {

		// Delete form values

		List<Key<?>> keys = new FluentArrayList<>();

		ofy().load().type(ApplicationFormValueEntity.class).filter("applicationId = ", applicationId.toString())
				.forEach(e -> {
					keys.add(Key.create(ApplicationFormValueEntity.class, e.getId()));
				});

		ofy().delete().keys(keys).now();
	}

	private static void updateApplicationRef(Long applicationId, String ref) {

		if (ref == null) {
			return;
		}

		ApplicationEntity e = ofy().load().type(ApplicationEntity.class).id(applicationId).safe();

		e.setRef(ref);
		e.setDateUpdated(Dates.now());

		ofy().save().entity(e).now();
	}

	private static void updateApplicationStatus(Long applicationId, ApplicationStatus status) {
		ApplicationEntity e = ofy().load().type(ApplicationEntity.class).id(applicationId).safe();

		e.setStatus(status.getValue());
		e.setDateUpdated(Dates.now());

		ofy().save().entity(e).now();
	}

	@ModelMethod(functionality = { Functionality.REVIEW_AGENT_ORGANIZATION_APPLICATION,
			Functionality.REVIEW_AGENT_APPLICATION, Functionality.REVIEW_ADMIN_APPLICATION })
	public static Long acceptApplication(Long principal, Long applicationId) {

		// Consolidate application
		Long userId = consolidateApplication(principal, applicationId);

		// Update application status
		updateApplicationStatus(applicationId, ApplicationStatus.ACCEPTED);

		// Delete field values for application
		// deleteFieldValuesForApplication(applicationId);

		String role = getApplicationRole(applicationId);

		String refValue = getApplicationRef(applicationId);

		// Update cached list index

		SearchModel.removeCachedListKey(getIndexedNameType(role),
				FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.PENDING.getValue())),
				applicationId);

		if (refValue != null) {
			SearchModel.removeCachedListKey(getIndexedNameType(role),
					new FluentArrayList<ListingFilter>()
							.with(new ListingFilter("status", ApplicationStatus.PENDING.getValue()))
							.with(new ListingFilter("ref", refValue)),
					applicationId);
		}

		SearchModel.addCachedListKey(getIndexedNameType(role),
				FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.ACCEPTED.getValue())),
				applicationId);

		if (refValue != null) {
			SearchModel.addCachedListKey(getIndexedNameType(role),
					new FluentArrayList<ListingFilter>()
							.with(new ListingFilter("status", ApplicationStatus.ACCEPTED.getValue()))
							.with(new ListingFilter("ref", refValue)),
					applicationId);
		}

		// Update type and entity Id of indexed name

		SearchModel.updateIndexedNameType(applicationId, userId, getIndexedNameType(role), IndexedNameType.USER);

		// Add to activity stream

		Sentence activity = Sentence.newInstance()
				.setSubject(SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(principal)))
				.setPredicate(CustomPredicate.APPROVED).setObject(
						ObjectEntity.get(ObjectType.APPLICATION).setIdentifiers(FluentArrayList.asList(applicationId)));

		ActivityStreamModel.newActivity(activity);

		return userId;
	}

	@ModelMethod(functionality = Functionality.REVIEW_AGENT_ORGANIZATION_APPLICATION)
	public static Map<Integer, Object> getApplicationDeclineReasons() {
		Map<Integer, Object> reasons = new HashMap<>();
		for (ApplicationDeclineReason reason : ApplicationDeclineReason.values()) {
			reasons.put(reason.getValue(), ClientRBRef.get(reason));
		}
		return reasons;
	}

	@ModelMethod(functionality = { Functionality.REVIEW_AGENT_ORGANIZATION_APPLICATION,
			Functionality.REVIEW_AGENT_APPLICATION, Functionality.REVIEW_ADMIN_APPLICATION })
	public static void declineApplication(Long applicationId, Long principal, Integer reason) {

		// Update application status
		updateApplicationStatus(applicationId, ApplicationStatus.DECLINED);

		// Create new declined application

		DeclinedApplicationEntity e = new DeclinedApplicationEntity().setApplicationId(applicationId)
				.setStaffId(principal).setReason(reason).setDateCreated(Dates.now());
		ofy().save().entity(e);

		// Delete field values for application
		// deleteFieldValuesForApplication(applicationId);

		String role = getApplicationRole(applicationId);

		String refValue = getApplicationRef(applicationId);

		// Update cached list index

		SearchModel.removeCachedListKey(getIndexedNameType(role),
				FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.PENDING.getValue())),
				applicationId);

		if (refValue != null) {
			SearchModel.removeCachedListKey(getIndexedNameType(role),
					new FluentArrayList<ListingFilter>()
							.with(new ListingFilter("status", ApplicationStatus.PENDING.getValue()))
							.with(new ListingFilter("ref", refValue)),
					applicationId);
		}

		SearchModel.addCachedListKey(getIndexedNameType(role),
				FluentArrayList.asList(new ListingFilter("status", ApplicationStatus.DECLINED.getValue())),
				applicationId);

		if (refValue != null) {
			SearchModel.addCachedListKey(getIndexedNameType(role),
					new FluentArrayList<ListingFilter>()
							.with(new ListingFilter("status", ApplicationStatus.DECLINED.getValue()))
							.with(new ListingFilter("ref", refValue)),
					applicationId);
		}

		// remove indexed name

		SearchModel.removeIndexedName(applicationId.toString(), getIndexedNameType(role));

		// Add to activity stream

		Sentence activity = Sentence.newInstance()
				.setSubject(SubjectEntity.get(SubjectType.USER).setIdentifiers(FluentArrayList.asList(principal)))
				.setPredicate(CustomPredicate.DECLINED)
				.setObject(
						ObjectEntity.get(ObjectType.APPLICATION).setIdentifiers(FluentArrayList.asList(applicationId)))
				.setSubordinativeClause(SubordinatingConjuction.BECAUSE,
						ClientRBRef.get(ApplicationDeclineReason.from(reason)));

		ActivityStreamModel.newActivity(activity);
	}

	@BlockerTodo("Notify user of new account creation")
	private static Long consolidateApplication(Long principal, Long applicationId) {

		String role = getApplicationRole(applicationId);
		RoleRealm realm = RoleModel.getRealm(role);

		Map<FieldType, String> keys = FormFieldRepository.getFieldIds(realm);
		Map<String, String> values = getFieldValues(applicationId);

		UserProfileSpec user = getConsolidatedUser(keys, values);
		Long userId = BaseUserModel.registerUser(user, role, principal);

		switch (realm) {
		case AGENT:
			BaseAgentModel.newAgent(userId, getConsolidatedAgent(keys, values));
			break;
		case ORGANIZATION_ADMIN:
			Long agentOrganization = BaseAgentModel
					.newAgentOrganization(
							getConsolidatedAgentOrganization(keys, values).setAdmin(userId)
					);
			BaseAgentModel.newAgentOrganizationAdmin(userId, agentOrganization);
			break;
		case ADMIN:
			break;
		}

		// Notify user of new account creation
		return userId;
	}

	private static AgentSpec getConsolidatedAgent(Map<FieldType, String> keys, Map<String, String> values) {

		Integer yearsOfExperience = Integer.parseInt(values.get(keys.get(FieldType.YEARS_OF_EXPERIENCE)));

		return new AgentSpec().setYearsOfExperience(yearsOfExperience);
	}

	private static AgentOrganizationSpec getConsolidatedAgentOrganization(Map<FieldType, String> keys,
			Map<String, String> values) {

		return new AgentOrganizationSpec().setName(values.get(keys.get(FieldType.ORGANIZATION_NAME)))
				.setEmail(values.get(keys.get(FieldType.ORGANIZATION_EMAIL)))
				.setPhone(Long.parseLong(values.get(keys.get(FieldType.ORGANIZATION_PHONE))))
				.setLogo(values.get(keys.get(FieldType.ORGANIZATION_LOGO)))
				.setAddress(values.get(keys.get(FieldType.ORGANIZATION_ADDRESS)))
				.setPostalCode(Integer.parseInt(values.get(keys.get(FieldType.ORGANIZATION_POSTAL_CODE))))
				.setCity(Integer.parseInt(values.get(keys.get(FieldType.ORGANIZATION_CITY))))
				.setTerritory(values.get(keys.get(FieldType.ORGANIZATION_STATE)))
				.setCountry(values.get(keys.get(FieldType.ORGANIZATION_COUNTRY)));
	}

	private static UserProfileSpec getConsolidatedUser(Map<FieldType, String> keys, Map<String, String> values) {
		
		return new UserProfileSpec()

				.setFirstName(values.get(keys.get(FieldType.FIRST_NAME)))
				.setLastName(values.get(keys.get(FieldType.LAST_NAME)))
				.setImage(values.get(keys.get(FieldType.IMAGE)))

				.setMiddleName(values.get(keys.get(FieldType.MIDDLE_NAME)))
				.setDateOfBirth(FrontendObjectMarshaller.unmarshalDate(values.get(keys.get(FieldType.DATE_OF_BIRTH))))
				.setGender(Gender.from(Integer.parseInt(values.get(keys.get(FieldType.GENDER)))))
				.setEmail(values.get(keys.get(FieldType.EMAIL)).toString()).setPassword(values.get(keys.get(FieldType.PASSWORD)))

				.setAddress(values.get(keys.get(FieldType.ADDRESS)))
				.setPhone(Long.parseLong(values.get(keys.get(FieldType.PHONE_NUMBER))))
				.setCity(Integer.parseInt(values.get(keys.get(FieldType.CITY))))
				.setTerritory(values.get(keys.get(FieldType.STATE)))
				.setCountry(values.get(keys.get(FieldType.COUNTRY)))
				
				.setFacebookProfile(values.get(keys.get(FieldType.FACEBOOK_PROFILE)))
				.setTwitterProfile(values.get(keys.get(FieldType.TWITTER_PROFILE)))
				.setLinkedInProfile(values.get(keys.get(FieldType.LINKEDIN_PROFILE)))
				.setSkypeProfile(values.get(keys.get(FieldType.SKYPE_PROFILE)))
				
				.setPreferredLocale(values.get(keys.get(FieldType.PREFERRED_LOCALE)));
	}

	//////// Utils (Used by ApplicationService) //////////

	public static void validateAdminReview(Long applicationId, Long principal) {

		String applicantRole = ApplicationModel.getApplicationRole(applicationId);
		RoleRealm applicantRealm = RoleModel.getRealm(applicantRole);

		if (!applicantRealm.equals(RoleRealm.ADMIN)) {
			throw new ResourceException(ResourceException.FAILED_VALIDATION);
		}
	}

	public static void validateAgentOrganizationReview(Long applicationId, Long principal) {

		String applicantRole = ApplicationModel.getApplicationRole(applicationId);
		RoleRealm applicantRealm = RoleModel.getRealm(applicantRole);

		if (!applicantRealm.equals(RoleRealm.ORGANIZATION_ADMIN)) {
			throw new ResourceException(ResourceException.FAILED_VALIDATION);
		}
	}

	public static void validateAgentReview(Long applicationId, Long principal) {

		String applicantRole = ApplicationModel.getApplicationRole(applicationId);
		RoleRealm applicantRealm = RoleModel.getRealm(applicantRole);

		if (!applicantRealm.equals(RoleRealm.AGENT)) {
			throw new ResourceException(ResourceException.FAILED_VALIDATION);
		}

		String principalRole = BaseUserModel.getRole(principal);
		RoleRealm principalRealm = RoleModel.getRealm(principalRole);

		if (!principalRealm.equals(RoleRealm.ADMIN)) {

			// Verify that the agent, and the organization admin belong to the same
			// organization
			Long agentOrganization = BaseAgentModel.getAgentOrganization(principalRealm, principal);

			String agentOrganizationField = FormFieldRepository.getFieldId(applicantRealm,
					FormFieldRepository.FieldType.AGENT_ORGANIZATION);
			Map<String, String> fieldValues = ApplicationModel.getFieldValues(applicationId,
					new FluentArrayList<String>().with(agentOrganizationField));

			Long applicantAgentOrganization = Long.parseLong(fieldValues.get(agentOrganizationField));
			if (!agentOrganization.equals(applicantAgentOrganization)) {
				throw new PlatformException(RexError.AGENT_ORGANIZATION_MISMATCH);
			}
		}
	}

	private static String getApplicantAvatar(Long applicationId, RoleRealm realm) {
		return getApplicantAvatar(applicationId, realm, null);
	}

	private static String getApplicantAvatar(Long applicationId, RoleRealm realm, Map<String, String> fieldValues) {

		String imageField = FormFieldRepository.getFieldId(realm, FormFieldRepository.FieldType.IMAGE);

		if (fieldValues == null) {
			fieldValues = getFieldValues(applicationId, new FluentArrayList<String>().with(imageField));
		}

		return fieldValues.get(imageField);
	}

	private static Boolean isMaleApplicant(Long applicationId, RoleRealm realm) {
		return isMaleApplicant(applicationId, realm, null);
	}

	private static Boolean isMaleApplicant(Long applicationId, RoleRealm realm, Map<String, String> fieldValues) {

		String genderField = FormFieldRepository.getFieldId(realm, FormFieldRepository.FieldType.GENDER);

		if (fieldValues == null) {
			fieldValues = getFieldValues(applicationId, new FluentArrayList<String>().with(genderField));
		}

		Integer value = Integer.parseInt(fieldValues.get(genderField));
		Gender gender = Gender.from(value);

		return gender.equals(Gender.MALE);
	}

	public static IndexedNameSpec getNameSpec(Long applicationId, RoleRealm realm) {
		return getNameSpec(applicationId, realm, null);
	}

	private static IndexedNameSpec getNameSpec(Long applicationId, RoleRealm realm, Map<String, String> fieldValues) {

		IndexedNameSpec spec = new IndexedNameSpec();

		String firstNameField = FormFieldRepository.getFieldId(realm, FormFieldRepository.FieldType.FIRST_NAME);
		String middleNameField = FormFieldRepository.getFieldId(realm, FormFieldRepository.FieldType.MIDDLE_NAME);
		String lastNameField = FormFieldRepository.getFieldId(realm, FormFieldRepository.FieldType.LAST_NAME);

		if (fieldValues == null) {
			fieldValues = getFieldValues(applicationId,
					new FluentArrayList<String>().with(firstNameField).with(middleNameField).with(lastNameField));
		}

		spec.setKey(applicationId.toString()).setX(fieldValues.get(firstNameField)).setY(fieldValues.get(lastNameField))
				.setZ(fieldValues.get(middleNameField));

		return spec;
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
