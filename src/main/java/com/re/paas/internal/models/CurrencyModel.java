package com.re.paas.internal.models;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.Map;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;

import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.FormSectionType;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.classes.TimeUnit;
import com.re.paas.internal.base.classes.spec.TokenCredentials;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.core.forms.InputType;
import com.re.paas.internal.core.forms.SimpleEntry;
import com.re.paas.internal.core.keys.ConfigKeys;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.entites.CurrencyRatesEntity;
import com.re.paas.internal.models.helpers.Dates;
import com.re.paas.internal.utils.Utils;

import io.vertx.core.json.JsonObject;

public class CurrencyModel implements BaseModel {

	private static TokenCredentials credentials = null;

	@Override
	public String path() {
		return "ext/services/currency";
	}

	@Override
	public void preInstall() {

		// Create configuration fields

		String sectionId = FormModel.newSection(ClientRBRef.get("currency_layer_api_settings"), null,
				FormSectionType.SYSTEM_CONFIGURATION, RoleRealm.ADMIN);

		ConfigModel.put(ConfigKeys.CURRENCY_LAYER_FORM_SECTION_ID, sectionId);

		String urlField = FormModel.newSimpleField(FormSectionType.SYSTEM_CONFIGURATION, sectionId,
				new SimpleEntry(InputType.TEXT, ClientRBRef.get("url")));

		String accessTokenField = FormModel.newSimpleField(FormSectionType.SYSTEM_CONFIGURATION, sectionId,
				new SimpleEntry(InputType.SECRET, ClientRBRef.get("access_token")));

		String refreshIntervalField = FormModel.newSimpleField(FormSectionType.SYSTEM_CONFIGURATION, sectionId,
				new SimpleEntry(InputType.TEXT, ClientRBRef.get("refresh_interval")));

		ConfigModel
				.putAll(new FluentHashMap<String, Object>().with(ConfigKeys.CURRENCY_LAYER_API_URL_FIELD_ID, urlField)
						.with(ConfigKeys.CURRENCY_LAYER_API_ACCESS_TOKEN_FIELD_ID, accessTokenField)
						.with(ConfigKeys.CURRENCY_LAYER_REFRESH_INTERVAL_FIELD_ID, refreshIntervalField));

		ConfigModel.putAll(new FluentHashMap<String, Object>().with(urlField, "http://www.apilayer.net/api/live")
				.with(accessTokenField, "5f8ea7f3810655cc37e9ba486a217559").with(refreshIntervalField, 30));

		start();
	}

	@Override
	public void start() {
		
		String defaultCurrency = defaultCurrency();
		
		// Set the forex rates for all supported countries
		RBModel.availableCountries().forEach(c -> {
			if (!c.equals(defaultCurrency)) {
				getCurrencyRate(defaultCurrency, c);
			}
		});

	}

	protected static Double getCurrencyRate(String currency) {
		return getCurrencyRate(ConfigModel.get(ConfigKeys.DEFAULT_CURRENCY).toString(), currency);
	}

	protected static Double getCurrencyRate(String base, String currency) {

		if (base.equals(currency)) {
			return 1.0;
		}

		Integer refreshInterval = getRefreshInterval();

		CurrencyRatesEntity e = ofy().load().type(CurrencyRatesEntity.class).id(base + currency).now();

		boolean minutesElapsed = Math.abs(Utils.getTimeOffset(TimeUnit.MINUTES, e.getLastUpdated())) > refreshInterval;

		if (!(e == null || minutesElapsed)) {
			return e.getRate();
		}

		Double rate = _getCurrencyRate(base, currency);

		e = new CurrencyRatesEntity().setPair(base + currency).setRate(rate).setLastUpdated(Dates.now());

		ofy().save().entity(e).now();

		return rate;
	}

	private static Integer getRefreshInterval() {
		Map<String, Object> keys = ConfigModel.getAll(ConfigKeys.CURRENCY_LAYER_REFRESH_INTERVAL_FIELD_ID);

		Map<String, Object> values = ConfigModel.getAll(keys.values().toArray(new String[keys.values().size()]));

		String refreshInterval = (String) values.get(keys.get(ConfigKeys.CURRENCY_LAYER_REFRESH_INTERVAL_FIELD_ID));
		return Integer.parseInt(refreshInterval);
	}

	private static TokenCredentials _getCredentials() {

		Map<String, Object> keys = ConfigModel.getAll(ConfigKeys.CURRENCY_LAYER_API_URL_FIELD_ID,
				ConfigKeys.CURRENCY_LAYER_API_ACCESS_TOKEN_FIELD_ID);

		Map<String, Object> values = ConfigModel.getAll(keys.values().toArray(new String[keys.values().size()]));

		String url = (String) values.get(keys.get(ConfigKeys.CURRENCY_LAYER_API_URL_FIELD_ID));
		String token = (String) values.get(keys.get(ConfigKeys.CURRENCY_LAYER_API_ACCESS_TOKEN_FIELD_ID));

		return CurrencyModel.credentials = new TokenCredentials().setToken(token).setUrl(url);
	}

	private static TokenCredentials getCredentials() {
		return credentials != null ? credentials : _getCredentials();
	}

	private static Double _getCurrencyRate(String base, String currency) {

		TokenCredentials credentials = getCredentials();

		try {

			Content content = Request.Get(credentials.getUrl() + "?access_key=" + credentials.getToken() + "&source="
					+ base + "&currencies=" + currency).execute().returnContent();

			JsonObject response = new JsonObject(content.asString());

			return response.getJsonObject("quotes").getDouble(base + currency);

		} catch (Exception e) {
			Exceptions.throwRuntime(e);
			return null;
		}
	}

	protected static String defaultCurrency() {
		return ConfigModel.get(ConfigKeys.DEFAULT_CURRENCY);
	}

	@Override
	public void install(InstallOptions options) {
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
