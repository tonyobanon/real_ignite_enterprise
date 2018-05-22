package com.re.paas.internal.clustering.classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Maps;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.clustering.Clustering;
import com.re.paas.internal.errors.ClusteringError;

public class MasterNodeConfig {

	public static final String DATA_GRID_PARTITION_SIZE = "datagrid.partitionSize";
	public static final String DATA_GRID_BASE_ITEM_SIZE = "datagrid.baseItemSize";

	public static final String AUTO_SCALING_FACTORS = "autoScalingFactors";
	public static final String AUTO_SCALING_BASELINE = "autoScalingBaseline";

	public static final String REGION = "region";

	public static final String PRIVATE_SUBNET_ID = "privateSubnetId";

	public static final String AZURE_SUBSCRIPTION = "-azure-subscription";
	public static final String AZURE_CLIENT = "-azure-client";
	public static final String AZURE_KEY = "-azure-key";
	public static final String AZURE_MANAGEMENT_URI = "-azure-managementURI";
	public static final String AZURE_BASE_URL = "-azure-baseURL";
	public static final String AZURE_AUTH_URL = "-azure-authURL";
	public static final String AZURE_GRAPH_URL = "-azure-graphURL";
	public static final String AZURE_RESOURCE_GROUP = "-azure-resourceGroup";
	public static final String AZURE_VIRTUAL_MACHINE_SIZE_TYPE = "-azure-virtualMachineSizeType";

	public static final String AWS_ACCESS_KEY = "-aws-accessKey";
	public static final String AWS_SECRET_KEY = "-aws-secretKey";
	public static final String AWS_INSTANCE_TYPE = "-aws-instanceType";

	private static String filePath;
	private static Map<String, String> properties = Maps.newConcurrentMap();

	public static void load(String filePath) {

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

		MasterNodeConfig.filePath = filePath;
	}

	public static String get(String property) {
		String v = properties.get(property);
		if ((v != null) && (v != "")) {
			return v;
		} else {

			return (String) Exceptions.throwRuntime(PlatformException.get(ClusteringError.EMPTY_PARAMETER,
					Clustering.MASTER_NODE_CONFIG + "/" + property));
		}
	}

	public static Integer getInteger(String property) {
		Integer val = null;
		try {
			val = Integer.parseInt(get(property));
		} catch (NumberFormatException e) {
			return (Integer) Exceptions.throwRuntime(PlatformException.get(ClusteringError.INVALID_PARAMETER,
					Clustering.MASTER_NODE_CONFIG + "/" + property));
		}
		return val;
	}

	public static String getOrNull(String property) {
		String v = properties.get(property);
		if ((v != null) && (!v.equals(""))) {
			return v;
		} else {
			return null;
		}
	}

	public static String getFilePath() {
		return filePath;
	}
}
