package com.re.paas.internal.core.storageapi;

public interface DatabaseStorage {

	public Object get();
	
	public Object getAll();
	
	public void put();
	
	public void putAll();
	
	public void query();
}
