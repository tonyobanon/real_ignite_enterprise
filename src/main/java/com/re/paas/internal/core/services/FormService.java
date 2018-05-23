package com.re.paas.internal.core.services;

import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.FormSectionType;
import com.re.paas.internal.base.core.GsonFactory;
import com.re.paas.internal.core.forms.CompositeEntry;
import com.re.paas.internal.core.forms.Question;
import com.re.paas.internal.core.forms.SimpleEntry;
import com.re.paas.internal.core.fusion.api.BaseService;
import com.re.paas.internal.core.fusion.api.FusionEndpoint;
import com.re.paas.internal.core.pdf.Section;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.models.FormModel;
import com.re.paas.internal.models.helpers.FormFieldRepository;
import com.re.paas.internal.models.helpers.FormFieldRepository.FieldType;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class FormService extends BaseService {
	@Override
	public String uri() {
		return "/forms";
	}

	@FusionEndpoint(uri = "/new-application-form-section", bodyParams = { "name", "description",
			"realm" }, method = HttpMethod.PUT, functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public void newApplicationFormSection(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String name = body.getString("name");
		String description = body.getString("description");

		RoleRealm roleRealm = RoleRealm.from(body.getInteger("realm"));

		FormModel.newSection(name, description, FormSectionType.APPLICATION_FORM, roleRealm);
	}

	@FusionEndpoint(uri = "/new-system-configuration-section", bodyParams = { "name", "description",
			"realm" }, method = HttpMethod.PUT, functionality = Functionality.MANAGE_SYSTEM_CONFIGURATION_FORM)
	public void newSystemConfigurationSection(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String name = body.getString("name");
		String description = body.getString("description");

		RoleRealm roleRealm = RoleRealm.from(body.getInteger("realm"));

		FormModel.newSection(name, description, FormSectionType.SYSTEM_CONFIGURATION, roleRealm);
	}

	@FusionEndpoint(uri = "/list-application-form-sections", requestParams = {
			"realm" }, functionality = Functionality.VIEW_APPLICATION_FORM)
	public void listApplicationFormSections(RoutingContext ctx) {

		RoleRealm roleRealm = RoleRealm.from(Integer.parseInt(ctx.request().getParam("realm")));
		List<Section> sections = FormModel.listSections(FormSectionType.APPLICATION_FORM, roleRealm);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(sections)).end();
	}

	@FusionEndpoint(uri = "/list-system-configuration-sections", functionality = Functionality.VIEW_SYSTEM_CONFIGURATION)
	public void listSystemConfigurationSections(RoutingContext ctx) {

		List<Section> sections = FormModel.listSections(FormSectionType.SYSTEM_CONFIGURATION, null);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(sections)).end();
	}

	@FusionEndpoint(uri = "/list-application-form-fields", requestParams = {
			"sectionId" }, functionality = Functionality.VIEW_APPLICATION_FORM)
	public void listApplicationFormFields(RoutingContext ctx) {

		String sectionId = ctx.request().getParam("sectionId");

		List<Question> fields = FormModel.getFields(FormSectionType.APPLICATION_FORM, sectionId);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(fields)).end();
	}

	@FusionEndpoint(uri = "/list-application-form-field-names", requestParams = {
			"sectionId" }, functionality = Functionality.VIEW_APPLICATION_FORM)
	public void listApplicationFormFieldNames(RoutingContext ctx) {

		String sectionId = ctx.request().getParam("sectionId");

		Map<String, ClientRBRef> fields = FormModel.getFieldNames(FormSectionType.APPLICATION_FORM, sectionId);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(fields)).end();
	}

	@FusionEndpoint(uri = "/list-all-application-form-fields", bodyParams = {
			"sectionIds" }, method = HttpMethod.POST, functionality = Functionality.VIEW_APPLICATION_FORM)
	public void listAllApplicationFormFields(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		List<String> sectionIds = GsonFactory.getInstance().fromJson(body.getJsonArray("sectionIds").encode(),
				new TypeToken<List<String>>() {
				}.getType());

		Map<String, List<Question>> fields = FormModel.getAllFields(FormSectionType.APPLICATION_FORM, sectionIds);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(fields)).end();
	}

	@FusionEndpoint(uri = "/list-system-configuration-fields", requestParams = {
			"sectionId" }, functionality = Functionality.VIEW_SYSTEM_CONFIGURATION)
	public void listSystemConfigurationFields(RoutingContext ctx) {

		String sectionId = ctx.request().getParam("sectionId");

		List<Question> fields = FormModel.getFields(FormSectionType.SYSTEM_CONFIGURATION, sectionId);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(fields)).end();
	}

	@FusionEndpoint(uri = "/list-system-configuration-field-names", requestParams = {
			"sectionId" }, functionality = Functionality.VIEW_SYSTEM_CONFIGURATION)
	public void listSystemConfigurationFieldNames(RoutingContext ctx) {

		String sectionId = ctx.request().getParam("sectionId");

		Map<String, ClientRBRef> fields = FormModel.getFieldNames(FormSectionType.SYSTEM_CONFIGURATION, sectionId);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(fields)).end();
	}

	@FusionEndpoint(uri = "/list-all-system-configuration-fields", bodyParams = {
			"sectionIds" }, method = HttpMethod.POST, functionality = Functionality.VIEW_SYSTEM_CONFIGURATION)
	public void listAllSystemConfigurationFields(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		List<String> sectionIds = GsonFactory.getInstance().fromJson(body.getJsonArray("sectionIds").encode(),
				new TypeToken<List<String>>() {
				}.getType());

		Map<String, List<Question>> fields = FormModel.getAllFields(FormSectionType.SYSTEM_CONFIGURATION, sectionIds);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(fields)).end();
	}

	@FusionEndpoint(uri = "/delete-application-form-section", requestParams = {
			"sectionId" }, method = HttpMethod.DELETE, functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public void deleteApplicationFormSection(RoutingContext ctx) {

		String sectionId = ctx.request().getParam("sectionId");
		FormModel.deleteSection(sectionId, FormSectionType.APPLICATION_FORM);
	}

	@FusionEndpoint(uri = "/delete-system-configuration-section", requestParams = {
			"sectionId" }, method = HttpMethod.DELETE, functionality = Functionality.MANAGE_SYSTEM_CONFIGURATION_FORM)
	public void deleteSystemConfigurationSection(RoutingContext ctx) {

		String sectionId = ctx.request().getParam("sectionId");
		FormModel.deleteSection(sectionId, FormSectionType.SYSTEM_CONFIGURATION);
	}

	@FusionEndpoint(uri = "/delete-application-form-field", requestParams = {
			"fieldId" }, method = HttpMethod.DELETE, functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public void deleteApplicationFormField(RoutingContext ctx) {

		String fieldId = ctx.request().getParam("fieldId");
		FormModel.deleteField(FormSectionType.APPLICATION_FORM, fieldId);
	}

	@FusionEndpoint(uri = "/delete-system-configuration-field", requestParams = {
			"fieldId" }, method = HttpMethod.DELETE, functionality = Functionality.MANAGE_SYSTEM_CONFIGURATION_FORM)
	public void deleteSystemConfigurationField(RoutingContext ctx) {

		String fieldId = ctx.request().getParam("fieldId");
		FormModel.deleteField(FormSectionType.SYSTEM_CONFIGURATION, fieldId);
	}

	@FusionEndpoint(uri = "/create-application-form-simple-field", bodyParams = { "sectionId",
			"spec" }, method = HttpMethod.PUT, functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public void newApplicationFormSimpleField(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String sectionId = body.getString("sectionId");
		SimpleEntry spec = GsonFactory.getInstance().fromJson(body.getJsonObject("spec").encode(), SimpleEntry.class);

		String id = FormModel.newSimpleField(FormSectionType.APPLICATION_FORM, sectionId, spec);

		ctx.response().setChunked(true).write(id).end();
	}

	@FusionEndpoint(uri = "/create-application-form-composite-field", bodyParams = { "sectionId",
			"spec" }, method = HttpMethod.PUT, functionality = Functionality.MANAGE_APPLICATION_FORMS)
	public void newApplicationFormCompositeField(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String sectionId = body.getString("sectionId");
		CompositeEntry spec = GsonFactory.getInstance().fromJson(body.getJsonObject("spec").encode(),
				CompositeEntry.class);

		String id = FormModel.newCompositeField(FormSectionType.APPLICATION_FORM, sectionId, spec);

		ctx.response().setChunked(true).write(id).end();
	}

	@FusionEndpoint(uri = "/create-system-configuration-simple-field", bodyParams = { "sectionId",
			"spec" }, method = HttpMethod.PUT, functionality = Functionality.MANAGE_SYSTEM_CONFIGURATION_FORM)
	public void newSystemConfigurationSimpleField(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String sectionId = body.getString("sectionId");
		SimpleEntry spec = GsonFactory.getInstance().fromJson(body.getJsonObject("spec").encode(), SimpleEntry.class);

		String id = FormModel.newSimpleField(FormSectionType.SYSTEM_CONFIGURATION, sectionId, spec);

		ctx.response().setChunked(true).write(id).end();
	}

	@FusionEndpoint(uri = "/create-system-configuration-composite-field", bodyParams = { "sectionId",
			"spec" }, method = HttpMethod.PUT, functionality = Functionality.MANAGE_SYSTEM_CONFIGURATION_FORM)
	public void newSystemConfigurationCompositeField(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String sectionId = body.getString("sectionId");
		CompositeEntry spec = GsonFactory.getInstance().fromJson(body.getJsonObject("spec").encode(),
				CompositeEntry.class);

		String id = FormModel.newCompositeField(FormSectionType.SYSTEM_CONFIGURATION, sectionId, spec);

		ctx.response().setChunked(true).write(id).end();
	}

	@FusionEndpoint(uri = "/get-application-form-field-ids", requestParams = {
			"realm" }, functionality = Functionality.GET_FORM_FIELD_IDS)
	public void getApplicationFormFieldIds(RoutingContext ctx) {

		RoleRealm roleRealm = RoleRealm.from(Integer.parseInt(ctx.request().getParam("realm")));

		Map<FieldType, String> result = FormFieldRepository.getFieldIds(roleRealm);

		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(result)).end();
	}
}
