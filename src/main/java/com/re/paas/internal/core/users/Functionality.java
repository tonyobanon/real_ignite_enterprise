package com.re.paas.internal.core.users;

import com.re.paas.internal.base.core.BlockerTodo;

@BlockerTodo("Add RB support to functionality titles")
public enum Functionality {
	
	/* No Auth */
	
	EXECUTE_CRON_JOBS(-5, "Execute Cron Jobs"),
	PAYMENT_NOIFICATION_CALLBACK(-6, "Payment Notification Callback"),
	
	// Forms Module ..
	VIEW_APPLICATION_FORM(-8, "View application form"),
	
	CREATE_APPLICATION(-10, "Create new application"),
	DOWNLOAD_QUESTIONNAIRE(-30, "Download questionairre"),
	
	GET_FORM_FIELD_IDS(-15, "Get form field Ids"),
	UPDATE_APPLICATION(-20, "Update existing application"),
	SUBMIT_APPLICATION(-21, "Sumit application"),
	
	GET_ROLE_REALMS(-80, "List role realms"),
	LIST_ROLES(-81, "List role names"),
	
	// Auth/Roles Module
	EMAIL_LOGIN_USER(-100, "Email user Login"),
	PHONE_LOGIN_USER(-110, "Email user Login"),
	
	// Core Module
	GET_LOCATION_DATA(-120, "Get location data"),
	GET_COUNTRY_NAMES(-121, "Get country names"),
	GET_TERRITORY_NAMES(-122, "Get territory names"),
	GET_CITY_NAMES(-123, "Get city names"),
	
	GET_BINARY_DATA(-130, "Get binary data"),
	SAVE_BINARY_DATA(-140, "Save binary data"),
	
	PLATFORM_INSTALLATION(-150, "Install platform"),
	
	GET_AVAILABLE_COUNTRIES(-160, "Get available countries"),
	GET_RESOURCE_BUNDLE_ENTRIES(-170, "Get resource bundle entries"),
	
	
	LIST_AGENT_ORGANIZATION_NAMES(-200, "List agent organization names"),
	
	VIEW_AGENT_ORGANIZATION(-202, "View Agent Organization"),
	LIST_AGENT_ORGANIZATION(-204, "List Agent Organizations"),
	SEARCH_AGENT_ORGANIZATION(-206, "Search Agent Organizations"),
	
	
	CREATE_AGENT_ORGANIZATION_MESSAGES(-210, "Create agent organzation messages"),
	CREATE_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES(-213, "Create agent organzation whistleblow messages"),

	CREATE_AGENT_ORGANIZATION_REVIEW(-216, "Create agent organzation review"),
	VIEW_AGENT_ORGANIZATION_REVIEWS(-217, "View agent organization reviews"),
	
	LIST_PROPERTY(-220, "List property"),
	VIEW_PROPERTY(-230, "View property"),
	
	
	GET_CITY_FEATURES(-260, "Get city features"),
	
	LIST_PROPERTY_TYPE_FEATURES(-270, "List property type features"),
	
	VIEW_AGENT(-280, "View Agent"),

	PERFORM_LIST_OPERATION(-290, "Perform list operation"),
	
	
	
	
	
	/*	AUTHORIZATION REQUIRED	*/
	
	
	ADD_SYSTEM_MOCK_DATA(1, "Add system mock data", false),
	
	
	
	/* Base User */
	
	VIEW_OWN_PROFILE(2, "View own profile", false),
	MANAGE_OWN_PROFILE(4, "Manage own profile", false),
	
	

	/* Auth */
	
	
	// Customer
	
	ADD_TO_OWN_SAVED_LIST(8, "Add to own saved list"),
	REMOVE_FROM_OWN_SAVED_LIST(10, "Remove from own saved list"),
	GET_OWN_SAVED_LIST(12, "Get own saved list"),
	
	
	// Base Principal

	GET_SEARCHABLE_LISTS(15, "Get searchable lists"),
	GET_PERSON_NAMES(80, "Get person names", false),
	
	GET_REALM_FUNCTIONALITIES(101, "Get realm functionalities", false),
	GET_ROLE_FUNCTIONALITIES(102, "Get role functionalities", false),
		
	
	
	// Forms Module ..
	MANAGE_APPLICATION_FORMS(30, "Manage application forms"),
	MANAGE_SYSTEM_CONFIGURATION_FORM(35, "Manage system configuration form"),
	
	VIEW_AGENT_ORGANIZATION_APPLICATIONS(40, "View agent organization applications"),
	REVIEW_AGENT_ORGANIZATION_APPLICATION(50, "Review agent organization applications"),
	
	VIEW_AGENT_APPLICATIONS(52, "View agent organization applications"),
	REVIEW_AGENT_APPLICATION(53, "Review agent organization applications"),

	VIEW_ADMIN_APPLICATIONS(54, "View agent organization applications"),
	REVIEW_ADMIN_APPLICATION(55, "Review agent organization applications"),
	
	// Core Module ..
	GET_USER_PROFILE(70, "Get user profile"),
	MANAGE_USER_ACCOUNTS(60, "Manage user accounts"),
	
	// Core Module ..
	MANAGE_BINARY_DATA(90, "Manage binary data"),
	
	// Auth/Roles Module
	MANAGE_ROLES(100, "Manage roles"),
	
	// Core Module
	MANAGE_SYSTEM_CACHES(105, "Manage system caches"),
	
	VIEW_SYSTEM_CONFIGURATION(111, "View system configuration"),
	UPDATE_SYSTEM_CONFIGURATION(110, "Update system configuration"),
	
	VIEW_SYSTEM_MTERICS(120, "View system metrics"),
	
	MANAGE_ACTIVITY_STREAM(340, "Manage activity Stream"),
	

	
	
	UPDATE_AGENT_ORGANIZATION(402, "Update Agent Organization"),
	
	DELETE_AGENT_ORGANIZATION(404, "Delete Agent Organization"),
	
	
	DELETE_AGENT(420, "Delete Agent"),
	
	
	LIST_AGENT_ORGANIZATION_MESSAGES(450, "List agent organization messages"),
	UPDATE_AGENT_ORGANIZATION_MESSAGES(452, "Update agent organzation messages"),
	DELETE_AGENT_ORGANIZATION_MESSAGES(454, "Delete agent organzation messages"),
	VIEW_AGENT_ORGANIZATION_MESSAGE(456, "View agent organzation message"),
	
	
	LIST_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES(470, "List agent organization whistleblow messages"),
	UPDATE_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES(472, "Update agent organzation whistleblow messages"),
	DELETE_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES(474, "Delete agent organzation whistleblow messages"),
	VIEW_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGE(476, "View agent organzation whistleblow message"),
	
	
	DELETE_AGENT_ORGANIZATION_REVIEWS(480, "Delete agent organzation reviews"),
	
	
	
	CREATE_PROPERTY_CREATION_REQUEST(500, "Create property creation request"),
	CREATE_PROPERTY_UPDATE_REQUEST(510, "Create property update request"),
	CREATE_PROPERTY_DELETION_REQUEST(520, "Delete property"),
	
	REVIEW_PROPERTY_REQUEST(530, "Review property request"),
	
	CREATE_PROPERTY_LISTING(560, "Create property listing"),
	UPDATE_PROPERTY_LISTING(570, "Update property listing"),
	DELETE_PROPERTY_LISTING(580, "Delete property listing"),
	
	GET_PROPERTY_LISTINGS(590, "Get property listings"),
	
	UPDATE_PROPERTY_LISTING_AVAILABILITY_STATUS(600, "Update property listing available status"),
	
	SET_CITY_FEATURES(610, "Set city features"),
	
	CREATE_PROPERTY_TYPE_FEATURES(620, "Create property type feature"),
	DELETE_PROPERTY_TYPE_FEATURES(622, "Delete property type feature"),
	
	
	ADD_TO_USER_SAVED_LIST(640, "Add to user saved list"),
	REMOVE_FROM_USER_SAVED_LIST(642, "Remove from user saved list"),
	GET_USER_SAVED_LIST(644, "Get user saved list"),
	
	
	CREATE_PROPERTY_PRICE_RULE(650, "Create property price rule"),
	VIEW_PROPERTY_PRICE_RULES(652, "View property price rule"),
	UPDATE_PROPERTY_PRICE_RULE(654, "Update property price rule"),
	DELETE_PROPERTY_PRICE_RULE(656, "Delete property price rule"),
	

	UPDATE_AGENT_AVAILABILITY_SCHEDULE(670, "Update Agent Availability Schedule"),
	GET_AGENT_AVAILABILITY_SCHEDULES(672, "Get Agent Availability Schedules"),
	
	UPDATE_AGENT_ORGANIZATION_AVAILABILITY_SCHEDULE(680, "Update Agent Availability Schedule"),
	GET_AGENT_ORGANIZATION_AVAILABILITY_SCHEDULES(682, "Get Agent Organization Availability Schedules"),
	

	CREATE_CRON_TASK(690, "New Cron Task"),
	GET_CRON_TASK_MODEL(692, "Get Cron Task Model"),
	VIEW_CRON_JOBS(694, "View Cron Jobs"),
	DELETE_CRON_TASK(696, "Delete Cron Task");
	
	private final int id;
	private final String name;
	private final Boolean isVisible;

	private Functionality(int id, String name) {
		this(id, name, true);
	}
	
	private Functionality(int id, String name, boolean isVisible) {
		this.id = id;
		this.name = name;
		this.isVisible = isVisible;
	}

	public static Functionality from(Integer value) {

		switch (value) {

		case -5:
			return Functionality.EXECUTE_CRON_JOBS;
			
		case -8:
			return Functionality.VIEW_APPLICATION_FORM;
		case -10:
			return Functionality.CREATE_APPLICATION;
		case -15:
			return Functionality.GET_FORM_FIELD_IDS;
		case -20:
			return Functionality.UPDATE_APPLICATION;
		case -21:
			return Functionality.SUBMIT_APPLICATION;
		case -30:
			return Functionality.DOWNLOAD_QUESTIONNAIRE;
		case -80:
			return Functionality.GET_ROLE_REALMS;
		case -81:
			return Functionality.LIST_ROLES;
		case -100:
			return Functionality.EMAIL_LOGIN_USER;
			
		case -110:
			return Functionality.PHONE_LOGIN_USER;
		case -120:
			return Functionality.GET_LOCATION_DATA;
		case -121:
			return Functionality.GET_COUNTRY_NAMES;
		case -122:
			return Functionality.GET_TERRITORY_NAMES;
		case -123:
			return Functionality.GET_CITY_NAMES;
		case -130:
			return Functionality.GET_BINARY_DATA;
		case -140:
			return Functionality.SAVE_BINARY_DATA;
		case -150:
			return Functionality.PLATFORM_INSTALLATION;
		case -160:
			return Functionality.GET_AVAILABLE_COUNTRIES;
		case -170:
			return Functionality.GET_RESOURCE_BUNDLE_ENTRIES;
		case -200:
			return Functionality.LIST_AGENT_ORGANIZATION_NAMES;
		case -202:
			return Functionality.VIEW_AGENT_ORGANIZATION;
		case -204:
			return Functionality.LIST_AGENT_ORGANIZATION;
		case -206:
			return Functionality.SEARCH_AGENT_ORGANIZATION;
		case -210:
			return Functionality.CREATE_AGENT_ORGANIZATION_MESSAGES;
		case -213:
			return Functionality.CREATE_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES;
		case -216:
			return Functionality.CREATE_AGENT_ORGANIZATION_REVIEW;
		case -217:
			return Functionality.VIEW_AGENT_ORGANIZATION_REVIEWS;
			
		case -220:
			return Functionality.LIST_PROPERTY;
		case -230:
			return Functionality.VIEW_PROPERTY;
			
		case -260:
			return Functionality.GET_CITY_FEATURES;
			
		case -270:
			return Functionality.LIST_PROPERTY_TYPE_FEATURES;
			
		case -280:
			return Functionality.VIEW_AGENT;
			
		case -290:
			return Functionality.PERFORM_LIST_OPERATION;
			
		case 1:
			return Functionality.ADD_SYSTEM_MOCK_DATA;
		case 2:
			return Functionality.VIEW_OWN_PROFILE;
		case 4:
			return Functionality.MANAGE_OWN_PROFILE;
			
		case 8:
			return Functionality.ADD_TO_OWN_SAVED_LIST;
		case 10:
			return Functionality.REMOVE_FROM_OWN_SAVED_LIST;
		case 12:
			return Functionality.GET_OWN_SAVED_LIST;

		case 15:
			return Functionality.GET_SEARCHABLE_LISTS;
		case 30:
			return Functionality.MANAGE_APPLICATION_FORMS;
		case 35:
			return Functionality.MANAGE_SYSTEM_CONFIGURATION_FORM;
		case 40:
			return Functionality.VIEW_AGENT_ORGANIZATION_APPLICATIONS;
		case 50:
			return Functionality.REVIEW_AGENT_ORGANIZATION_APPLICATION;
		case 52:
			return Functionality.VIEW_AGENT_APPLICATIONS;
		case 53:
			return Functionality.REVIEW_AGENT_APPLICATION;
		case 54:
			return Functionality.VIEW_ADMIN_APPLICATIONS;
		case 55:
			return Functionality.REVIEW_ADMIN_APPLICATION;
		case 60:
			return Functionality.MANAGE_USER_ACCOUNTS;
		case 70:
			return Functionality.GET_USER_PROFILE;
		case 80:
			return Functionality.GET_PERSON_NAMES;
		case 90:
			return Functionality.MANAGE_BINARY_DATA;
		case 100:
			return Functionality.MANAGE_ROLES;
		case 101:
			return Functionality.GET_REALM_FUNCTIONALITIES;
		case 102:
			return Functionality.GET_ROLE_FUNCTIONALITIES;
			
		case 105:
			return Functionality.MANAGE_SYSTEM_CACHES;
		case 110:
			return Functionality.UPDATE_SYSTEM_CONFIGURATION;
		case 111:
			return Functionality.VIEW_SYSTEM_CONFIGURATION;
		case 120:
			return Functionality.VIEW_SYSTEM_MTERICS;	
		case 340:
			return Functionality.MANAGE_ACTIVITY_STREAM;
			
		case 402:
			return Functionality.UPDATE_AGENT_ORGANIZATION;
		case 404:
			return Functionality.DELETE_AGENT_ORGANIZATION;
			
		case 420:
			return Functionality.DELETE_AGENT;
			
		case 430:
			return Functionality.UPDATE_AGENT_AVAILABILITY_SCHEDULE;
			
		case 450:
			return Functionality.LIST_AGENT_ORGANIZATION_MESSAGES;			
		case 452:
			return Functionality.UPDATE_AGENT_ORGANIZATION_MESSAGES;
		case 454:
			return Functionality.DELETE_AGENT_ORGANIZATION_MESSAGES;
		case 456:
			return Functionality.VIEW_AGENT_ORGANIZATION_MESSAGE;
			
		case 470:
			return Functionality.LIST_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES;			
		case 472:
			return Functionality.UPDATE_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES;
		case 474:
			return Functionality.DELETE_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES;
		case 476:
			return Functionality.VIEW_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGE;
			
					
		case 480:
			return Functionality.DELETE_AGENT_ORGANIZATION_REVIEWS;	
			
			
		case 500:
			return Functionality.CREATE_PROPERTY_CREATION_REQUEST;
		case 510:
			return Functionality.CREATE_PROPERTY_UPDATE_REQUEST;
			
		case 520:
			return Functionality.CREATE_PROPERTY_DELETION_REQUEST;

		case 530:
			return Functionality.REVIEW_PROPERTY_REQUEST;
			
			
		case 560:
			return Functionality.CREATE_PROPERTY_LISTING;
			
		case 570:
			return Functionality.UPDATE_PROPERTY_LISTING;
			
		case 580:
			return Functionality.DELETE_PROPERTY_LISTING;
		
		case 590:
			return Functionality.GET_PROPERTY_LISTINGS;
			
		case 600:
			return Functionality.UPDATE_PROPERTY_LISTING_AVAILABILITY_STATUS;
			
		case 610:
			return Functionality.SET_CITY_FEATURES;
			
			
			
			
		case 620:
			return Functionality.CREATE_PROPERTY_TYPE_FEATURES;
			
		case 622:
			return Functionality.DELETE_PROPERTY_TYPE_FEATURES;
			
			
		case 640:
			return Functionality.ADD_TO_USER_SAVED_LIST;
		case 642:
			return Functionality.REMOVE_FROM_USER_SAVED_LIST;
		case 644:
			return Functionality.GET_USER_SAVED_LIST;
			
			
		case 650:
			return Functionality.CREATE_PROPERTY_PRICE_RULE;
		case 652:
			return Functionality.VIEW_PROPERTY_PRICE_RULES;
		case 654:
			return Functionality.UPDATE_PROPERTY_PRICE_RULE;
		case 656:
			return Functionality.DELETE_PROPERTY_PRICE_RULE;
			
			
		case 670:
			return Functionality.UPDATE_AGENT_AVAILABILITY_SCHEDULE;
			
		case 672:
			return Functionality.GET_AGENT_AVAILABILITY_SCHEDULES;
			
			
		case 680:
			return Functionality.UPDATE_AGENT_ORGANIZATION_AVAILABILITY_SCHEDULE;
			
		case 682:
			return Functionality.GET_AGENT_ORGANIZATION_AVAILABILITY_SCHEDULES;
			
			
		case 690:
			return Functionality.CREATE_CRON_TASK;
			
		case 692:
			return Functionality.GET_CRON_TASK_MODEL;
			
		case 694:
			return Functionality.VIEW_CRON_JOBS;
			
		case 696:
			return Functionality.VIEW_CRON_JOBS;
			
		default:
			throw new IllegalArgumentException("An invalid value was provided");
		}
	}

	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}

	public Boolean getIsVisible() {
		return isVisible;
	}
	
}
