package com.re.paas.internal.models;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.base.core.Todo;
import com.re.paas.internal.core.keys.ConfigKeys;
import com.re.paas.internal.entites.ConfigEntity;

@Todo("Add functionality, that allow incremental updates to config parameters from the frontend, "
		+ "i.e uploading a config file")
@BlockerTodo("Use Ibm Icu as the default locale provider")
public class ConfigModel implements BaseModel {

	@Override
	public String path() {
		return "core/config";
	}

	@Override
	public void preInstall() {
	}

	@Override
	public void install(InstallOptions options) {

		ConfigModel.put(ConfigKeys.ORGANIZATION_NAME, options.getCompanyName());
		ConfigModel.put(ConfigKeys.ORGANIZATION_LOGO_URL, options.getCompanyLogoUrl());
		ConfigModel.put(ConfigKeys.ORGANIZATION_COUNTRY, options.getCountry());
		ConfigModel.put(ConfigKeys.ORGANIZATION_AUDIENCE, options.getAudience());

		ConfigModel.put(ConfigKeys.DEFAULT_CURRENCY, options.getCurrency());
		ConfigModel.put(ConfigKeys.DEFAULT_TIMEZONE, options.getTimezone());

	}

	public static Map<String, Object> getAll(String... keys) {
		Map<String, Object> result = new HashMap<String, Object>(keys.length);
		ofy().load().type(ConfigEntity.class).ids(keys).forEach((k, v) -> {
			result.put(k, v != null ? v.getValue() : null);
		});
		return result;
	}

	public static String get(String key) {
		ConfigEntity e = ofy().load().type(ConfigEntity.class).id(key).now();
		return e != null ? e.getValue() : null;
	}

	public static Object put(String key, Object value) {
		if (value == null) {
			return null;
		}
		ofy().save().entity(new ConfigEntity().setKey(key).setValue(value)).now();
		return value;
	}

	public static void putAll(Map<String, Object> values) {

		List<ConfigEntity> e = new ArrayList<>();

		values.forEach((k, v) -> {
			if (v != null) {
				e.add(new ConfigEntity().setKey(k).setValue(v));
			}
		});

		ofy().save().entities(e).now();
	}

	public static void delete(String key) {
		ofy().delete().key(Key.create(ConfigEntity.class, key)).now();
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
