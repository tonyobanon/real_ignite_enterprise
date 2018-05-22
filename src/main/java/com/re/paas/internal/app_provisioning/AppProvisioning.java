package com.re.paas.internal.app_provisioning;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.re.paas.internal.base.AppDelegate;
import com.re.paas.internal.base.classes.KeyValuePair;
import com.re.paas.internal.base.core.AppUtils;
import com.re.paas.internal.base.core.BlockerBlockerTodo;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.spi.SpiBase;
import com.re.paas.internal.utils.Utils;

public class AppProvisioning {

	private static final String APP_DEPENDENCIES_CONFIG_KEY = "app_dependencies";
	
	public static final String DEFAULT_APP_ID = "default";

	private static Map<String, AppClassLoader> appClassloaders = Collections
			.synchronizedMap(new HashMap<String, AppClassLoader>());

	static Map<String, JsonObject> appConfig = Collections.synchronizedMap(new HashMap<String, JsonObject>());

	private static final Path BASE_APPS_PATH = Paths.get(System.getProperty("java.io.tmpdir") + File.separator
			+ AppDelegate.getPlatformPrefix() + File.separator + "apps" + File.separator);

	private static Path getAppBasePath() {
		return BASE_APPS_PATH;
	}

	public static void install(String uri) {

		// Fetch bundle

		// Save to DB, along with version

		// Extract to app directory

		
		// Get app Id
		String app = null;

		SpiBase.start(app);
	}

	public static void list() {

	}
	
	private static String[] getAppDependencies(String appId) {
		JsonArray arr = AppProvisioning.appConfig.get(appId).get(APP_DEPENDENCIES_CONFIG_KEY).getAsJsonArray();
		String[] array = new String[arr.size()];
		for(int i = 0; i < arr.size(); i++) {
			array[i] = arr.get(i).getAsString();
		}
		return array;
	}

	public static void start() {
		try {
			start0();
		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}
	}

	@BlockerBlockerTodo("Sort that date. Therefore least recent date comes in first, Attend to comments")
	public static void start0() throws IOException {

		Path appBase = getAppBasePath();

		if (!Files.exists(appBase)) {
			Files.createDirectories(appBase);
		}

		// Get bundles from DB,

		// And extract them to appBase
		

		Stream<Path> paths = Files.list(appBase);
		
		// Call getAppDependencies(..) and ensure that all dependencies are available
		
		paths.forEach(path -> {

			String appId = path.getFileName().toString();

			JsonObject config = null;
			try {
				config = Utils.getJson(Files.newInputStream(path.resolve("config.json")));
			} catch (Exception e) {
				throw new RuntimeException("Error occured while loading config.json", e);
			}

			appConfig.put(appId, config);			

			AppClassLoader cl = AppClassLoader.get(path, appId, getAppDependencies(appId));

			// Create classloader
			appClassloaders.put(appId, cl);
		});
		
		paths.close();
	}

	public static void stop(String app) {
		
		assert !app.equals(DEFAULT_APP_ID);
		
		AppClassLoader cl = appClassloaders.get(app);
		@SuppressWarnings("unused")
		JsonObject obj = appConfig.get(app);
		
		cl.setStopping(true);
		SpiBase.stop(app);
		
		cl = null;
		obj = null;
		
		appClassloaders.remove(app);
		appConfig.remove(app);
		
		//Schedule Application Restart
	}

	public static Collection<String> listApps() {
		return appClassloaders.keySet();
	}

	public static ClassLoader getClassloader() {
		return getClassloader((String)null);
	}
	
	public static AppClassLoader getClassloader(String appId) {
		return appClassloaders.get(appId);
	}

	public static String getAppId(Class<?> clazz) {
		ClassLoader cl = clazz.getClassLoader();

		if (cl instanceof AppClassLoader) {
			return ((AppClassLoader) cl).getAppId();
		} else {
			return DEFAULT_APP_ID;
		}
	}

	public static JsonObject getConfig(String appId) {
		return appConfig.get(appId);
	}

	private static Map<JsonObject, ClassLoader> getConfigurations(String appId) {

		Map<JsonObject, ClassLoader> m = Maps.newHashMap();

		m.put(AppUtils.getConfig(), getClassloader());

		if (appId == null) {

			for (String x : listApps()) {

				AppClassLoader cl = getClassloader(x);

				if (!cl.isStopping()) {
					m.put(getConfig(x), cl);
				}
			}

		} else {
			AppClassLoader cl = getClassloader(appId);

			if (!cl.isStopping()) {
				m.put(getConfig(appId), cl);
			}
		}

		return m;
	}

	public static KeyValuePair<String, ClassLoader> getConfiguration(String appId, String key) {

		Map<JsonObject, ClassLoader> configuration = AppProvisioning.getConfigurations(appId);

		KeyValuePair<String, ClassLoader> r = null;

		for (Entry<JsonObject, ClassLoader> e : configuration.entrySet()) {

			JsonObject config = e.getKey();
			ClassLoader cl = e.getValue();

			JsonElement v = config.get(key);

			if (v != null) {
				r = new KeyValuePair<String, ClassLoader>(v.getAsString(), cl);
			}
		}

		return r;
	}

	public static boolean isInternalClass(Class<?> clazz) {
		return isInternalClass(clazz.getName());
	}

	public static boolean isInternalClass(String clazz) {
		return clazz.startsWith("com.ce.saas");
	}

}
