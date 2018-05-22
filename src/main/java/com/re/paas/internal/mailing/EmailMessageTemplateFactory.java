package com.re.paas.internal.mailing;

import com.re.paas.internal.base.AppDelegate;
import com.re.paas.internal.templating.api.TemplateObjectModelFactory;

public class EmailMessageTemplateFactory extends TemplateObjectModelFactory<EmailMessageTemplate> {

	@Override
	public EmailMessageTemplate create(EmailMessageTemplate template) {

		/*
		template
		.setCompanyName(ConfigModel.get(ConfigKeys.ORGANIZATION_NAME))
		.setSoftwareVendorName(AppDelegate.SOFTRWARE_VENDER_EMAIL)
		.setSoftwareVendorUrl(AppDelegate.SOFTRWARE_VENDER_NAME);
		*/
		
		template
		.setCompanyName("Compute Essentials Technologies")
		.setCompanyAddress("34, Victoria Street, Ojota, Lagos")
		.setSoftwareVendorName(AppDelegate.SOFTRWARE_VENDER_EMAIL)
		.setSoftwareVendorUrl(AppDelegate.SOFTRWARE_VENDER_NAME);
		
		return template;
	}

}
