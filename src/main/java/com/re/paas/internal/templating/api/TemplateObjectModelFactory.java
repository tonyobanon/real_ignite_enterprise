package com.re.paas.internal.templating.api;

import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.utils.ClassUtils;

public abstract class TemplateObjectModelFactory<M extends TemplateObjectModel> {

	private final Class<M> templateClass;
	
	public TemplateObjectModelFactory() {
		
		@SuppressWarnings("unchecked")
		Class<M> templateClass = (Class<M>) ClassUtils.getGenericSuperclasses(getClass()).get(0);
		
		if(templateClass.getName().equals(TemplateObjectModel.class.getName())) {
			// Only subclasses of TemplateObjectModel are allowed
			Exceptions.throwRuntime("Factories may only be created for subclasses of " + TemplateObjectModel.class.getName());
		}
		
		this.templateClass = templateClass;		
	}

	abstract public M create(M template);

	public Class<M> getObjectModelClass() {
		return templateClass;
	}

}
