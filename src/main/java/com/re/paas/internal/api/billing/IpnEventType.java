package com.re.paas.internal.api.billing;

public enum IpnEventType {
	
	AUTHORISATION_SUCCESS(0, InvoicePaymentStatus.PENDING_3D_SECURE_AUTHORIZATION, InvoicePaymentStatus.AUTHORIZATION_SUCCESS), 
	AUTHORISATION_FAILED(1, InvoicePaymentStatus.PENDING_3D_SECURE_AUTHORIZATION, InvoicePaymentStatus.AUTHORIZATION_FAILED), 
	
	CAPTURE(2, InvoicePaymentStatus.PENDING_CAPTURE), 
	CAPTURE_FAILED(3, InvoicePaymentStatus.PENDING_CAPTURE), 
	
	CANCEL_OR_REFUND(4, InvoicePaymentStatus.PENDING_3D_SECURE_AUTHORIZATION, InvoicePaymentStatus.AUTHORIZATION_SUCCESS), 
	
	CANCELLATION(5, InvoicePaymentStatus.PENDING_3D_SECURE_AUTHORIZATION, InvoicePaymentStatus.AUTHORIZATION_SUCCESS),
	
	REFUNDED_REVERSED(6, InvoicePaymentStatus.REFUNDED), 
	REFUND(7),
	
	REQUEST_FOR_INFORMATION(8), 
	
	NOTIFICATION_OF_CHARGEBACK(9), 	
	CHARGEBACK(10),
	CHARGEBACK_REVERSED(11), 
	
	REPORT_AVAILABLE(12);
	
	private final int value;
	
	private final InvoicePaymentStatus[] acceptableStatuses;
	private Long merchantReference;
	private String message;

	private IpnEventType(Integer value, InvoicePaymentStatus...acceptableStatuses ) {
		this.value = value;
		this.acceptableStatuses = acceptableStatuses;
	}

	public static IpnEventType from(int value) {
		switch (value) {
		case 0:
			return IpnEventType.AUTHORISATION_SUCCESS;
		case 1:
			return IpnEventType.AUTHORISATION_FAILED;
		case 2:
			return IpnEventType.CAPTURE;
		case 3:
			return IpnEventType.CAPTURE_FAILED;
		case 4:
			return IpnEventType.CANCEL_OR_REFUND;
		case 5:
			return IpnEventType.CANCELLATION;
		case 6:
			return IpnEventType.REFUNDED_REVERSED;
		case 7:
			return IpnEventType.REFUND;
		case 8:
			return IpnEventType.REQUEST_FOR_INFORMATION;
		case 9:
			return IpnEventType.NOTIFICATION_OF_CHARGEBACK;
		case 10:
			return IpnEventType.CHARGEBACK;
		case 11:
			return IpnEventType.CHARGEBACK_REVERSED;
		case 12:
			return IpnEventType.REPORT_AVAILABLE;
		default:
			throw new IllegalArgumentException("An invalid value was provided");
		}
	}

	public Integer getValue() {
		return value;
	}

	public String getMessage() {
		return message;
	}

	public IpnEventType setMessage(String message) {
		this.message = message;
		return this;
	}

	public Long getMerchantReference() {
		return merchantReference;
	}

	public IpnEventType setMerchantReference(Long merchantReference) {
		this.merchantReference = merchantReference;
		return this;
	}

	public InvoicePaymentStatus[] getAcceptableStatuses() {
		return acceptableStatuses;
	}

}
