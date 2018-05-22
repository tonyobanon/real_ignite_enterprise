package com.re.paas.gae_adapter.requests.servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import com.google.appengine.api.LifecycleManager;
import com.google.appengine.api.LifecycleManager.ShutdownHook;
import com.google.common.collect.Lists;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.annotation.Entity;
import com.re.paas.internal.Main;
import com.re.paas.internal.spi.ClassIdentityType;
import com.re.paas.internal.spi.ClasspathScanner;

@WebServlet(urlPatterns = "/baseservlet", loadOnStartup = 5)
public class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {

		try {
			
			new ClasspathScanner<>(Lists.newArrayList("Entity"), Entity.class, ClassIdentityType.ANNOTATION).scanClasses().forEach(e -> {
				ObjectifyService.register(e);
			});
			
			//This only works on GAE production, so in the dev environment use AppShutdownServlet manually
			LifecycleManager.getInstance().setShutdownHook(new ShutdownHook() {
				public void shutdown() {
					LifecycleManager.getInstance().interruptAllRequests();
					if (Main.isStarted()) {
						Main.stop();
					}
				}
			});

			ObjectifyService.run(new VoidWork() {
				public void vrun() {
					Main.main(new String[] {});
				}
			});

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void destroy() {
		if (Main.isStarted()) {
			Main.stop();
		}
	}

}
