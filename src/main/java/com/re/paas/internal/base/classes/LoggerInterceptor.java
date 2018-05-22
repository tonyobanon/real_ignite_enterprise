package com.re.paas.internal.base.classes;

import java.util.Collection;
import java.util.function.Consumer;

public interface LoggerInterceptor extends Consumer<Collection<String>> {
	 
    void accept(Collection<String> t);
 
}
