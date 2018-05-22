package com.re.paas.internal.base.classes;

public class ObjectWrapper<T>{

	private T o;
	
	public ObjectWrapper<T> set(T o) {
		this.o = o;
		return this;
	}
	
	public T get() {
		return o;
	} 
	
}
