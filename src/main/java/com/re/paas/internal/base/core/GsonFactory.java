package com.re.paas.internal.base.core;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.re.paas.internal.base.classes.gsonserializers.DateDeserializer;
import com.re.paas.internal.base.classes.gsonserializers.DateSerializer;

public class GsonFactory {

	private static final Gson instance = createInstance();

	private static Gson createInstance() {
		
		return new GsonBuilder()
				.enableComplexMapKeySerialization()
				// .serializeNulls()
				.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setPrettyPrinting()
				// .setVersion(1.0)
				.registerTypeAdapter(Date.class, new DateSerializer())
				.registerTypeAdapter(Date.class, new DateDeserializer())

				.create();
	}

	public static Gson getInstance() {
		return instance;
	}

	public static <T> T fromJson(String json, Class<T> type) {
		return instance.fromJson(json, type);
	}
	
	public static <V> Map<String, String> toMap(JsonObject obj) {
		Map<String, String> entries = new HashMap<>(obj.size());
		obj.entrySet().forEach((e) -> {
			entries.put(e.getKey(), e.getValue().getAsString());
		});
		return entries;
	}

	static {
	}
}
