package com.re.paas.internal.clustering.datagrid;


public class ArgValidator {

	public static String validateString(String arg, String errorMsg) throws RuntimeException {
		if (arg == null || arg.equals("")) {
			throw new RuntimeException(errorMsg);
		} else {
			return arg;
		}
	}

}
