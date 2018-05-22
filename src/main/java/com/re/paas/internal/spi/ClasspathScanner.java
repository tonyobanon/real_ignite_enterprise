package com.re.paas.internal.spi;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.re.paas.internal.app_provisioning.AppClassLoader;
import com.re.paas.internal.base.core.AppUtils;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.utils.Utils;

public class ClasspathScanner<T> {

	private AppClassLoader classLoader;

	private final String fileExtension;
	private final Iterable<String> nameSuffixes;
	private final ClassIdentityType classIdentityType;
	private final Class<T> classType;

	private int maxCount = -1;

	/**
	 * This constructor should for XML and JSON artifacts
	 */
	public ClasspathScanner(Iterable<String> nameSuffixes, String fileExtension, Class<T> type) {

		this.nameSuffixes = nameSuffixes;
		this.fileExtension = fileExtension;

		this.classIdentityType = null;
		this.classType = type;

	}

	/**
	 * This constructor should be used for classes
	 */
	public ClasspathScanner(Iterable<String> nameSuffixes, Class<T> type, ClassIdentityType identityType) {

		this.nameSuffixes = nameSuffixes;
		this.fileExtension = "class";

		this.classIdentityType = identityType;
		this.classType = type;
	}

	/**
	 * This constructor should be used for classes
	 */
	public ClasspathScanner(Class<T> type, ClassIdentityType identityType) {
		this(null, type, identityType);
	}

	private static boolean isExtensionSiupported(String ext) {
		return /* ext.equals("json") || */ext.equals("xml");
	}

	public List<T> scanArtifacts() {

		if (!isExtensionSiupported(fileExtension)) {
			Exceptions.throwRuntime(new RuntimeException("The specified file extension is not supported"));
		}

		if (classType == null) {
			return new ArrayList<>();
		}

		final List<T> classes = new ArrayList<T>();

		// Scan classpath

		try {

			Path basePath = classLoader != null ? classLoader.getPath() : AppUtils.getBasePath();

			Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

					String fullPath = file.toString();

					boolean hasNameSuffix = false;

					for (String nameSuffix : nameSuffixes) {
						if (fullPath.endsWith(nameSuffix + "." + fileExtension)) {
							hasNameSuffix = true;
						}
					}

					if (!hasNameSuffix) {
						return FileVisitResult.CONTINUE;
					}

					try {

						ObjectMapper xmlMapper = new XmlMapper();
						T o = xmlMapper.readValue(Utils.getString(fullPath), classType);
						classes.add(o);

					} finally {
						// Do nothing
					}

					if (maxCount != -1 && maxCount >= classes.size()) {
						return FileVisitResult.TERMINATE;
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path file, IOException e) throws IOException {
					if (e == null) {
						return FileVisitResult.CONTINUE;
					} else {
						// directory iteration failed
						throw e;
					}
				}
			});

		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}

		return classes;

	}

	public List<Class<? extends T>> scanClasses() {

		if (classIdentityType == null || classType == null) {
			return new ArrayList<>();
		}

		final List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();

		// Scan classpath

		try {

			ClassLoader cl = classLoader != null ? classLoader : AppUtils.getBaseClassloader();
			Path basePath = classLoader != null ? classLoader.getPath() : AppUtils.getBasePath();

			Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
				@SuppressWarnings("unchecked")
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

					String fullyQualifiedName = basePath.relativize(file).toString();

					boolean hasNameSuffix = false;

					for (String nameSuffix : nameSuffixes) {
						if (fullyQualifiedName.endsWith(nameSuffix + "." + fileExtension)) {
							hasNameSuffix = true;
						}
					}

					if (!hasNameSuffix) {
						return FileVisitResult.CONTINUE;
					}

					String className = fullyQualifiedName.replaceAll(Pattern.quote(File.separator), ".")
							.replaceAll(".class\\z", "");

					try {

						Class<?> model = Class.forName(className, true, cl);

						if (Modifier.isAbstract(model.getModifiers()) || Modifier.isInterface(model.getModifiers())) {
							return FileVisitResult.CONTINUE;
						}

						if (model.getSuperclass() != null && model.getSuperclass().equals(Enum.class)) {
							return FileVisitResult.CONTINUE;
						}
						
						boolean hasNoArgConstructor = false;
						
						for(Constructor<?> constr : model.getConstructors()) {
							if(constr.getParameterCount() == 0) {
								hasNoArgConstructor = true;
								break;
							}
						}
						
						if(!hasNoArgConstructor) {
							Logger.get().warn("Class: " + model.getName() + " has no no-arg constructor and will be skipped ..");
							return FileVisitResult.CONTINUE;
						}
						

						switch (classIdentityType) {
						case ANNOTATION:
							if (model.isAnnotationPresent((Class<? extends Annotation>) classType)) {
								classes.add((Class<? extends T>) model);
							}
							break;
						case ASSIGNABLE_FROM:
							if (classType.isAssignableFrom(model) && !model.getName().equals(classType.getName())) {
								classes.add((Class<? extends T>) model);
							}
							break;
						case DIRECT_SUPER_CLASS:

							if (model.getSuperclass() != null && model.getSuperclass().equals(classType)
									&& !model.getName().equals(classType.getName())) {
								classes.add((Class<? extends T>) model);
							}
							break;
						}

					} catch (ClassNotFoundException e) {
						Logger.get().error(e.getMessage());
					}

					if (maxCount != -1 && maxCount >= classes.size()) {
						return FileVisitResult.TERMINATE;
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path file, IOException e) throws IOException {
					if (e == null) {
						return FileVisitResult.CONTINUE;
					} else {
						// directory iteration failed
						throw e;
					}
				}
			});

		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}

		return classes;

	}

	public int getMaxCount() {
		return maxCount;
	}

	public ClasspathScanner<T> setMaxCount(int maxCount) {
		this.maxCount = maxCount;
		return this;
	}

	public AppClassLoader getClassLoader() {
		return classLoader;
	}

	public ClasspathScanner<T> setClassLoader(AppClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}

}
