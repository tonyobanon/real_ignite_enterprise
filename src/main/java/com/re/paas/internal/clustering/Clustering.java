package com.re.paas.internal.clustering;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.re.paas.internal.base.AppDelegate;
import com.re.paas.internal.base.core.DefaultLogger;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.clustering.classes.ClusterConfig;
import com.re.paas.internal.clustering.classes.NodeConfig;
import com.re.paas.internal.clustering.classes.Utils;

public class Clustering {

	// 24KB
	public static final Integer MAX_TOTAL_MESSAGE_SIZE = 240000;

	public static final Integer NODE_COMMUNICATION_TIMEOUT = 5000;

	public static String APP_BASE = null;

	public static final String CLUSTER_CONFIG = "application_config\\cluster-config.properties";
	public static final String MASTER_NODE_CONFIG = "application_config\\master-node-config.properties";
	public static final String NODE_CONFIG = "application_config\\node-config.properties";


	private static String getTempFile() {

		String tempFile = System.getProperty("java.io.tmpdir") + AppDelegate.getPlatformPrefix() + "~"
				+ CloudEnvironment.get().clusteringHost().getHostAddress().replace(".", "-") + ".tmp";

		return tempFile;
	}

	public static void start() {

		if (!ClusterConfig.isLoaded()) {

			Logger.get().info("Reading application base directory");
			APP_BASE = Utils.getAppBaseDir();

			// Load Configurations
			ClusterConfig.load(APP_BASE + CLUSTER_CONFIG);
			NodeConfig.load(APP_BASE + NODE_CONFIG);
		}

		if (Files.exists(Paths.get(getTempFile()))) {
			//Exceptions.throwRuntime(PlatformException.get(ClusteringError.CLUSTER_NODE_ALREADY_RUNNING_ON_MACHINE));
		}

		Logger.get().info("Starting node clustering sequence..");

		
		// Start Cluster Services
		ClusterNode.start();
		
		
		// Create temporary file
		try {
			File temp = new File(getTempFile());
			temp.deleteOnExit();
			temp.createNewFile();
		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}
	}

	public static void stop() {

		// Delete Temp File: (Irrespective that temp file set to deleteOnExit)
		try {
			Files.deleteIfExists(Paths.get(getTempFile()));
		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}

		// Stop Node
		ClusterNode.stop();
	}

}
