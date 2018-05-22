package com.re.paas.internal.base.classes;

public enum Gender {

	FEMALE(0), MALE(1);

	private int value;

	private Gender(Integer value) {
		this.value = value;
	}

	public static Gender from(int value) {

		switch (value) {

		case 0:
			return Gender.FEMALE;
			
		case 1:
			return Gender.MALE;
			
		default:
			throw new IllegalArgumentException("An invalid value was provided");
		}
	}

	public int getValue() {
		return value;
	}
}
