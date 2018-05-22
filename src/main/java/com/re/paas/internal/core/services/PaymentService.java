package com.re.paas.internal.core.services;

import com.re.paas.internal.core.fusion.BaseService;
import com.re.paas.internal.core.fusion.FusionEndpoint;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.BasePaymentModel;

import io.vertx.ext.web.RoutingContext;

public class PaymentService extends BaseService {
	  @Override
	public String uri() {
	// TODO Auto-generated method stub
	return "/payment-service";
	}
		@FusionEndpoint(uri = BasePaymentModel.IPN_CALLBACK_URL , functionality = Functionality.MANAGE_ACTIVITY_STREAM,
				createXhrClient = false)
		public void notificationHook(RoutingContext ctx) {
			
		}
}
