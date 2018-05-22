package com.re.paas.internal.clustering.datagrid;

import java.io.File;
import java.util.List;

import com.google.common.base.Splitter;
import com.re.paas.internal.clustering.classes.Patterns;
import com.re.paas.internal.clustering.classes.Utils;

public class Caches {

	public static final String coherenceCacheFile = System.getProperty("user.home") + File.separator
			+ "coherence-cache-config.xml";

	public static String[] defaultCaches() {
		String[] defaultCaches = { "PartitionedCache" };
		return defaultCaches;
	}

	public static List<String> toList(String cacheNames) {
		if (cacheNames != null) {
			List<String> cacheList = Splitter.on(Patterns.commaSeperator()).splitToList(cacheNames);
			for (String cacheName : cacheList) {
				if (!Patterns.fullAlphaNumeric().matcher(cacheName).matches()) {
					throw new RuntimeException("The cacheName: " + cacheName + " is invalid.");
				}
			}
			return cacheList;
		} else {
			return null;
		}
	}

	public static List<String> readArgs(String[] args) {
		String caches = Utils.getArgument(args, "caches");
		if (caches != null) {
			return toList(caches);
		}
		throw new RuntimeException("no -cache parameter was passed as argument");
	}

}
