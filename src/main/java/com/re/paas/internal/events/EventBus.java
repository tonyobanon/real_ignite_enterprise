package com.re.paas.internal.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.core.cron.Scheduler;
import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.spi.SpiTypes;
import com.re.paas.internal.utils.ClassUtils;

public class EventBus {

	private static Map<String, List<BiConsumer<Boolean, BaseEvent>>> listeners = Maps.newHashMap();

	public static void start() {

		// Scan for Event Listeners
		new SpiDelegate<>(SpiTypes.EVENT_LISTENER).get(c -> {
					
					Object o = ClassUtils.createInstance(c);

					for (Method m : c.getMethods()) {

						Subscribe s = m.getAnnotation(Subscribe.class);

						if (s == null) {
							return;
						}
						
						if (BaseEvent.class.isAssignableFrom(m.getParameterTypes()[0])
								&& !(m.getParameterTypes()[0]).getName().equals(BaseEvent.class.getName())) {
							
							String eventClass = (m.getParameterTypes()[0]).getName();

							if(!listeners.containsKey(eventClass)) {
								listeners.put(eventClass, Lists.newArrayList());
							}
							
							listeners.get(eventClass).add((isAsync, evt) -> {

								if (isAsync && !s.allowAsyncEvent()) {
									Exceptions.throwRuntime(PlatformException.get(EventError.ASYNC_EVENTS_ARE_DISABLED,
											m.getName(), c.getName()));
								}

								try {
									m.invoke(o, evt);
								} catch (IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e) {
									Exceptions.throwRuntime(e);
								}
							});
						}
					}
				});

	}

	public static void dispatch(BaseEvent evt) {
		dispatch(evt, true);
	}

	public static void dispatch(BaseEvent evt, boolean isAsync) {

		if (isAsync) {

			Scheduler.now(() -> {
					listeners.get(evt.getClass().getName()).forEach(o -> {
						o.accept(isAsync, evt);
					});
			});

		} else {
			listeners.get(evt.getClass().getName()).forEach(o -> {
				o.accept(isAsync, evt);
			});
		}

	}

}
