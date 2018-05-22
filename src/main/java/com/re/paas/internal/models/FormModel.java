package com.re.paas.internal.models;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;
import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.FormSectionType;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.classes.QueryFilter;
import com.re.paas.internal.base.classes.RBEntry;
import com.re.paas.internal.base.core.PlatformInternal;
import com.re.paas.internal.base.core.ResourceException;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.core.forms.CompositeEntry;
import com.re.paas.internal.core.forms.Question;
import com.re.paas.internal.core.forms.SimpleEntry;
import com.re.paas.internal.core.pdf.Section;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.entites.FormCompositeFieldEntity;
import com.re.paas.internal.entites.FormSectionEntity;
import com.re.paas.internal.entites.FormSimpleFieldEntity;
import com.re.paas.internal.models.helpers.EntityHelper;
import com.re.paas.internal.models.helpers.EntityUtils;
import com.re.paas.internal.models.helpers.FormFieldRepository;
import com.re.paas.internal.utils.Utils;

public class FormModel implements BaseModel {

	@Override
	public String path() {
		return "core/form";
	}

	@Override
	public void preInstall() {
		FormFieldRepository.createDefaultFields();
	}

	public static void install() {
	}

	@PlatformInternal
	public static Map<RoleRealm, String> newSection(Object name, FormSectionType type) {

		Map<RoleRealm, String> result = new FluentHashMap<>();
		for (RoleRealm realm : RoleRealm.values()) {
			result.put(realm, newSection(name, null, type, realm));
		}
		return result;
	}

	/**
	 * This creates a new section, for the given realm
	 */
	@ModelMethod(functionality = { Functionality.MANAGE_APPLICATION_FORMS,
			Functionality.MANAGE_SYSTEM_CONFIGURATION_FORM })
	public static String newSection(Object title, Object description, FormSectionType type, RoleRealm realm) {

		FormSectionEntity e = new FormSectionEntity().setId(Utils.newShortRandom()).setType(type.getValue())
				.setRealm(realm != null ? realm.getValue() : null);

		if (title instanceof ClientRBRef) {
			e.setTitle((ClientRBRef) title);
		}

		if (description != null & description instanceof ClientRBRef) {
			e.setDescription((ClientRBRef) description);
		}

		ofy().save().entity(e).now();

		boolean b = false;
		String locale = LocaleModel.getUserLocale();
		
		if (e.getTitle() == null) {

			String titleKey = "form_section_" + e.getId() + "_title";
			RBModel.newEntry(new RBEntry(titleKey, locale, title.toString()));

			e.setTitle(ClientRBRef.get(titleKey));
			b = true;
		}

		if (description != null && e.getDescription() == null) {

			String descriptionKey = "form_section_" + e.getId() + "_description";
			RBModel.newEntry(new RBEntry(descriptionKey, locale, description.toString()));

			e.setDescription(ClientRBRef.get(descriptionKey));
			b = true;
		}

		if (b) {
			ofy().save().entity(e);
		}

		return e.getId();
	}

	private static Integer getSectionType(String sectionId) {
		FormSectionEntity e = ofy().load().type(FormSectionEntity.class).id(sectionId).safe();
		return e.getType();
	}

	/**
	 * This lists all sections for the given realm
	 */
	@ModelMethod(functionality = { Functionality.VIEW_APPLICATION_FORM, Functionality.VIEW_SYSTEM_CONFIGURATION })
	public static List<Section> listSections(FormSectionType type, RoleRealm realm) {

		List<QueryFilter> filters = new FluentArrayList<>();
		filters.add(QueryFilter.get("type = ", type.getValue()));

		if (realm != null) {
			filters.add(QueryFilter.get("realm = ", realm.getValue()));
		}

		QueryFilter[] filtersArray = filters.toArray(new QueryFilter[filters.size()]);

		List<Section> result = new FluentArrayList<>();

		EntityUtils.query(FormSectionEntity.class, filtersArray).forEach(e -> {
			result.add(new Section().setId(e.getId().toString()).setTitle(e.getTitle()).setSummary(e.getDescription()));
		});

		return result;
	}

	@ModelMethod(functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public static void deleteSection(String sectionId, FormSectionType type) {

		FormSectionEntity e = ofy().load().type(FormSectionEntity.class).id(sectionId).safe();

		if (!e.getType().equals(type.getValue())) {
			throw new ResourceException(ResourceException.DELETE_NOT_ALLOWED);
		}

		deleteSection(sectionId);
	}

	/**
	 * This deletes a section
	 */
	protected static void deleteSection(String sectionId) {

		// Delete fields

		listFieldKeys(sectionId).forEach(k -> {

			ofy().delete().key(k);
		});

		// Delete entity

		ofy().delete().key(Key.create(FormSectionEntity.class, sectionId)).now();
	}

	/**
	 * This creates a new simple field
	 */
	protected static String newSimpleField(FormSectionType type, String sectionId, SimpleEntry spec,
			Boolean isDefault) {

		if (!getSectionType(sectionId).equals(type.getValue())) {
			throw new ResourceException(ResourceException.ACCESS_NOT_ALLOWED);
		}

		// Create field

		FormSimpleFieldEntity e = EntityHelper.fromObjectModel(sectionId, isDefault, spec);

		ofy().save().entity(e).now();

		// Ensure key uniqueness in FormCompositeFieldEntity

		if (ofy().load().type(FormCompositeFieldEntity.class).id(e.getId()).now() != null) {

			Logger.get().warn("Duplicate key was created while creating simple form field. Recreating ..");

			ofy().delete().key(Key.create(FormSimpleFieldEntity.class, e.getId())).now();
			return newSimpleField(type, sectionId, spec, isDefault);
		}

		if (e.getTitle() == null) {

			String titleKey = "form_simple_field_" + e.getId() + "_title";
			RBModel.newEntry(new RBEntry(titleKey, LocaleModel.getUserLocale(), spec.getTitle().toString()));

			e.setTitle(ClientRBRef.get(titleKey));

			ofy().save().entity(e);
		}

		return e.getId();
	}

	/**
	 * This creates a new simple field
	 */
	@ModelMethod(functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public static String newSimpleField(FormSectionType type, String sectionId, SimpleEntry spec) {
		return newSimpleField(type, sectionId, spec, false);
	}

	// Created for FormFieldRepository
	public static String newSimpleField(String sectionId, SimpleEntry spec) {
		return newSimpleField(FormSectionType.APPLICATION_FORM, sectionId, spec);
	}

	private static Map<String, ClientRBRef> listSimpleFieldNames(QueryFilter... filters) {

		Map<String, ClientRBRef> entries = new HashMap<>();

		EntityUtils.lazyQuery(FormSimpleFieldEntity.class, filters).forEach(e -> {
			entries.put(e.getId(), e.getTitle());
		});

		return entries;
	}

	private static Map<String, ClientRBRef> listCompositeFieldNames(QueryFilter... filters) {

		Map<String, ClientRBRef> entries = new HashMap<>();

		EntityUtils.lazyQuery(FormCompositeFieldEntity.class, filters).forEach(e -> {
			entries.put(e.getId(), e.getTitle());
		});

		return entries;
	}

	/**
	 * This lists all simple simple fields that matches the specified query filter
	 */
	private static List<SimpleEntry> listSimpleFields(QueryFilter... filters) {

		List<SimpleEntry> entries = new FluentArrayList<>();

		EntityUtils.lazyQuery(FormSimpleFieldEntity.class, filters).forEach(e -> {
			entries.add(EntityHelper.toObjectModel(e));
		});

		return entries;
	}

	/**
	 * This creates a new composite field
	 */
	protected static String newCompositeField(FormSectionType type, String sectionId, CompositeEntry spec,
			Boolean isDefault) {

		if (!getSectionType(sectionId).equals(type.getValue())) {
			throw new ResourceException(ResourceException.ACCESS_NOT_ALLOWED);
		}

		// Create field

		FormCompositeFieldEntity e = EntityHelper.fromObjectModel(sectionId, isDefault, spec);

		ofy().save().entity(e).now();

		// Ensure key uniqueness in FormSimpleFieldEntity

		if (ofy().load().type(FormSimpleFieldEntity.class).id(e.getId()).now() != null) {

			Logger.get().warn("Duplicate key was created while creating composite form field. Recreating ..");

			ofy().delete().type(FormCompositeFieldEntity.class).id(e.getId()).now();

			return newCompositeField(type, sectionId, spec, isDefault);
		}

		boolean b = false;

		List<RBEntry> rbEntries = new ArrayList<RBEntry>();

		if (e.getTitle() == null) {

			String titleKey = "form_composite_field_" + e.getId() + "_title";
			rbEntries.add(new RBEntry(titleKey, LocaleModel.getUserLocale(), spec.getTitle().toString()));

			e.setTitle(ClientRBRef.get(titleKey));
			b = true;
		}

		if (e.getOptions().isEmpty()) {

			Map<ClientRBRef, Object> nOptions = new HashMap<>();

			int i = 1;
			for (Map.Entry<Object, Object> entry : spec.getItems().entrySet()) {
				String titleKey = "form_composite_field_" + e.getId() + "_opt_" + i;
				rbEntries.add(new RBEntry(titleKey, LocaleModel.getUserLocale(), entry.getKey().toString()));
				nOptions.put(ClientRBRef.get(titleKey), entry.getValue());
				i++;
			}

			e.setOptions(nOptions);
			b = true;
		}

		if (b) {
			RBModel.newEntry(rbEntries.toArray(new RBEntry[rbEntries.size()]));
			ofy().save().entity(e);
		}

		return e.getId();
	}

	/**
	 * This creates a new composite field
	 */
	@ModelMethod(functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public static String newCompositeField(FormSectionType type, String sectionId, CompositeEntry spec) {
		return newCompositeField(type, sectionId, spec, false);
	}

	// Created for FormFieldRepository
	public static String newCompositeField(String sectionId, CompositeEntry spec) {
		return newCompositeField(FormSectionType.APPLICATION_FORM, sectionId, spec);
	}

	/**
	 * This lists all composite fields that matches the specified query filter
	 */
	private static List<CompositeEntry> listCompositeFields(QueryFilter... filters) {

		List<CompositeEntry> entries = new FluentArrayList<>();

		EntityUtils.lazyQuery(FormCompositeFieldEntity.class, filters).forEach(e -> {
			entries.add(EntityHelper.toObjectModel(e));
		});

		return entries;
	}

	protected static Map<String, Boolean> listAllFieldKeys(FormSectionType type, RoleRealm realm) {

		Map<String, Boolean> keys = new FluentHashMap<>();

		listSections(type, realm).forEach(section -> {

			Map<String, Boolean> o = new FluentHashMap<>();

			EntityUtils.lazyQuery(FormSimpleFieldEntity.class, QueryFilter.get("section =", section.getId()))
					.forEach(e -> {
						o.put(e.getId(), e.getIsRequired());
					});

			EntityUtils.lazyQuery(FormCompositeFieldEntity.class, QueryFilter.get("section =", section.getId()))
					.forEach(e -> {
						o.put(e.getId(), e.getIsRequired());
					});

			keys.putAll(o);
		});

		return keys;
	}

	/**
	 * This lists the keys for all simple and composite fields that exists in a
	 * section
	 */
	private static List<Key<?>> listFieldKeys(String sectionId) {

		List<Key<?>> keys = new FluentArrayList<>();

		ofy().load().type(FormSimpleFieldEntity.class).filter("section = ", sectionId).forEach(e -> {
			keys.add(Key.create(FormSimpleFieldEntity.class, e.getId()));
		});

		ofy().load().type(FormCompositeFieldEntity.class).filter("section = ", sectionId).forEach(e -> {
			keys.add(Key.create(FormCompositeFieldEntity.class, e.getId()));
		});

		return keys;
	}

	/**
	 * This gets fields names available in the given section
	 */
	@ModelMethod(functionality = { Functionality.VIEW_APPLICATION_FORM, Functionality.VIEW_SYSTEM_CONFIGURATION })
	public static Map<String, ClientRBRef> getFieldNames(FormSectionType type, String sectionId) {

		FormSectionEntity e = ofy().load().type(FormSectionEntity.class).id(sectionId).safe();

		if (!e.getType().equals(type.getValue())) {
			throw new ResourceException(ResourceException.ACCESS_NOT_ALLOWED);
		}

		Map<String, ClientRBRef> fields = new HashMap<String, ClientRBRef>();

		QueryFilter filter = QueryFilter.get("section =", sectionId);

		// Add simple fields
		fields.putAll(listSimpleFieldNames(filter));

		// Add composite fields
		fields.putAll(listCompositeFieldNames(filter));

		return fields;
	}

	/**
	 * This gets fields available in the given section
	 */
	@ModelMethod(functionality = { Functionality.VIEW_APPLICATION_FORM, Functionality.VIEW_SYSTEM_CONFIGURATION })
	public static List<Question> getFields(FormSectionType type, String sectionId) {

		FormSectionEntity e = ofy().load().type(FormSectionEntity.class).id(sectionId).safe();

		if (!e.getType().equals(type.getValue())) {
			throw new ResourceException(ResourceException.ACCESS_NOT_ALLOWED);
		}

		List<Question> fields = new FluentArrayList<>();

		QueryFilter filter = QueryFilter.get("section =", sectionId);

		// Add simple fields
		fields.addAll(listSimpleFields(filter));

		// Add composite fields
		fields.addAll(listCompositeFields(filter));

		return fields;
	}

	/**
	 * This gets all fields available in the given section(s)
	 */
	@ModelMethod(functionality = { Functionality.VIEW_APPLICATION_FORM, Functionality.VIEW_SYSTEM_CONFIGURATION })
	public static Map<String, List<Question>> getAllFields(FormSectionType type, List<String> sectionIds) {

		Map<String, List<Question>> result = new FluentHashMap<>();

		sectionIds.forEach(sectionId -> {

			result.put(sectionId, getFields(type, sectionId));
		});

		return result;
	}

	/**
	 * This deletes a given field
	 */
	@ModelMethod(functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public static void deleteField(FormSectionType type, String id) {

		boolean isDeleted = false;

		// Delete field entity

		FormSimpleFieldEntity se = ofy().load().type(FormSimpleFieldEntity.class).id(id).now();
		if (se != null && !se.getIsDefault()) {

			if (!getSectionType(se.getSection()).equals(type.getValue())) {
				throw new ResourceException(ResourceException.DELETE_NOT_ALLOWED);
			}

			ofy().delete().key(Key.create(FormSimpleFieldEntity.class, id)).now();
			isDeleted = true;
		}

		if (se == null) {
			FormCompositeFieldEntity ce = ofy().load().type(FormCompositeFieldEntity.class).id(id).now();
			if (ce != null && !ce.getIsDefault()) {

				if (!getSectionType(ce.getSection()).equals(type.getValue())) {
					throw new ResourceException(ResourceException.DELETE_NOT_ALLOWED);
				}

				ofy().delete().key(Key.create(FormCompositeFieldEntity.class, id)).now();
				isDeleted = true;
			}
		}

		// Then, delete delete saved values
		if (isDeleted) {
			if (type.equals(FormSectionType.APPLICATION_FORM)) {
				BaseUserModel.deleteFieldValues(id);
				ApplicationModel.deleteFieldValues(id);
			}
		} else {
			// <id> may be a default field
			throw new ResourceException(ResourceException.DELETE_NOT_ALLOWED);
		}
	}

	@Override
	public void install(InstallOptions options) {
		// TODO Auto-generated method stub
		
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
