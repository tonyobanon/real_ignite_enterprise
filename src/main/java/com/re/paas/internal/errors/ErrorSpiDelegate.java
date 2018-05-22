package com.re.paas.internal.errors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.spi.SpiTypes;

public class ErrorSpiDelegate extends SpiDelegate<Error> {

	@Override
	protected void init() {

		Consumer<Class<Error>> consumer = c -> {
			addError(c);
		};

		get(consumer);
	}

	private void addError(Class<Error> c) {

		// Verify that c contains a static from(int) method
		Method from = ErrorHelper.getFromMethod(c);

		if (!(from != null && Modifier.isStatic(from.getModifiers()))) {
			Exceptions.throwRuntime("Class: " + c.getName() + " should have a static method: from(int)");
		}

		String namespace = getNamespace(c);

		if (get(namespace) != null) {
			Exceptions.throwRuntime("Class: " + c.getName() + " needs to be renamed because namespace: '" + namespace
					+ "' already exists");
		}

		set(namespace, c);
	}

	private static final String getNamespace(Class<?> c) {

		String namespace = null;

		try {
			// Get namespace
			namespace = (String) c.getMethod("namespace").invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			Exceptions
					.throwRuntime("Class: " + c.getName() + " must contain a public static no-arg method: 'namespace'");
		}

		return namespace;
	}

	private void removeError(Class<Error> c) {
		remove(getNamespace(c));
	}

	@SuppressWarnings("unchecked")
	static Map<String, Class<Error>> getResources() {

		Map<String, Class<Error>> map = Maps.newHashMap();

		getAll(SpiTypes.ERROR).forEach((k, v) -> {
			map.put((String) k, (Class<Error>) v);
		});

		return map;
	}

	@Override
	protected void add(List<Class<Error>> classes) {
		classes.forEach(c -> {
			addError(c);
		});
	}

	@Override
	protected void remove(List<Class<Error>> classes) {
		classes.forEach(c -> {
			removeError(c);
		});
	}

}
