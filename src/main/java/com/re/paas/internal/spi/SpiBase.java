package com.re.paas.internal.spi;

import java.util.List;

import com.re.paas.internal.app_provisioning.AppClassLoader;
import com.re.paas.internal.app_provisioning.AppProvisioning;
import com.re.paas.internal.base.core.PlatformInternal;
import com.re.paas.internal.base.logging.Logger;

public class SpiBase {

	public static void start() {
		start(null);
	}

	public static void start(String appId) {
		BaseSPILocator.start(appId);
		SpiDelegate.start(appId);
	}

	public static void stop(String appId) {
		Logger.get().info("Stopping application: " + appId);
		stop0(appId);
	}

	@PlatformInternal
	private static void stop0(String appId) {

		BaseSPILocator.spiClasses.forEach((type, apps) -> {

			AppClassLoader cl = AppProvisioning.getClassloader(appId);
			
			assert cl.isStopping();
			
			List<Class<?>> classes = apps.remove(appId);

			if (classes == null || classes.isEmpty()) {
				return;
			}
			
			// Get the corresponding delegate
			
			SpiDelegate<?> delegate = SpiDelegate.spiDelegates.get(type);
			delegate.remove0(classes);

			
			if(BaseSPILocator.defaultSpiLocator.get(type).getClass().getClassLoader().equals(cl)) {
				// Start new locator
				BaseSPILocator.start(new SpiTypes[] {type});
			}
			
			if(delegate.getClass().getClassLoader().equals(cl)) {
				// Start new delegate
				SpiDelegate.start(new SpiTypes[] {type});
			}
		});
		

	}

}
