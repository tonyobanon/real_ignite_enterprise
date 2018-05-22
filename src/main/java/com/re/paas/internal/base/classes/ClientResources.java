package com.re.paas.internal.base.classes;

import java.util.regex.Pattern;

public class ClientResources {

	static final String RB_TRANSLATE_CSS_CLASSNAME = "ce-translate";

	static final Pattern RB_KEY_PATTERN = Pattern.compile("\\A[a-zA-Z]+([_]*[a-zA-Z0-9]+)*\\z");
	
	public static class WebResource {

		private final String uri;
		private final String name;

		private WebResource(String uri, String name) {
			if(!uri.startsWith("http")) {
				uri = "http://" + uri;
			}
			this.uri = uri;
			this.name = name;
		}

		public static WebResource get(String uri, String name) {
			return new WebResource(uri, name);
		}
		
		public String getUri() {
			return uri;
		}

		public String getName() {
			return name;
		}

		@Override
		@ClientAware
		public String toString() {
			return "<a href='" + uri + "'>" + name + "</a>";
		}
	}
	
	public static class ClientRBRefEntry {
		
		private String text;
		private ClientRBRefEntryType type;
		
		public ClientRBRefEntry() {
		}
		
		public ClientRBRefEntry(Object text, ClientRBRefEntryType type) {
			this.text = text.toString();
			this.type = type;
		}

		public String getText() {
			return text;
		}
		
		public ClientRBRefEntry setText(String text) {
			this.text = text;
			return this;
		}
		
		public ClientRBRefEntryType getType() {
			return type;
		}
		
		public ClientRBRefEntry setType(ClientRBRefEntryType type) {
			this.type = type;
			return this;
		}
	}
	
	public static enum ClientRBRefEntryType {
		TRANSLATE, NON_TRANSLATE
	}

	public static enum HtmlCharacterEntities {

		SPACE("&#32;"), NON_BREAKING_SPACE("&nbsp;"), COMMA("&#44;"), LESS_THAN("&lt;"), GREATER_THAN(
				"&gt;"), AMPERSAND("&amp;"), DOUBLE_QUOTATION("&quot"), SINGLE_QUOTATION("&apos");

		private final String value;

		private HtmlCharacterEntities(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}
	
}
