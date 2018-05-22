package com.re.paas.internal.base.logging;

import com.re.paas.internal.base.classes.LoggerInterceptor;
import com.re.paas.internal.base.core.DefaultLogger;
import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.spi.SpiTypes;

public interface Logger {

	public static Logger get() {
		@SuppressWarnings("unchecked")
		SpiDelegate<Logger> delegate = (SpiDelegate<Logger>) SpiDelegate.getDelegate(SpiTypes.LOGGER);
		if(delegate == null) {
			return new DefaultLogger();
		} else {
			return (Logger) delegate.get();
		}
	}
	
	public boolean enabled();

	public abstract void verboseMode(VerboseLevel verboseLevel);

	public abstract void verboseMode(String verboseLevel);

	public abstract void withPipeline(LogPipeline pipeline);

	public abstract void withSnapshotEnabled(Boolean isEnabled);

	public abstract void withSnapshotInterval(Long interval);

	public abstract void withLoggerInterceptor(LoggerInterceptor interceptor);

	public abstract Boolean isSnapshotEnabled();

	public abstract void start();

	public abstract void stop();

	public abstract void debug(String msg);

	public abstract void debug(String namespace, String msg);

	public abstract void error(String msg);

	public abstract void error(String namespace, String msg);

	public abstract void fatal(String msg);

	public abstract void fatal(String namespace, String msg);

	public abstract void info(String msg);

	public abstract void info(String namespace, String msg);

	public abstract void trace(String msg);

	public abstract void trace(String namespace, String msg);

	public abstract void warn(String msg);

	public abstract void warn(String namespace, String msg);

	public static enum VerboseLevel {
		OFF, INFO, WARN, ERROR, FATAL, DEBUG, TRACE
	}

}
