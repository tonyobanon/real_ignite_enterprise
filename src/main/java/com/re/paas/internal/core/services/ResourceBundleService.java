package com.re.paas.internal.core.services;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.re.paas.internal.base.core.GsonFactory;
import com.re.paas.internal.core.fusion.BaseService;
import com.re.paas.internal.core.fusion.FusionEndpoint;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.LocationModel;
import com.re.paas.internal.models.RBModel;
import com.re.paas.internal.utils.LocaleUtils;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class ResourceBundleService extends BaseService {
	@Override
	public String uri() {
		return "/resource-bundle";
	}

	public static final String DEFAULT_LOCALE_COOKIE = "DEFAULT_LOCALE";

	@FusionEndpoint(uri = "/get-available-countries", functionality = Functionality.GET_AVAILABLE_COUNTRIES)
	public void getAvailableCountries(RoutingContext ctx) {

		// K: locale, V: name, code
		Map<String, JsonObject> result = new HashMap<>();

		RBModel.getLocaleCountries().forEach((language, countries) -> {

			LocationModel.getCountryNames(countries).forEach((code, name) -> {

				String k = language + LocaleUtils.LANGUAGE_COUNTRY_DELIMETER + code;

				JsonObject v = new JsonObject();
				v.addProperty("code", code);
				v.addProperty("name", name);

				result.put(k, v);
			});

		});

		ctx.response().write(GsonFactory.getInstance().toJson(result)).setChunked(true);
	}

	@FusionEndpoint(uri = "/get-rb-entry", bodyParams = {
			"keys" }, method = HttpMethod.POST, functionality = Functionality.GET_RESOURCE_BUNDLE_ENTRIES)
	public void getRbEntry(RoutingContext ctx) {

		Map<String, Object> keys = ctx.getBodyAsJson().getJsonObject("keys").getMap();
		Map<String, String> result = new HashMap<>();

		keys.forEach((k, v) -> {
			result.put(k, RBModel.get(v.toString()));
		});

		ctx.response().write(GsonFactory.getInstance().toJson(result)).setChunked(true);
	}

}
