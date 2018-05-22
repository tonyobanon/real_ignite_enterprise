package com.re.paas.internal.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.re.paas.internal.app_provisioning.AppProvisioning;
import com.re.paas.internal.base.classes.KeyValuePair;
import com.re.paas.internal.base.core.BlockerBlockerTodo;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.utils.ClassUtils;

/**
 * Note: T must be non-parameterized
 */
public abstract class SpiDelegate<T> {

	static Map<SpiTypes, SpiDelegate<?>> spiDelegates = Collections
			.synchronizedMap(new LinkedHashMap<SpiTypes, SpiDelegate<?>>());

	private KeyValuePair<SpiTypes, Class<?>> type = null;

	static Map<SpiTypes, Map<Object, Object>> spiResources = Collections
			.synchronizedMap(new HashMap<SpiTypes, Map<Object, Object>>());

	
	protected static final String DEFAULT_NAMESPACE = "default";

	public static SpiDelegate<?> getDelegate(SpiTypes type) {
		return spiDelegates.get(type);
	}

	public final void set(Object namespace, Object obj) {
		SpiDelegate.spiResources.get(type.getKey()).put(namespace, obj);
	}

	public Map<Object, Object> getAll() {
		return spiResources.get(type.getKey());
	}
	
	protected static Map<Object, Object> getAll(SpiTypes type) {
		return spiResources.get(type);
	}
	
	@SuppressWarnings("unchecked")
	public Object get(String namespace) {
		Object o = SpiDelegate.spiResources.get(type.getKey()).get(namespace);
		if (o instanceof Class) {
			return ClassUtils.createInstance((Class<T>) o);
		} else {
			return o;
		}
	}

	public Object get() {
		return get(DEFAULT_NAMESPACE);
	}
	
	public Object remove(String namespace) {
		return spiResources.get(type.getKey()).remove(namespace);
	}

	/**
	 * This reads the generic types declared for this class, and registers the
	 * corresponding SpiType
	 */
	public SpiDelegate() {

		Class<?> c = ClassUtils.getGenericSuperclasses(getClass()).get(0);

		// Get the spiType that c represents

		for (Entry<SpiTypes, BaseSPILocator> e : BaseSPILocator.defaultSpiLocator.entrySet()) {
			if (c.isAssignableFrom(e.getValue().classType())) {
				type = new KeyValuePair<SpiTypes, Class<?>>(e.getKey(), c);
				break;
			}
		}

		if (type == null) {
			Exceptions.throwRuntime("The specified generic type do not conform to any SpiType");
		}
	}

	static final void start(String appId) {
		start(appId, SpiTypes.values());
	}
	
	static final void start(SpiTypes[] types) {
		start(null, types);
	}

	static final void start(String appId, SpiTypes[] types) {

		for (SpiTypes type : types) {

			String key = "platform.spi." + type.toString().toLowerCase() + ".delegate";
			KeyValuePair<String, ClassLoader> config = AppProvisioning.getConfiguration(appId, key);

			SpiDelegate<?> delegate = spiDelegates.get(type);

			boolean newDelegate = delegate == null || !config.getValue().equals(delegate.getClass().getClassLoader());

			if (newDelegate) {
				
				Logger.get().info("Starting new SPI Delegate " + appId != null ? "for " + appId : "" + " " + types.toString());

				Class<? extends SpiDelegate<?>> delegateClass = ClassUtils.forName(config.getKey(), config.getValue());

				delegate = ClassUtils.createInstance(delegateClass);

				if (!delegate.getSpiType().equals(type)) {
					Exceptions.throwRuntime(
							"Class: " + delegateClass.getName() + " is not a delegate for SpiType: " + type.name());
				}

				if (spiDelegates.containsKey(type)) {
					spiDelegates.remove(type).destroy();
				}

				spiDelegates.put(type, delegate);

				if (spiResources.containsKey(type)) {
					spiResources.get(type).clear();
				}
				spiResources.put(type, Maps.newHashMap());

				delegate.init();

			} else {

				Logger.get().info("Using existing SPI Delegate " + appId != null ? "for " + appId : "" + " " + types.toString());

				List<Class<?>> classes = BaseSPILocator.spiClasses.get(type).get(appId);

				if (classes != null && !classes.isEmpty()) {
					delegate.add0(classes);
				}
			}
		}
	}

	private final SpiTypes getSpiType() {
		return type.getKey();
	}

	@SuppressWarnings("unchecked")
	protected final void get(Consumer<Class<T>> consumer) {
		get(getSpiType(), c -> {
			consumer.accept((Class<T>) c);
		});
	}

	@BlockerBlockerTodo("Ensure that the SPIs are tranversed in the correct order")
	private static final void get(SpiTypes type, Consumer<Class<?>> consumer) {

		loop: for (List<Class<?>> l : BaseSPILocator.spiClasses.get(type).values()) {
			for (Class<?> c : l) {
				consumer.accept(c);
				if (type.getCount() != -1 && SpiDelegate.spiResources.get(type).size() == type.getCount()) {
					Logger.get().info("All resources has been added successfully for SpiType: " + type.name() + " ..");
					break loop;
				}
			}
		}

		// Validate resource count
		Map<Object, Object> o = SpiDelegate.spiResources.get(type);

		if (type.getCount() != -1 && o.size() > type.getCount()) {
			Exceptions.throwRuntime("SpiType: " + type.name() + " should have " + type.getCount() + " resource(s)");
		}

		if (o.isEmpty()) {
			Logger.get().warn("No resources were registered for the SpiType: " + type.name());
		}

	}

	/**
	 * This method is used to initialize the needed resources by this delegate
	 */
	protected abstract void init();

	/**
	 * This releases the resources used by this delegate, and performs any cleanup
	 * tasks
	 */
	protected void destroy() {

	}

	@SuppressWarnings("unchecked")
	private final List<Class<T>> getClasses(List<Class<?>> classes) {
		List<Class<T>> o = Lists.newArrayList();
		classes.forEach(c -> {
			o.add((Class<T>) c);
		});
		return o;
	}

	final void add0(List<Class<?>> classes) {
		add(getClasses(classes));
	}

	final void remove0(List<Class<?>> classes) {
		remove(getClasses(classes));
	}

	protected void add(List<Class<T>> classes) {
	}

	protected void remove(List<Class<T>> classes) {
	}

}
