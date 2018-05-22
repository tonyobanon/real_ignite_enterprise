package com.re.paas.internal.errors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.re.paas.internal.base.core.Exceptions;

public class ErrorHelper {

	static Method getFromMethod(Class<?> clazz) {
		Method o = null;
		try {
			o = clazz.getMethod("from", Integer.class);
		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			Exceptions.throwRuntime(e);
		}
		return o;
	}

	public static Error getError(String namespace, int code) {
		Class<Error> errorClass = ErrorSpiDelegate.getResources().get(namespace);
		if (errorClass == null) {
			return null;
		}
		Error o = null;
		try {
			o = (Error) getFromMethod(errorClass).invoke(null, code);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Exceptions.throwRuntime(e);
		}
		return o;
	}

}
