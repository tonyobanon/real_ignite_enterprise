package com.re.paas.internal.base.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.re.paas.internal.base.classes.LoggerInterceptor;
import com.re.paas.internal.base.logging.LogPipeline;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.models.helpers.Dates;

public class DefaultLogger implements Logger {

	private static final String requestNamespace = "HttpRequest.log";
	private static final String defaultNamespace = "System.log";

	private static Boolean isSnapshotEnabled = false;

	private static Long snapshotInitialInterval = 5l;
	private static Long snapshotInterval = 1l;
	private static LoggerInterceptor loggerInterceptor;

	private static LogPipeline logPipeline = null;

	private static Collection<String> logEntries = Collections.synchronizedList(new ArrayList<String>());

	private static boolean isInfoEnabled;
	private static boolean isWarnEnabled;
	private static boolean isErrorEnabled;
	private static boolean isFatalEnabled;
	private static boolean isDebugEnabled;
	private static boolean isTraceEnabled;

	public DefaultLogger() {
		verboseMode(VerboseLevel.TRACE.toString());
	}
	
	private static void resetModes() {
		isInfoEnabled = false;
		isWarnEnabled = false;
		isErrorEnabled = false;
		isFatalEnabled = false;
		isDebugEnabled = false;
		isTraceEnabled = false;
	}
	
	@Override
	public boolean enabled() {
		return true;
	}
	
	@Override
	public void verboseMode(VerboseLevel verboseLevel) {
		verboseMode(verboseLevel.toString());
	}

	@Override
	public void verboseMode(String verboseLevel) {

		VerboseLevel level = null;

		try {
			level = VerboseLevel.valueOf(verboseLevel);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}

		resetModes();

		switch (level) {
		case OFF:
			break;

		case TRACE:
			isInfoEnabled = true;
			isWarnEnabled = true;
			isErrorEnabled = true;
			isFatalEnabled = true;
			isDebugEnabled = true;
			isTraceEnabled = true;
			break;

		case DEBUG:
			isInfoEnabled = true;
			isWarnEnabled = true;
			isErrorEnabled = true;
			isFatalEnabled = true;
			isDebugEnabled = true;

			break;

		case FATAL:

			isInfoEnabled = true;
			isWarnEnabled = true;
			isErrorEnabled = true;
			isFatalEnabled = true;
			break;

		case ERROR:
			isInfoEnabled = true;
			isWarnEnabled = true;
			isErrorEnabled = true;
			break;

		case WARN:
			isInfoEnabled = true;
			isWarnEnabled = true;
			break;

		case INFO:
			isInfoEnabled = true;
			break;

		default:
			throw new IllegalArgumentException("Unrecognized verbose level");
		}
	}

	@Override
	public void withPipeline(LogPipeline pipeline) {
		logPipeline = pipeline;
	}

	@Override
	public void withSnapshotEnabled(Boolean isEnabled) {
		isSnapshotEnabled = isEnabled;
	}

	@Override
	public void withSnapshotInterval(Long interval) {
		snapshotInterval = interval;
	}

	@Override
	public void withLoggerInterceptor(LoggerInterceptor interceptor) {
		loggerInterceptor = interceptor;
	}

	@Override
	public Boolean isSnapshotEnabled() {
		return isSnapshotEnabled;
	}

	@Override
	public void start() {

		if (isSnapshotEnabled && loggerInterceptor != null) {

			SingleThreadExecutor.scheduleAtFixedRate(() -> {

					// Send Log Entries
					loggerInterceptor.accept(logEntries);

					// Clear Log Entries
					logEntries.clear();

			}, snapshotInitialInterval, snapshotInterval, TimeUnit.SECONDS);

		}
	}
	
	@Override
	public void stop() {
		
	}

	private static boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	private static boolean isErrorEnabled() {
		return isErrorEnabled;
	}

	private static boolean isFatalEnabled() {
		return isFatalEnabled;
	}

	private static boolean isInfoEnabled() {
		return isInfoEnabled;
	}

	private static boolean isTraceEnabled() {
		return isTraceEnabled;
	}

	private static boolean isWarnEnabled() {
		return isWarnEnabled;
	}

	private static void logDelegate(VerboseLevel level, String namespace, String message) {
		SingleThreadExecutor.execute(() -> {
				String[] lines = format(namespace, message, level);

				if (logPipeline != null) {
					for (String line : lines) {
						logPipeline.println(level, line);
					}
				}

				if (isSnapshotEnabled) {
					for (String line : lines) {
						logEntries.add(line);
					}
				}
		});
	}

	private static final String getNamespace() {
		return ThreadContext.isRequestContext() ? requestNamespace : defaultNamespace;
	}
	
	@Override
	public void debug(String msg) {
		debug(getNamespace(), msg);
	}

	@Override
	public void debug(String namespace, String msg) {
		if (isDebugEnabled()) {
			logDelegate(VerboseLevel.DEBUG, namespace, msg);
		}
	}

	@Override
	public void error(String msg) {
		error(getNamespace(), msg);
	}

	@Override
	public void error(String namespace, String msg) {
		if (isErrorEnabled()) {
			logDelegate(VerboseLevel.ERROR, namespace, msg);
		}
	}

	@Override
	public void fatal(String msg) {
		fatal(getNamespace(), msg);
	}

	@Override
	public void fatal(String namespace, String msg) {
		if (isFatalEnabled()) {
			logDelegate(VerboseLevel.FATAL, namespace, msg);
		}
	}

	@Override
	public void info(String msg) {
		info(getNamespace(), msg);
	}

	@Override
	public void info(String namespace, String msg) {
		if (isInfoEnabled()) {
			logDelegate(VerboseLevel.INFO, namespace, msg);
		}
	}

	@Override
	public void trace(String msg) {
		trace(getNamespace(), msg);
	}

	@Override
	public void trace(String namespace, String msg) {
		if (isTraceEnabled()) {
			logDelegate(VerboseLevel.TRACE, namespace, msg);
		}
	}

	@Override
	public void warn(String msg) {
		warn(getNamespace(), msg);
	}

	@Override
	public void warn(String namespace, String msg) {
		if (isWarnEnabled()) {
			logDelegate(VerboseLevel.WARN, namespace, msg);
		}
	}

	private static String[] format(String namespace, String msg, VerboseLevel level) {

		String[] lines = msg.split("\\n");

		String[] result = new String[lines.length];
		result[0] = "[" + Dates.now().toString() + "]" + " " + "[" + level.name() + "]" + " " + "[" + namespace + "]"
				+ " " + "[" + lines[0] + "]";

		if (lines.length > 1) {
			for (int i = 1; i < lines.length; i++) {
				result[i] = "  " + lines[i];
			}
		}
		return result;
	}

	static {
	}
}
