package com.re.paas.internal.base;

import java.io.IOException;

import com.re.paas.internal.app_provisioning.AppProvisioning;
import com.re.paas.internal.base.classes.ConsoleSignals;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.logging.LogPipeline;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.base.logging.Logger.VerboseLevel;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.clustering.Clustering;
import com.re.paas.internal.clustering.NodeRole;
import com.re.paas.internal.core.cron.Scheduler;
import com.re.paas.internal.core.fusion.FusionServiceDelegate;
import com.re.paas.internal.core.fusion.ServerOptions;
import com.re.paas.internal.core.fusion.WebRoutes;
import com.re.paas.internal.core.fusion.WebServer;
import com.re.paas.internal.errors.ErrorHelper;
import com.re.paas.internal.events.EventBus;
import com.re.paas.internal.models.BaseModelLocator;
import com.re.paas.internal.spi.SpiBase;
import com.re.paas.internal.spi.SpiTypes;

public class AppDelegate {

	public static final String APPLICATION_NAME = "Compute Essentials";

	public static final String SOFTRWARE_VENDER_NAME = "Compute Essentials, Inc";
	public static final String SOFTRWARE_VENDER_EMAIL = "corporate@compute-essentials.com";

	public static void main(boolean safeMode) {

		if (!safeMode) {
			AppProvisioning.start();
		}
		
		SpiBase.start();

		Logger.scanAll();
		
		Logger.get().withPipeline(LogPipeline.from(System.out, System.err));

		Logger.get().verboseMode(VerboseLevel.DEBUG);

		Logger.get().debug("Starting Application ..");

		CloudEnvironment.detect();

		ErrorHelper.init();

		NodeRole.scanAll();

		BaseModelLocator.scanModels();

		FusionServiceDelegate.scanRoutes();

		// WebRoutes.scanRoutes();

		// @Dev
		// BaseModel.startAll();

		EventBus.start();

		Logger.get().info("Starting Clustering Engine");

		Clustering.start();

		// Ensure that our application is shutdown gracefully
		if (CloudEnvironment.get().isStandalone()) {

			if (!CloudEnvironment.get().isProduction()) {
				ConsoleSignals.start();
				ConsoleSignals.addHook("exit", () -> {
					AppDelegate.stop();
				});
			}

			// Add shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				stop();
			}));

		} else {
			// For the Servlet/GAE shutdown hook, see
			// com.ce.saas.gae_adapter.requests.servlets.BaseServlet
		}

	}

	public static void stop() {

		Logger.get().info("Stopping Application ..");

		Scheduler.getDefaultExecutor().shutdownNow();

		// Stop File watcher

		if (!CloudEnvironment.get().isProduction()) {

			if (WebRoutes.fileWatcherPool != null) {
				WebRoutes.fileWatcherPool.shutdownNow();
			}

			if (WebRoutes.watchService != null) {

				try {
					WebRoutes.watchService.close();
				} catch (IOException e) {
					Exceptions.throwRuntime(e);
				}
			}
		}

		// Stop Clustering services

		Clustering.stop();
	}

	public static String getPlatformPrefix() {
		return "ce";
	}

	public static String getPlatformName() {
		return "ce";
	}

}
