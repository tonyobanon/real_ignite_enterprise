package com.re.paas.internal.core.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositeEntry extends BaseSimpleEntry {

	private String itemsSource;
	private Map<Object, Object> items;

	private ArrayList<String> defaultSelections;
	private boolean allowMultipleChoice;

	public CompositeEntry(Object title) {
		this(null, title);
	}

	public CompositeEntry(Object id, Object title) {
		super(id, title);
		items = new HashMap<>();
	}

	public Map<Object, Object> getItems() {
		return items;
	}

	public CompositeEntry withItems(Map<Object, Object> items) {
		this.items.putAll(items);
		return this;
	}

	public CompositeEntry withItem(Object k, Object v) {
		this.items.put(k, v);
		return this;
	}

	public boolean isAllowMultipleChoice() {
		return allowMultipleChoice;
	}

	public CompositeEntry setAllowMultipleChoice(boolean allowMultipleChoice) {
		this.allowMultipleChoice = allowMultipleChoice;
		return this;
	}

	public String getItemsSource() {
		return itemsSource;
	}

	public CompositeEntry setItemsSource(String itemsSource) {
		this.itemsSource = itemsSource;
		return this;
	}

	public ArrayList<String> getDefaultSelections() {
		return defaultSelections;
	}

	public CompositeEntry setDefaultSelections(List<String> defaultSelections) {
		this.defaultSelections = new ArrayList<>(defaultSelections);
		return this;
	}

}
