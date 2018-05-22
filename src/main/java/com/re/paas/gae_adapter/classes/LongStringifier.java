package com.re.paas.gae_adapter.classes;

import com.googlecode.objectify.stringifier.Stringifier;

public class LongStringifier implements Stringifier<Long> {

	@Override
	public Long fromString(String arg0) {
		return Long.parseLong(arg0);
	}

	@Override
	public String toString(Long arg0) {
		return arg0.toString();
	}

}
