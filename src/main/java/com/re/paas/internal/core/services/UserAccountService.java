package com.re.paas.internal.core.services;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import com.re.paas.internal.base.classes.spec.LoginIdType;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.core.fusion.BaseService;
import com.re.paas.internal.core.fusion.CookieImpl;
import com.re.paas.internal.core.fusion.FusionEndpoint;
import com.re.paas.internal.core.fusion.FusionHelper;
import com.re.paas.internal.core.fusion.Sessions;
import com.re.paas.internal.core.fusion.WebRoutes;
import com.re.paas.internal.core.keys.CacheValues;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.utils.Utils;

import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

public class UserAccountService extends BaseService {
@Override
public String uri() {
	return "/users/accounts";
}
	@FusionEndpoint(uri = "/authenticateUser", headerParams = {"email", "pass", "rem" }, requestParams = {
			"idType", "returnUrl" }, functionality = Functionality.PHONE_LOGIN_USER)
	public void authenticateUser(RoutingContext ctx) {

		LoginIdType type = LoginIdType.from(Integer.parseInt(ctx.request().getParam("idType")));

		String pass = ctx.request().getHeader("pass");
		String rem = ctx.request().getHeader("rem");

		String returnUrl = ctx.request().getParam("returnUrl");

		try {

			Long userId = null;

			switch (type) {
			case EMAIL:
				String email = ctx.request().getHeader("email");
				userId = BaseUserModel.loginByEmail(email, pass);
				break;
			case PHONE:
				Long phone = Long.parseLong(ctx.request().getHeader("phone"));
				userId = BaseUserModel.loginByPhone(phone, pass);
				break;
			}

			String sessionToken = Utils.newRandom();

			loginUser(userId, sessionToken, ctx.request().remoteAddress().host(),
					rem.equals("true") ? CacheValues.SESSION_TOKEN_COOKIE_LONG_EXPIRY_IN_SECS
							: CacheValues.SESSION_TOKEN_COOKIE_SHORT_EXPIRY_IN_SECS);

			if (returnUrl.equals("null")) {
				returnUrl = WebRoutes.DEFAULT_CONSOLE_URI;
			}

			Cookie cookie = new CookieImpl(FusionHelper.sessionTokenName(), sessionToken).setPath("/");

			cookie.setMaxAge(rem.equals("true") ? CacheValues.SESSION_TOKEN_COOKIE_LONG_EXPIRY_IN_SECS
					: CacheValues.SESSION_TOKEN_COOKIE_SHORT_EXPIRY_IN_SECS);

			ctx.addCookie(cookie);

			ctx.response().setStatusCode(HttpServletResponse.SC_FOUND);
			ctx.response().putHeader(getLocationHeader(), returnUrl);

		} catch (PlatformException e) {
			ctx.response().setStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
		}
		ctx.response().end();
	}

	private static void loginUser(Long userId, String sessionToken, String remoteAdress, int expiry) {
		Sessions.newSession(userId, sessionToken, expiry, remoteAdress, TimeUnit.SECONDS);
	}

}
