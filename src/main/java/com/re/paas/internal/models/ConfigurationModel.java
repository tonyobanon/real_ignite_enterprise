package com.re.paas.internal.models;

import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.core.users.Functionality;

@BlockerTodo("Implement ASAP")
public class ConfigurationModel implements BaseModel {

	@Override
	public String path() {
		return "core/configuration";
	}
	
	@ModelMethod(functionality = Functionality.VIEW_SYSTEM_CONFIGURATION)
	public static final void getAll() {
		
		//get all config entries that is marked as front-end
		
	}

	@Override
	public void install(InstallOptions options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preInstall() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unInstall() {
		// TODO Auto-generated method stub
		
	}

}
