package com.re.paas.internal.core.forms;

public class SimpleEntry extends BaseSimpleEntry {

	private InputType inputType;
	private String defaultValue;
	
	private String textValue;

	public SimpleEntry(InputType inputType, Object title) {
		this(null, inputType, title);
	}
	
	public SimpleEntry(Object id, InputType inputType, Object title) {
		super(id, title);
		this.inputType = inputType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
	
	public SimpleEntry setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public InputType getInputType() {
		return inputType;
	}

	public SimpleEntry withInputType(InputType inputType) {
		this.inputType = inputType;
		return this;
	}
	
	public String getTextValue() {
		return textValue;
	}

	public SimpleEntry withTextValue(String textValue) {
		this.textValue = textValue;
		return this;
	}

}
