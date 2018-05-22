package com.re.paas.internal.core.services;

import com.re.paas.internal.base.classes.ActivitityStreamTimeline;
import com.re.paas.internal.core.fusion.BaseService;
import com.re.paas.internal.core.fusion.FusionEndpoint;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.ActivityStreamModel;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ActivityStreamService extends BaseService {
  
	@Override
	public String uri() {
		return "/activity-stream";
	}
	
	@FusionEndpoint(uri = "/set-timeline", requestParams = { "timeline" }, method = HttpMethod.POST,
			functionality = Functionality.MANAGE_ACTIVITY_STREAM)
	public void setActivityStreamTimeline(RoutingContext ctx) {
		ActivitityStreamTimeline timeline = ActivitityStreamTimeline.from(Integer.parseInt(ctx.request().getParam("timeline")));
		ActivityStreamModel.setActivityTimeline(timeline);
	}
	        
	@FusionEndpoint(uri = "/get-timeline",
			functionality = Functionality.MANAGE_ACTIVITY_STREAM)
	public void getActivityStreamTimeline(RoutingContext ctx) {
		ActivitityStreamTimeline timeline = ActivityStreamModel.getActivityTimeline();
		ctx.response().setChunked(true).write(new JsonObject().put("timeline", timeline.getValue()).encode()); 
	}  
	         
	@FusionEndpoint(uri = "/is-enabled", 
			functionality = Functionality.MANAGE_ACTIVITY_STREAM)
	public void isActivityStreamEnabled(RoutingContext ctx) {
		Boolean isEnabled = ActivityStreamModel.isEnabled();
		ctx.response().setChunked(true).write(new JsonObject().put("isEnabled", isEnabled).encode());
	}    
	 
	@FusionEndpoint(uri = "/disable", method = HttpMethod.POST, 
			functionality = Functionality.MANAGE_ACTIVITY_STREAM)
	public void disableActivityStream(RoutingContext ctx) {
		ActivityStreamModel.disable(); 
	}
	
	@FusionEndpoint(uri = "/enable", method = HttpMethod.POST,
			functionality = Functionality.MANAGE_ACTIVITY_STREAM)
	public void enableActivityStream(RoutingContext ctx) {
		ActivityStreamModel.enable();
	}
	
}
