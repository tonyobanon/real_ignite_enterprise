package com.re.paas.internal.base.core;

import java.util.HashMap;
import java.util.Map;

public class ThreadContext {

	private static final String IS_REQUEST_CONTEXT = "is_request_ctx";

	private static final ThreadContext instance = new ThreadContext();

	private static ThreadLocal<Map<String, Object>> threadAttrs = new ThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			return new HashMap<String, Object>();
		}
	};

	public static Object empty() {
		return threadAttrs.get().isEmpty();
	}

	public static Object get(String key) {
		return threadAttrs.get().get(key);
	}

	public static ThreadContext set(String key, Object value) {
		threadAttrs.get().put(key, value);
		return instance;
	}

	public static ThreadContext remove(String key) {
		threadAttrs.get().remove(key);
		return instance;
	}

	public static ThreadContext clear() {
		threadAttrs.get().clear();
		return instance;
	}

	/**
	 * This indicates that the current thread executes within the context of a HTTP
	 * request
	 */
	public static ThreadContext newRequestContext() {
		Map<String, Object> o = threadAttrs.get();
		o.clear();
		o.put(IS_REQUEST_CONTEXT, true);
		return instance;
	}
	
	/**
	 * This checks if the current thread executes within the context of a HTTP
	 * request
	 */
	public static boolean isRequestContext() {
		Boolean o = (Boolean) threadAttrs.get().get(IS_REQUEST_CONTEXT);
		return o != null && o == true;
	}

}
