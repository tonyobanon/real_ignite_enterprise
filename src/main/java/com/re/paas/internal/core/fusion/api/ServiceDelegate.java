package com.re.paas.internal.core.fusion.api;

import java.util.List;

import com.google.common.collect.Multimap;
import com.re.paas.internal.core.fusion.RouteHandler;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.spi.SpiDelegate;

public abstract class ServiceDelegate extends SpiDelegate<BaseService> {

	public abstract Multimap<Route, RouteHandler> getRouteHandlers();
	
	/**
	 * Note: All routes paths returned are not prefixed with
	 * {@code APIRoutes.BASE_PATH}. All callers should consolidate this when setting
	 * up their respective containers. <br>
	 * Also, note that exceptions will possibly be thrown during the execution of
	 * these handlers, and callers should create proper exception catching mechanism
	 * on their containers. <br>
	 * Callers should create mechanisms to properly end the response after all
	 * handlers, have finished execution
	 */
	public abstract List<RouteHandler> getRouteHandlers(Route route);

	public abstract Functionality getRouteFunctionality(Route route);

	public abstract List<String> getFunctionalityRoute(Functionality functionality);

}
