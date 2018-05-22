package com.re.paas.gae_adapter.classes;

import com.googlecode.objectify.stringifier.Stringifier;

public class IntStringifier implements Stringifier<Integer> {

	@Override
	public Integer fromString(String arg0) {
		return Integer.parseInt(arg0);
	}

	@Override
	public String toString(Integer arg0) {
		return arg0.toString();
	}

}
