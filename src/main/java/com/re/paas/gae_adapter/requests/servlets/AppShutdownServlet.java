package com.re.paas.gae_adapter.requests.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.re.paas.internal.base.AppDelegate;
import com.re.paas.internal.cloud_provider.CloudEnvironment;

/**
 * This class serves as a mechanism to shutdown the app on the GAE dev environment, since LifecycleManager only 
 * works on the production servers, and the GAE dev container always shuts down abruptly without calling 
 * the destroy() method of servlets.
 * */
@WebServlet(urlPatterns = "/internal/gae_adapter/app/shutdown")
public class AppShutdownServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(!CloudEnvironment.get().isProduction()) {
			AppDelegate.stop();
		}		
	}
}
