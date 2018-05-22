package com.re.paas.internal.core.services;

import java.util.Map;

import com.re.paas.internal.base.core.GsonFactory;
import com.re.paas.internal.core.fusion.BaseService;
import com.re.paas.internal.core.fusion.FusionEndpoint;
import com.re.paas.internal.core.fusion.FusionHelper;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.ApplicationModel;
import com.re.paas.internal.utils.ObjectUtils;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ApplicationService extends BaseService {

	@Override
	public String uri() {
		return "/user-applications";
	}
	
	@FusionEndpoint(uri = "/create-application", bodyParams = {
			"roleName" }, method = HttpMethod.PUT, functionality = Functionality.CREATE_APPLICATION)
	public void createApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String roleName = body.getString("roleName");

		Long applicationId = ApplicationModel.newApplication(roleName);

		// Add to activity stream

		ctx.response().write(applicationId.toString()).setChunked(true).end();
	}

	@FusionEndpoint(uri = "/update-application", bodyParams = { "applicationId",
			"values" }, method = HttpMethod.POST, functionality = Functionality.UPDATE_APPLICATION)
	public void updateApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		Long applicationId = Long.parseLong(body.getString("applicationId"));
		Map<String, Object> values = body.getJsonObject("values").getMap();

		ApplicationModel.updateApplication(applicationId, ObjectUtils.toStringMap(values));
	}

	@FusionEndpoint(uri = "/submit-application", bodyParams = {
			"applicationId" }, method = HttpMethod.PUT, functionality = Functionality.SUBMIT_APPLICATION)
	public void submitApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		Long applicationId = Long.parseLong(body.getString("applicationId"));

		ApplicationModel.submitApplication(applicationId);
	}

	@FusionEndpoint(uri = "/get-application-role", requestParams = {
			"applicationId" }, functionality = Functionality.VIEW_APPLICATION_FORM)
	public void getApplicationRole(RoutingContext ctx) {

		Long applicationId = Long.parseLong(ctx.request().getParam("applicationId"));
		String roleName = ApplicationModel.getApplicationRole(applicationId);

		ctx.response().write(new JsonObject().put("role", roleName).encode()).setChunked(true).end();
	}

	@FusionEndpoint(uri = "/get-pdf-questionnaire", requestParams = {
			"roleName" }, functionality = Functionality.DOWNLOAD_QUESTIONNAIRE)
	public void getPDFQuestionnaire(RoutingContext ctx) {

		String roleName = ctx.request().getParam("roleName");
		String blobId = ApplicationModel.getPDFQuestionnaire(roleName);

		ctx.response().write(new JsonObject().put("blobId", blobId).encode()).setChunked(true).end();
	}

	@FusionEndpoint(uri = "/get-field-values", requestParams = {
			"applicationId" }, functionality = Functionality.UPDATE_APPLICATION)
	public void getApplicationFieldValues(RoutingContext ctx) {

		Long applicationId = Long.parseLong(ctx.request().getParam("applicationId"));

		Map<String, String> result = ApplicationModel.getFieldValues(applicationId);

		ctx.response().write(GsonFactory.getInstance().toJson(result)).setChunked(true).end();
	}

	@FusionEndpoint(uri = "/get-consolidated-field-values", requestParams = {
			"applicationId" }, functionality = Functionality.UPDATE_APPLICATION)
	public void getConsolidatedApplicationFieldValues(RoutingContext ctx) {

		Long applicationId = Long.parseLong(ctx.request().getParam("applicationId"));

		Map<String, String> result = ApplicationModel.getConsolidatedFieldValues(applicationId);

		ctx.response().write(GsonFactory.getInstance().toJson(result)).setChunked(true).end();
	}

	@FusionEndpoint(uri = "/accept-agent-organization-application", bodyParams = {
			"applicationId" }, method = HttpMethod.POST, functionality = Functionality.REVIEW_AGENT_ORGANIZATION_APPLICATION)
	public void acceptAgentOrganizationApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		Long principal = FusionHelper.getUserId(ctx.request());

		Long applicationId = Long.parseLong(body.getString("applicationId"));

		ApplicationModel.validateAgentOrganizationReview(applicationId, principal);

		Long id = ApplicationModel.acceptApplication(principal, applicationId);
		ctx.response().write(id.toString());
	}

	@FusionEndpoint(uri = "/accept-agent-application", bodyParams = {
			"applicationId" }, method = HttpMethod.POST, functionality = Functionality.REVIEW_AGENT_APPLICATION)
	public void acceptAgentApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		Long principal = FusionHelper.getUserId(ctx.request());

		Long applicationId = Long.parseLong(body.getString("applicationId"));

		ApplicationModel.validateAgentReview(applicationId, principal);

		Long id = ApplicationModel.acceptApplication(principal, applicationId);
		ctx.response().write(id.toString());
	}

	@FusionEndpoint(uri = "/accept-admin-application", bodyParams = {
			"applicationId" }, method = HttpMethod.POST, functionality = Functionality.REVIEW_ADMIN_APPLICATION)
	public void acceptAdminApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		Long principal = FusionHelper.getUserId(ctx.request());

		Long applicationId = Long.parseLong(body.getString("applicationId"));

		ApplicationModel.validateAdminReview(applicationId, principal);

		Long id = ApplicationModel.acceptApplication(principal, applicationId);
		ctx.response().write(id.toString());
	}

	@FusionEndpoint(uri = "/get-decline-reasons", functionality = Functionality.REVIEW_AGENT_ORGANIZATION_APPLICATION)
	public void getApplicationDeclineReasons(RoutingContext ctx) {

		Map<Integer, Object> result = ApplicationModel.getApplicationDeclineReasons();
		ctx.response().write(GsonFactory.getInstance().toJson(result)).setChunked(true);
	}

	@FusionEndpoint(uri = "/decline-agent-organization-application", bodyParams = { "applicationId",
			"reason" }, method = HttpMethod.PUT, functionality = Functionality.REVIEW_AGENT_ORGANIZATION_APPLICATION)
	public void declineAgentOrganizationApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		Long applicationId = Long.parseLong(body.getString("applicationId"));
		Long principal = FusionHelper.getUserId(ctx.request());
		Integer reason = Integer.parseInt(body.getString("reason"));

		ApplicationModel.validateAgentOrganizationReview(applicationId, principal);

		ApplicationModel.declineApplication(applicationId, principal, reason);
	}

	@FusionEndpoint(uri = "/decline-agent-application", bodyParams = { "applicationId",
			"reason" }, method = HttpMethod.PUT, functionality = Functionality.REVIEW_AGENT_APPLICATION)
	public void declineAgentApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		Long applicationId = Long.parseLong(body.getString("applicationId"));
		Long principal = FusionHelper.getUserId(ctx.request());
		Integer reason = Integer.parseInt(body.getString("reason"));

		ApplicationModel.validateAgentReview(applicationId, principal);

		ApplicationModel.declineApplication(applicationId, principal, reason);
	}

	@FusionEndpoint(uri = "/decline-admin-application", bodyParams = { "applicationId",
			"reason" }, method = HttpMethod.PUT, functionality = Functionality.REVIEW_ADMIN_APPLICATION)
	public void declineAdminApplication(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		Long applicationId = Long.parseLong(body.getString("applicationId"));
		Long principal = FusionHelper.getUserId(ctx.request());
		Integer reason = Integer.parseInt(body.getString("reason"));

		ApplicationModel.validateAdminReview(applicationId, principal);

		ApplicationModel.declineApplication(applicationId, principal, reason);
	}

	@FusionEndpoint(uri = "/can-user-review", requestParams = {}, functionality = Functionality.VIEW_AGENT_ORGANIZATION_APPLICATIONS)
	public void canUserReviewApplications(RoutingContext ctx) {
		isAccessAllowed(ctx, Functionality.REVIEW_AGENT_ORGANIZATION_APPLICATION);
	}

}
