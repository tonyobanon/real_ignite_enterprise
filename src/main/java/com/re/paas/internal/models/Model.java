package com.re.paas.internal.models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {
	
	boolean enabled() default true;

	String version() default BaseModelLocator.DEFAULT_MODEL_VERSION;
	
	Class<? extends BaseModel>[] dependencies() default {};
	
}
