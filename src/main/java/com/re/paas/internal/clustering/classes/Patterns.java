package com.re.paas.internal.clustering.classes;

import java.util.regex.Pattern;

public class Patterns {

	public static Pattern fullAlphaNumeric() {
		return Pattern.compile("\\A\\p{Alnum}+\\z");
	}

	public static Pattern Entry() {
		return Pattern.compile("(\\p{Space})*[a-z]*(\\p{Space})*:(\\p{Space})*\\p{ASCII}+(\\p{Space})*");
	}

	public static Pattern KVSeperator() {
							return Pattern.compile("(\\p{Space})*:(\\p{Space})*");
	}

	public static Pattern commaSeperator() {
		return Pattern.compile("(\\p{Space})*,(\\p{Space})*");
	}
	
	public static Pattern azureVmUUID() {
		return Pattern.compile("(\\p{Alnum}){8}-(\\p{Alnum}){4}-(\\p{Alnum}){4}-(\\p{Alnum}){4}-(\\p{Alnum}){12}");
	}

}
