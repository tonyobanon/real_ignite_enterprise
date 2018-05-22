package com.re.paas.internal.base.api.event_streams;

import com.re.paas.internal.core.users.Functionality;

public enum ObjectType {

	APPLICATION(Article.AN, Functionality.VIEW_AGENT_ORGANIZATION_APPLICATIONS),

	SYSTEM_CACHE(Article.THE, Functionality.MANAGE_SYSTEM_CACHES),

	SYSTEM_CONFIGURATION(Article.THE, Functionality.VIEW_SYSTEM_CONFIGURATION),

	APPLICATION_FORM(Article.AN, Functionality.MANAGE_APPLICATION_FORMS),

	SYSTEM_CONFIGURATION_FORM(Article.THE, Functionality.MANAGE_SYSTEM_CONFIGURATION_FORM),

	SYSTEM_ROLE(Article.A, Functionality.MANAGE_ROLES),
	
	//For these kinds, Articles must be defined manually by callers
	
	SEMESTER_COURSES(),
	
	EMAIL(),
	
	PHONE_NUMBER(),
	
	PASSWORD(),
	
	IMAGE(),
	
		
	USER_ROLE(Article.THE);
	
	
	private final Article article;
	private final Functionality functionality;

	private ObjectType() {
		this(null);
	}
	
	private ObjectType(Article article) {
		this(article, null);
	}
	
	private ObjectType(Article article, Functionality functionality) {
		this.article = article;
		this.functionality = functionality;
	}
	
	public Article getArticle() {
		return article;
	}
	
	public Functionality getFunctionality() {
		return functionality;
	}
}
