package com.re.paas.internal.core.fusion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.re.paas.internal.core.users.Functionality;

import io.vertx.core.http.HttpMethod;

@Retention(RetentionPolicy.RUNTIME)
public @interface FusionEndpoint {
	
	String uri();

	boolean createXhrClient() default true;
	
	boolean requireSSL() default false;
	
	boolean isBlocking() default false;
	
	boolean isAsync() default true;
	
	String[] headerParams() default {};

	String[] requestParams() default {};

	String[] bodyParams() default {};
	
	boolean enableMultipart() default false;
	
	boolean cache() default false;
	
	HttpMethod method() default HttpMethod.GET;

	Functionality functionality();
	
	Class<? extends ServiceAuthenticator> customAuthenticator() default DefaultServiceAuthenticator.class;
}
