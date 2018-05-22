package com.re.paas.gae_adapter.requests.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.re.paas.gae_adapter.core.fusion.GAERouteContext;
import com.re.paas.internal.core.fusion.WebRoutes;

@WebServlet(urlPatterns = { "/" })
public class BaseWebServlet extends BaseServlet {

	private static final long serialVersionUID = 261422209019226946L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String path = req.getServletPath() + (req.getPathInfo() != null ? req.getPathInfo() : "");
		
		if(path.startsWith("/api")) {
			//GAE Fix: In the cloud, this servlet is called for /api/*, but not on local dev. server
			return;
		}
		
		GAERouteContext ctx = new GAERouteContext(req);
		WebRoutes.get(ctx);
		ctx.response().transform(resp);
		resp.flushBuffer();
	}

}
