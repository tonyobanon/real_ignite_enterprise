package com.re.paas.internal.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.re.paas.internal.app_provisioning.AppClassLoader;
import com.re.paas.internal.app_provisioning.AppProvisioning;
import com.re.paas.internal.base.classes.KeyValuePair;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.errors.ApplicationError;
import com.re.paas.internal.utils.ClassUtils;

public abstract class BaseSPILocator {

	static Map<SpiTypes, BaseSPILocator> defaultSpiLocator = Collections
			.synchronizedMap(new HashMap<SpiTypes, BaseSPILocator>());

	static Map<SpiTypes, Map<String, List<Class<?>>>> spiClasses = Collections
			.synchronizedMap(new HashMap<SpiTypes, Map<String, List<Class<?>>>>());

	
	public static void start(String appId) {
		start(appId, SpiTypes.values());
	}
	
	public static void start(SpiTypes[] types) {
		start(null, types);
	}
	
	public static void start(String appId, SpiTypes[] types) {

		for (SpiTypes type : types) {

			BaseSPILocator spi = defaultSpiLocator.get(type);
			
			String key = "platform.spi." + type.toString().toLowerCase() + ".locator";
			KeyValuePair<String, ClassLoader> config = AppProvisioning.getConfiguration(appId, key);
			
			boolean newLocator = spi == null || !config.getValue().equals(spi.getClass().getClassLoader());

			if (newLocator) {
				
				Logger.get().info("Starting new SPI Locator " + appId != null ? "for " + appId : "" + " " + types.toString());
				
				Class<? extends BaseSPILocator> locatorClass = ClassUtils.forName(config.getKey(), config.getValue());

				spi = ClassUtils.createInstance(locatorClass);

				// Verify that declared spi type corresponds to type
				if (!(spi.spiType() == type)) {
					Exceptions.throwRuntime(PlatformException
							.get(ApplicationError.SERVICE_PROVIDER_CLASS_NOT_CONCRETE_IMPL, locatorClass.getName()));
				}

				// Verify that no other spi locator uses classType
				for (Entry<SpiTypes, BaseSPILocator> e : BaseSPILocator.defaultSpiLocator.entrySet()) {
					if (spi.classType().isAssignableFrom(e.getValue().classType())) {
						Exceptions
								.throwRuntime("Type: " + spi.classType().getName() + " is already defined for SpiType: "
										+ e.getKey() + " --> " + e.getValue().classType());
					}
				}

				if (appId == null) {
					spiClasses.put(type, Maps.newHashMap());
				}

				Logger.get()
						.info("Setting " + locatorClass.getName() + " as the default SPI Locator for type: " + type.name());

				defaultSpiLocator.put(type, spi);

			} else {
				
				Logger.get().info("Using existing SPI Locator " + appId != null ? "for " + appId : "" + "..");
				
				spi = defaultSpiLocator.get(type);
			}

			addClasses(newLocator, spi, appId);
		}
	}

	private static void addClasses(boolean newLocator, BaseSPILocator spi, String appId) {

		Logger.get().info("Scanning SPI classes for type: " + spi.spiType());

		if (newLocator) {

			spiClasses.get(spi.spiType()).put(AppProvisioning.DEFAULT_APP_ID, scanClasses(spi, null));

			AppProvisioning.listApps().forEach(x -> {

				List<Class<?>> classes = scanClasses(spi, AppProvisioning.getClassloader(x));

				if (!classes.isEmpty()) {
					spiClasses.get(spi.spiType()).put(x, classes);
				}
			});

		} else {

			List<Class<?>> classes = scanClasses(spi, AppProvisioning.getClassloader(appId));
			if (!classes.isEmpty()) {
				spiClasses.get(spi.spiType()).put(appId, classes);
			}
		}
	}

	private static List<Class<?>> scanClasses(BaseSPILocator spi, AppClassLoader cl) {

		List<Class<?>> classes = Lists.newArrayList();

		ClasspathScanner<?> cs = new ClasspathScanner<>(spi.classSuffix(), spi.classType(), spi.classIdentity())
				.setMaxCount(spi.spiType().getCount()).setClassLoader(cl);

		cs.scanClasses().forEach(c -> {
			classes.add(c);
		});

		return classes;
	}

	abstract SpiTypes spiType();

	public abstract Iterable<String> classSuffix();

	ClassIdentityType classIdentity() {
		return ClassIdentityType.ASSIGNABLE_FROM;
	}

	abstract Class<?> classType();

}
