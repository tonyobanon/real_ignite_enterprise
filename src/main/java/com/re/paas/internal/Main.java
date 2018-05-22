package com.re.paas.internal;

import com.re.paas.internal.base.AppDelegate;
import com.re.paas.internal.utils.Utils;

public class Main {

	private static boolean isStarted = false;

	public static void main(String[] args) {

		if (isStarted) {
			return;
		}

		String safeMode = Utils.getArgument(args, "safemode");

		AppDelegate.main(safeMode != null && (safeMode.equals("true") || safeMode.equals("1")));

		isStarted = true;
	}

	public static boolean isStarted() {
		return isStarted;
	}

	public static void stop() {
		AppDelegate.stop();
		isStarted = false;
	}
}
