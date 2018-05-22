package com.re.paas.internal.core.fusion;

import com.re.paas.internal.base.core.ResourceException;

public class ServiceException extends ResourceException {

	public ServiceException(int errCode) {
		super(errCode);
	}
	
	public ServiceException(int errCode, String msg) {
		super(errCode, msg);
	}

	private static final long serialVersionUID = 1L;
	
}
