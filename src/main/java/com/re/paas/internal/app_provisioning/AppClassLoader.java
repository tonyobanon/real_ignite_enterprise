package com.re.paas.internal.app_provisioning;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.google.common.collect.Maps;
import com.re.paas.internal.base.core.BlockerBlockerTodo;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.JdkUpgradeTask;

public class AppClassLoader extends ClassLoader {

	private static final Map<String, Class<?>> loadedClasses = Maps.newHashMap();

	private final String appId;
	private final Path path;
	private boolean isStopping = false;

	private final String[] appDependencies;

	private AppClassLoader(ClassLoader parent, Path path, String appId) {
		this(parent, path, appId, null);
	}
	
	private AppClassLoader(ClassLoader parent, Path path, String appId, String[] appDependencies) {
		super(parent);
		this.appId = appId;
		this.path = path;
		this.appDependencies = appDependencies;
	}

	public static AppClassLoader get(Path path, String appId, String[] appDependencies) {
		return new AppClassLoader(AppClassLoader.class.getClassLoader(), path, appId, appDependencies);
	}

	public URL getResource(String name) {
		URL url = this.findResource(name);
		if (url == null) {
			url = super.getResource(name);
		}
		return url;
	}

	@Override
	protected URL findResource(String name) {
		try {
			return path.resolve(name).toUri().toURL();
		} catch (MalformedURLException e) {
			return (URL) Exceptions.throwRuntime(e);
		}
	}

	@JdkUpgradeTask("Verify that Jdk9 still throws a ClassNotFoundException, which is almost useless")
	@Override
	public Class<?> loadClass(String name, boolean resolve) {

		synchronized (getClassLoadingLock(name)) {

			Class<?> c = null;

			// First, Delegate to parent classloader
			try {
				c = super.loadClass(name, false);
				return c;

			} catch (ClassNotFoundException e) {
			}

			// Then, check if the class has already been loaded by the current classloader
			c = findLoadedClass(name);

			if (c != null) {
				return c;
			}

			
			c = loadedClasses.get(name);
			if (c != null) {
				return c;
			}
			

			// Find class file using this classloader and define in Metaspace
			byte[] data = loadClassData(name);
			if (data != null) {
				c = defineClass(name, data, 0, data.length);
				return c;
			}

			// Then, Check classloader of app dependencies

			for (int i = 0; i < appDependencies.length; i++) {
				try {
					c = AppProvisioning.getClassloader(appDependencies[i]).loadClass(name);
					break;
				} catch (ClassNotFoundException e1) {
				}
			}

			if (c != null) {
				loadedClasses.put(name, c);
				return c;
			}

			Exceptions.throwRuntime(new ClassNotFoundException(name));
			return null;
		}

	}

	@BlockerBlockerTodo("Add support for nested classes, as this will not work in that scenario")
	private byte[] loadClassData(String className) {

		try {

			// read class
			InputStream is = Files.newInputStream(path.resolve(className.replace(".", "/") + ".class"));
			ByteArrayOutputStream byteSt = new ByteArrayOutputStream();
			// write into byte
			int len = 0;
			while ((len = is.read()) != -1) {
				byteSt.write(len);
			}
			// convert into byte array
			return byteSt.toByteArray();

		} catch (IOException e) {

			if (!(e instanceof FileNotFoundException)) {
				Exceptions.throwRuntime(e);
			}

			return null;
		}
	}

	public Path getPath() {
		return path;
	}

	public String getAppId() {
		return appId;
	}

	public boolean isStopping() {
		return isStopping;
	}

	AppClassLoader setStopping(boolean isStopping) {
		this.isStopping = isStopping;
		return this;
	}

	static {
		registerAsParallelCapable();
	}

}
