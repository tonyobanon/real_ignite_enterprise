package com.re.paas.internal.base.classes;

import java.util.HashMap;

public class FluentHashMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 1L;
	
	public FluentHashMap<K, V> with(K key, V value){
		super.put(key, value);
		return this;
	}
	
	public static FluentHashMap<String, String> forNameMap(){
		return new FluentHashMap<String, String>();
	}
	
	public static FluentHashMap<String, Object> forValueMap(){
		return new FluentHashMap<String, Object>();
	}
}
