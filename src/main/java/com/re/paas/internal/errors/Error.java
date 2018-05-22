package com.re.paas.internal.errors;

/**
 * All enums implementing this interface should at least contain a static method
 * from(int)
 */
public interface Error {

	public int getCode();

	public String getMessage();

	public boolean isFatal();

}
