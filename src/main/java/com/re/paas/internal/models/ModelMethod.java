package com.re.paas.internal.models;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.re.paas.internal.core.users.Functionality;

@Retention(SOURCE)
@Target(METHOD)
public @interface ModelMethod {

	Functionality[] functionality();
	boolean experimental() default false;
    boolean isFeatureReady() default true;
}
