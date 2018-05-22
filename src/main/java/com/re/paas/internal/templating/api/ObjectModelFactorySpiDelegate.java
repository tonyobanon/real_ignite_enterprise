package com.re.paas.internal.templating.api;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.spi.SpiTypes;
import com.re.paas.internal.utils.ClassUtils;

public class ObjectModelFactorySpiDelegate
		extends SpiDelegate<TemplateObjectModelFactory<? extends TemplateObjectModel>> {

	@Override
	protected void init() {
		get(c -> {
			addTemplateObjectModelFactory(c);
		});
	}

	@Override
	protected void add(List<Class<TemplateObjectModelFactory<? extends TemplateObjectModel>>> classes) {
		classes.forEach(c -> {
			addTemplateObjectModelFactory(c);
		});
	}

	@Override
	protected void remove(List<Class<TemplateObjectModelFactory<? extends TemplateObjectModel>>> classes) {
		classes.forEach(c -> {
			removeTemplateObjectModelFactory(c);
		});
	}
	
	static Map<Class<?>, TemplateObjectModelFactory<? extends TemplateObjectModel>> getResources() {

		Map<Class<?>, TemplateObjectModelFactory<? extends TemplateObjectModel>> map = Maps.newHashMap();

		getAll(SpiTypes.TEMPLATE_OBJECT_MODEL_FACTORY).forEach((k, v) -> {
			map.put((Class<?>) k, (TemplateObjectModelFactory<?>) v);
		});

		return map;
	}

	private void removeTemplateObjectModelFactory(Class<TemplateObjectModelFactory<? extends TemplateObjectModel>> c) {
		
		Map<Class<?>, TemplateObjectModelFactory<? extends TemplateObjectModel>> resources = getResources();
		
		Set<Entry<Class<?>, TemplateObjectModelFactory<? extends TemplateObjectModel>>> set = resources.entrySet();
		
		for (Entry<Class<?>, TemplateObjectModelFactory<? extends TemplateObjectModel>> e : set) {
			if (e.getValue().getClass().equals(c)) {
				set.remove(e);
			}
		}
	}

	private void addTemplateObjectModelFactory(Class<TemplateObjectModelFactory<? extends TemplateObjectModel>> c) {
		TemplateObjectModelFactory<? extends TemplateObjectModel> tmf = ClassUtils.createInstance(c);
		set(tmf.getObjectModelClass(), tmf);
	}

}
