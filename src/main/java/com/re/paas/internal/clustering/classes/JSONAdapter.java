package com.re.paas.internal.clustering.classes;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class JSONAdapter {
	
	private final JsonObjectBuilder jsonObjectImpl;

	public JSONAdapter() {
		jsonObjectImpl = Json.createObjectBuilder();
	}

	public JSONAdapter withString(String k, String v){
		jsonObjectImpl.add(k, v);
		return this;
	}
	
	public JSONAdapter withNumber(String k, Integer v){
		jsonObjectImpl.add(k, v);
		return this;
	}
	
	public JSONAdapter withNumber(String k, Long v){
		jsonObjectImpl.add(k, v);
		return this;
	}
	
	public JsonObject build(){
		return jsonObjectImpl.build();
	}
}
