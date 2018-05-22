package com.re.paas.internal.clustering.classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.re.paas.internal.base.core.DefaultLogger;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.clustering.Clustering;
import com.re.paas.internal.errors.ClusteringError;

public class ClusterConfig {
	
	protected static boolean isLoaded;

	public static Pattern CLUSTER_NAME_PATTERN = Pattern.compile("\\A([a-zA-Z]+[-]*[a-zA-Z]+)+\\z");
	public static final String CLUSTER_NAME = "clusterName";

	public static final String HTTP_PORT = "httpPort";
	
	public static final String CLUSTERING_PORT = "clusteringPort";

	public static final String SIGNATURE = "signature";

	private static String filePath;
	private static Map<String, String> properties = Maps.newConcurrentMap();

	public static void load(String filePath) {

		Logger.get().info("Reading " + filePath);
		
		InputStream in = null;
		try {
			in = new FileInputStream(new File(filePath));
		} catch (FileNotFoundException e) {
			Exceptions.throwRuntime(e);
		}

		Properties o = new Properties();
		try {
			o.load(in);
		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}
		
		o.forEach((k, v) -> {
			properties.put((String) k, ((String) v).trim());
		});

		ClusterConfig.filePath = filePath;
		ClusterConfig.isLoaded = true;
	}

	public static String get(String property) {
		String v = properties.get(property);
		if ((v != null) && (v != "")) {
			return v;
		} else {
			
			return (String) Exceptions.throwRuntime(
					PlatformException.get(ClusteringError.EMPTY_PARAMETER, Clustering.CLUSTER_CONFIG + "/" + property));			
		}
	}
	
	public static Integer getInteger(String property) {
		Integer val = null;
		try {
			val = Integer.parseInt(get(property));
		} catch (NumberFormatException e) {
			
			return (Integer) Exceptions.throwRuntime(
					PlatformException.get(ClusteringError.INVALID_PARAMETER, Clustering.CLUSTER_CONFIG + "/" + property));			
		}
		return val;
	}

	public static String getOrNull(String property) {
		String v = properties.get(property);
		if ((v != null) && (v != "")) {
			return v;
		} else {
			return null;
		}
	}

	public static String getFilePath() {
		return filePath;
	}
	
	public static boolean isLoaded() {
		return isLoaded;
	}

	static {

	}

}
