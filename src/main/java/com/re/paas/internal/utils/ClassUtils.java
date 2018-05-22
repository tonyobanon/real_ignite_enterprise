package com.re.paas.internal.utils;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.Todo;

public class ClassUtils<T> {

	private static final String classNamePattern = "([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*";
	private static final Pattern GENERIC_TYPE_PATTERN = Pattern
			.compile("((?<=\\Q<\\E)" + classNamePattern + "(\\Q, \\E" + classNamePattern + ")*" + "(?=\\Q>\\E\\z)){1}");

	
	public static <T> Class<? extends T> forName(String name, ClassLoader cl) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends T> o =  (Class<? extends T>) Class.forName(name, true, cl);
			return o;
		} catch (ClassNotFoundException e) {
			Exceptions.throwRuntime(e);
			return null;
		}
	}
	
	@BlockerTodo("Add support for caching")
	public static <T> T createInstance(Class<T> clazz) {

		try {

			return clazz.newInstance();

		} catch (InstantiationException e) {
			
			Exceptions.throwRuntime(e);
			return null;
			
		} catch (IllegalAccessException e) {

			// Try again, but this time, temporarily make the no-arg constructors accessible

			Constructor<?> modified = null;
			
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				if (c.getParameterCount() == 0) {
					c.setAccessible(true);
					modified = c;
					break;
				}
			}

			try {
				
				T o =  null;
				if(modified != null) {
					o =  clazz.newInstance();
					modified.setAccessible(false);
				}
				
				return o;

			} catch (Exception ex) {
				Exceptions.throwRuntime(e);
				return null;
			}

		}
	}

	public static InputStream getResourceAsStream(Class<?> claz, String name) {

		/**
		 * remove leading slash so path will work with classes in a JAR file
		 */
		while (name.startsWith("/")) {
			name = name.substring(1);
		}

		ClassLoader classLoader = claz.getClassLoader();

		return classLoader.getResourceAsStream(name);

	}

	@Todo("Add support for multi-depth generic hierarchies")
	/**
	 * This method does not support multi-depth generic hierarchies
	 * */
	public static List<Class<?>> getGenericSuperclasses(Class<?> clazz) {

		List<Class<?>> result = Lists.newArrayList();

		String typeName = clazz.getGenericSuperclass().getTypeName();

		Matcher m = GENERIC_TYPE_PATTERN.matcher(typeName);

		boolean b = m.find();

		if (b) {
			String classes = m.group();

			for (String o : classes.split(", ")) {
				try {
					result.add(clazz.getClassLoader().loadClass(o));
				} catch (ClassNotFoundException e) {
					Exceptions.throwRuntime(e);
				}
			}
		}
		return result;
	}

	public static List<Field> getInheritedFields(Class<?> clazz, Class<?> abstractParent) {
		return getInheritedFields0(clazz.getSuperclass(), abstractParent, null);
	}
	
	public static List<Field> getInheritedFields(Class<?> clazz, Class<?> abstractParent, Consumer<Field> fieldConsumer) {
		return getInheritedFields0(clazz.getSuperclass(), abstractParent, fieldConsumer);
	}

	private static List<Field> getInheritedFields0(Class<?> clazz, Class<?> abstractParent, Consumer<Field> fieldConsumer) {

		assert abstractParent.isAssignableFrom(clazz);

		List<Field> result = Lists.newArrayList();

		while (!clazz.getName().equals(abstractParent.getName()) && (clazz.getSuperclass() != null)) {

			for (Field f : clazz.getDeclaredFields()) {
				
				fieldConsumer.accept(f);
				
				int mod = f.getModifiers();
				if (Modifier.isProtected(mod) && (!Modifier.isFinal(mod))) {
					result.add(f);
				}
			}

			clazz = clazz.getSuperclass();
		}

		return result;
	}
	
	@SuppressWarnings("unused")
	private static Class<?> getSuperClass(Class<?> clazz) {

		while (clazz.getSuperclass() != null && !(clazz.getSuperclass().equals(Object.class))) {
			clazz = clazz.getSuperclass();
		}

		return clazz;
	}

}
