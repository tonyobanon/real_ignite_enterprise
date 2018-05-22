package com.re.paas.internal.core.pdf;

import java.util.List;

import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.core.forms.Question;

public class Section {

	private String id;
	private ClientRBRef title;
	private ClientRBRef summary;
	private List<Question> entries;
	
	public List<Question> getEntries() {
		return entries;
	}

	public Section withEntry(Question entry) {
		this.entries.add(entry);
		return this;
	}

	public Section withEntries(List<Question> entries) {
		this.entries = entries;
		return this;
	}

	public ClientRBRef getTitle() {
		return title;
	}

	public Section setTitle(ClientRBRef title) {
		this.title = title;
		return this;
	}

	public ClientRBRef getSummary() {
		return summary;
	}

	public Section setSummary(ClientRBRef summary) {
		this.summary = summary;
		return this;
	}

	public String getId() {
		return id;
	}

	public Section setId(String id) {
		this.id = id;
		return this;
	}
}
