package com.re.paas.internal.models.helpers;

import java.util.Map;

import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.FormSectionType;
import com.re.paas.internal.base.classes.Gender;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.core.forms.CompositeEntry;
import com.re.paas.internal.core.forms.InputType;
import com.re.paas.internal.core.forms.SimpleEntry;
import com.re.paas.internal.core.fusion.api.BaseService;
import com.re.paas.internal.core.fusion.api.ServiceDelegate;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.models.ConfigModel;
import com.re.paas.internal.models.FormModel;

@BlockerTodo("Add proper sort order for form section and questions")
public class FormFieldRepository {

	private static final String FORM_FIELD_MAPPING_KEY_PREFIX = "FORM_FIELD_MAPPING_";

	public static void createDefaultFields() {

		ServiceDelegate serviceDelegate = BaseService.getDelegate();

		FormModel.newSection(ClientRBRef.get("profile_information"), FormSectionType.APPLICATION_FORM)
				.forEach((k, v) -> {

					saveFieldId(k, FieldType.FIRST_NAME,
							FormModel.newSimpleField(v,
									(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("first_name"))
											.setSortOrder(2).setIsDefault(true)));

					saveFieldId(k, FieldType.LAST_NAME,
							FormModel.newSimpleField(v,
									(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("last_name"))
											.setSortOrder(4).setIsDefault(true)));

					saveFieldId(k, FieldType.EMAIL,
							FormModel.newSimpleField(v,
									(SimpleEntry) new SimpleEntry(InputType.EMAIL, ClientRBRef.get("email"))
											.setSortOrder(7).setIsDefault(true)));

					saveFieldId(k, FieldType.PASSWORD,
							FormModel.newSimpleField(v,
									(SimpleEntry) new SimpleEntry(InputType.SECRET, ClientRBRef.get("password"))
											.setSortOrder(8).setIsDefault(true)));

					saveFieldId(k, FieldType.PHONE_NUMBER,
							FormModel.newSimpleField(v,
									(SimpleEntry) new SimpleEntry(InputType.PHONE, ClientRBRef.get("phone_number"))
											.setSortOrder(9).setIsDefault(true)));

					saveFieldId(k, FieldType.PREFERRED_LOCALE,
							FormModel.newSimpleField(v,
									(SimpleEntry) new SimpleEntry(InputType.PREFERED_LOCALE,
											ClientRBRef.get("preferred_locale")).setSortOrder(10).setIsDefault(true)
													.setIsRequired(false).setIsVisible(false)));

					switch (k) {

					case ADMIN:
					case ORGANIZATION_ADMIN:
					case AGENT:

						saveFieldId(k, FieldType.IMAGE,
								FormModel.newSimpleField(v,
										(SimpleEntry) new SimpleEntry(InputType.IMAGE, ClientRBRef.get("passport"))
												.setSortOrder(1).setIsDefault(true).setIsRequired(false)));

						saveFieldId(k, FieldType.MIDDLE_NAME,
								FormModel.newSimpleField(v,
										(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("middle_name"))
												.setSortOrder(3).setIsDefault(true).setIsRequired(false)));

						saveFieldId(k, FieldType.GENDER,
								FormModel.newCompositeField(v,
										(CompositeEntry) new CompositeEntry(ClientRBRef.get("gender"))
												.withItem(ClientRBRef.get("male"), Gender.MALE.getValue())
												.withItem(ClientRBRef.get("female"), Gender.FEMALE.getValue())
												.setSortOrder(5).setIsDefault(true)));

						saveFieldId(k, FieldType.DATE_OF_BIRTH, FormModel.newSimpleField(v,
								(SimpleEntry) new SimpleEntry(InputType.DATE_OF_BIRTH, ClientRBRef.get("date_of_birth"))
										.setSortOrder(6).setIsDefault(true)));

						break;
					}

				});

		FormModel.newSection(ClientRBRef.get("contact_information"), FormSectionType.APPLICATION_FORM)
				.forEach((k, v) -> {

					switch (k) {

					case ADMIN:
					case ORGANIZATION_ADMIN:
					case AGENT:
						
						saveFieldId(k, FieldType.ADDRESS,
								FormModel.newSimpleField(v,
										(SimpleEntry) new SimpleEntry(InputType.ADDRESS, ClientRBRef.get("address"))
												.setSortOrder(2).setIsDefault(true)));

						String countryField = FormModel.newCompositeField(v,
								(CompositeEntry) new CompositeEntry(ClientRBRef.get("country"))
										.setItemsSource(serviceDelegate.getFunctionalityRoute(Functionality.GET_COUNTRY_NAMES).get(0))
										.setSortOrder(3).setIsDefault(true));
						saveFieldId(k, FieldType.COUNTRY, countryField);

						String stateField = FormModel.newCompositeField(v,
								(CompositeEntry) new CompositeEntry(ClientRBRef.get("state"))
										.setItemsSource(serviceDelegate.getFunctionalityRoute(Functionality.GET_TERRITORY_NAMES).get(0))
										.setContext(countryField).setSortOrder(4).setIsDefault(true));
						saveFieldId(k, FieldType.STATE, stateField);

						String cityField = FormModel.newCompositeField(v,
								(CompositeEntry) new CompositeEntry(ClientRBRef.get("city"))
										.setItemsSource(serviceDelegate.getFunctionalityRoute(Functionality.GET_CITY_NAMES).get(0))
										.setContext(stateField).setSortOrder(5).setIsDefault(true));
						saveFieldId(k, FieldType.CITY, cityField);

						saveFieldId(k, FieldType.FACEBOOK_PROFILE, FormModel.newSimpleField(v,
								(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("facebook_profile"))
										.setSortOrder(6).setIsDefault(true).setIsRequired(false)));

						saveFieldId(k, FieldType.TWITTER_PROFILE, FormModel.newSimpleField(v,
								(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("twitter_profile"))
										.setSortOrder(7).setIsDefault(true).setIsRequired(false)));

						saveFieldId(k, FieldType.LINKEDIN_PROFILE, FormModel.newSimpleField(v,
								(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("linkedin_profile"))
										.setSortOrder(8).setIsDefault(true).setIsRequired(false)));

						saveFieldId(k, FieldType.SKYPE_PROFILE,
								FormModel.newSimpleField(v,
										(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("skype_profile"))
												.setSortOrder(9).setIsDefault(true).setIsRequired(false)));

					}
				});

		FormModel.newSection(ClientRBRef.get("organization_profile"), FormSectionType.APPLICATION_FORM)
				.forEach((k, v) -> {

					switch (k) {

					case ORGANIZATION_ADMIN:

						saveFieldId(k, FieldType.ORGANIZATION_NAME, FormModel.newSimpleField(v,
								(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("organization_name"))
										.setSortOrder(2).setIsDefault(true)));

						saveFieldId(k, FieldType.ORGANIZATION_EMAIL, FormModel.newSimpleField(v,
								(SimpleEntry) new SimpleEntry(InputType.EMAIL, ClientRBRef.get("organization_email"))
										.setSortOrder(4).setIsDefault(true)));

						saveFieldId(k, FieldType.ORGANIZATION_PHONE, FormModel.newSimpleField(v,
								(SimpleEntry) new SimpleEntry(InputType.PHONE, ClientRBRef.get("organization_phone"))
										.setSortOrder(5).setIsDefault(true)));

						saveFieldId(k, FieldType.ORGANIZATION_LOGO,
								FormModel.newSimpleField(v,
										(SimpleEntry) new SimpleEntry(InputType.IMAGE, ClientRBRef.get("logo"))
												.setSortOrder(6).setIsDefault(true)));

						saveFieldId(k, FieldType.ORGANIZATION_ADDRESS,
								FormModel.newSimpleField(v,
										(SimpleEntry) new SimpleEntry(InputType.ADDRESS, ClientRBRef.get("address"))
												.setSortOrder(8).setIsDefault(true)));

						saveFieldId(k, FieldType.ORGANIZATION_POSTAL_CODE,
								FormModel.newSimpleField(v,
										(SimpleEntry) new SimpleEntry(InputType.PLAIN, ClientRBRef.get("postal_code"))
												.setSortOrder(8).setIsDefault(true)));

						String countryField = FormModel.newCompositeField(v,
								(CompositeEntry) new CompositeEntry(ClientRBRef.get("country"))
										.setItemsSource(serviceDelegate.getFunctionalityRoute(Functionality.GET_COUNTRY_NAMES).get(0))
										.setSortOrder(10).setIsDefault(true));
						saveFieldId(k, FieldType.ORGANIZATION_COUNTRY, countryField);

						String stateField = FormModel.newCompositeField(v,
								(CompositeEntry) new CompositeEntry(ClientRBRef.get("state"))
										.setItemsSource(serviceDelegate.getFunctionalityRoute(Functionality.GET_TERRITORY_NAMES).get(0))
										.setContext(countryField).setSortOrder(12).setIsDefault(true));
						saveFieldId(k, FieldType.ORGANIZATION_STATE, stateField);

						String cityField = FormModel.newCompositeField(v,
								(CompositeEntry) new CompositeEntry(ClientRBRef.get("city"))
										.setItemsSource(serviceDelegate.getFunctionalityRoute(Functionality.GET_CITY_NAMES).get(0))
										.setContext(stateField).setSortOrder(14).setIsDefault(true));
						saveFieldId(k, FieldType.ORGANIZATION_CITY, cityField);

						break;
					}

				});

		FormModel.newSection("agent_profile", FormSectionType.APPLICATION_FORM).forEach((k, v) -> {

			switch (k) {

			case AGENT:

				String cityField = getFieldId(k, FieldType.CITY);

				String organizationField = FormModel.newCompositeField(v,
						(CompositeEntry) new CompositeEntry(ClientRBRef.get("organization"))
								.setItemsSource(serviceDelegate.getFunctionalityRoute(Functionality.LIST_AGENT_ORGANIZATION_NAMES).get(0))
								.setContext(cityField).setSortOrder(2).setIsDefault(true));
				saveFieldId(k, FieldType.AGENT_ORGANIZATION, organizationField);

				saveFieldId(k, FieldType.YEARS_OF_EXPERIENCE, FormModel.newSimpleField(v,
						(SimpleEntry) new SimpleEntry(InputType.NUMBER_2L, ClientRBRef.get("years_of_experience"))
								.setSortOrder(4).setIsDefault(true)));

				break;
			}

		});

	}

	public static String getRefField(RoleRealm realm) {

		String ref = null;

		switch (realm) {
		case AGENT:
			ref = getFieldId(realm, FieldType.AGENT_ORGANIZATION);
			break;
		}

		return ref;
	}

	public static Map<FieldType, String> getFieldIds(RoleRealm realm) {

		FluentHashMap<FieldType, String> result = new FluentHashMap<FieldType, String>()

				.with(FieldType.FIRST_NAME, FormFieldRepository.getFieldId(realm, FieldType.FIRST_NAME))
				.with(FieldType.LAST_NAME, FormFieldRepository.getFieldId(realm, FieldType.LAST_NAME))
				.with(FieldType.EMAIL, FormFieldRepository.getFieldId(realm, FieldType.EMAIL))
				.with(FieldType.PASSWORD, FormFieldRepository.getFieldId(realm, FieldType.PASSWORD))
				.with(FieldType.PHONE_NUMBER, FormFieldRepository.getFieldId(realm, FieldType.PHONE_NUMBER))
				.with(FieldType.PREFERRED_LOCALE, FormFieldRepository.getFieldId(realm, FieldType.PREFERRED_LOCALE));

		switch (realm) {

		case ADMIN:
		case AGENT:
		case ORGANIZATION_ADMIN:

			result

					.with(FieldType.IMAGE, FormFieldRepository.getFieldId(realm, FieldType.IMAGE))
					.with(FieldType.MIDDLE_NAME, FormFieldRepository.getFieldId(realm, FieldType.MIDDLE_NAME))
					.with(FieldType.GENDER, FormFieldRepository.getFieldId(realm, FieldType.GENDER))
					.with(FieldType.DATE_OF_BIRTH, FormFieldRepository.getFieldId(realm, FieldType.DATE_OF_BIRTH))

					.with(FieldType.ADDRESS, FormFieldRepository.getFieldId(realm, FieldType.ADDRESS))
					.with(FieldType.CITY, FormFieldRepository.getFieldId(realm, FieldType.CITY))
					.with(FieldType.STATE, FormFieldRepository.getFieldId(realm, FieldType.STATE))
					.with(FieldType.COUNTRY, FormFieldRepository.getFieldId(realm, FieldType.COUNTRY))

					.with(FieldType.FACEBOOK_PROFILE, FormFieldRepository.getFieldId(realm, FieldType.FACEBOOK_PROFILE))
					.with(FieldType.TWITTER_PROFILE, FormFieldRepository.getFieldId(realm, FieldType.TWITTER_PROFILE))
					.with(FieldType.LINKEDIN_PROFILE, FormFieldRepository.getFieldId(realm, FieldType.LINKEDIN_PROFILE))
					.with(FieldType.SKYPE_PROFILE, FormFieldRepository.getFieldId(realm, FieldType.SKYPE_PROFILE));

		}

		switch (realm) {

		case ADMIN:
			result.with(FieldType.ORGANIZATION_NAME, FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_NAME))
					.with(FieldType.ORGANIZATION_EMAIL,
							FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_EMAIL))
					.with(FieldType.ORGANIZATION_PHONE,
							FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_PHONE))
					.with(FieldType.ORGANIZATION_LOGO,
							FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_LOGO))
					.with(FieldType.ORGANIZATION_ADDRESS,
							FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_ADDRESS))
					.with(FieldType.ORGANIZATION_POSTAL_CODE,
							FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_POSTAL_CODE))
					.with(FieldType.ORGANIZATION_CITY,
							FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_CITY))
					.with(FieldType.ORGANIZATION_STATE,
							FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_STATE))
					.with(FieldType.ORGANIZATION_COUNTRY,
							FormFieldRepository.getFieldId(realm, FieldType.ORGANIZATION_COUNTRY));
			break;

		case AGENT:
			result.with(FieldType.AGENT_ORGANIZATION,
					FormFieldRepository.getFieldId(realm, FieldType.AGENT_ORGANIZATION))
					.with(FieldType.YEARS_OF_EXPERIENCE,
							FormFieldRepository.getFieldId(realm, FieldType.YEARS_OF_EXPERIENCE));
			break;

		default:
			break;
		}

		return result;
	}

	private static void saveFieldId(RoleRealm realm, FieldType fieldType, String Id) {
		String key = FORM_FIELD_MAPPING_KEY_PREFIX + realm.name() + "_" + fieldType;
		ConfigModel.put(key, Id);
	}

	public static String getFieldId(RoleRealm realm, FieldType fieldType) {
		String key = FORM_FIELD_MAPPING_KEY_PREFIX + realm.name() + "_" + fieldType;
		Object value = ConfigModel.get(key);
		return value != null ? value.toString() : null;
	}

	public enum FieldType {
		PREFERRED_LOCALE, IMAGE, FIRST_NAME, MIDDLE_NAME, LAST_NAME, DATE_OF_BIRTH, GENDER, EMAIL, PASSWORD, PHONE_NUMBER, ADDRESS, CITY, STATE, COUNTRY, POSTAL_CODE, ORGANIZATION_NAME, ORGANIZATION_EMAIL, ORGANIZATION_PHONE, ORGANIZATION_LOGO, ORGANIZATION_ADDRESS, ORGANIZATION_POSTAL_CODE, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, YEARS_OF_EXPERIENCE, AGENT_ORGANIZATION, FACEBOOK_PROFILE, TWITTER_PROFILE, LINKEDIN_PROFILE, SKYPE_PROFILE
	}

}
