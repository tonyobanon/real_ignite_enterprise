package com.re.paas.internal.clustering;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.clustering.functions.BaseFunction;
import com.re.paas.internal.clustering.odyssey.IOUtils;
import com.re.paas.internal.spi.ClassIdentityType;
import com.re.paas.internal.spi.ClasspathScanner;
import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.spi.SpiTypes;
import com.re.paas.internal.utils.ClassUtils;

import io.netty.channel.Channel;

public abstract class ClusterFunction<P, R> {

	private Channel channel;

	private static Map<Short, ClusterFunction<Object, Object>> functions = Collections
			.synchronizedMap(new HashMap<Short, ClusterFunction<Object, Object>>(32767));

	public static void scanAll() {

		new SpiDelegate<ClusterFunction<Object, Object>>(SpiTypes.CLUSTER_FUNCTION).get(c -> {
			ClusterFunction<Object, Object> o = ClassUtils.createInstance(c);

			if (o.isJoinWorkflowApplicable()) {
				functions.put(o.id().getValue(), o);
			}
		});
	}

	private boolean isJoinWorkflowApplicable() {

		Role current = CloudEnvironment.get().joinWorkflow().getWorkflow().getRole();
		boolean found = false;

		for (Role role : roles()) {
			found = role == current;
			if (found)
				break;
		}

		return found;
	}

	public abstract Role[] roles();

	public abstract BaseFunction id();

	private Channel getChannel() {
		return channel;
	}

	private ClusterFunction<P, R> setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}

	private final void response(R t) {
		IOUtils.writeAndFlush(getChannel(), t);
	}

	private final void accept(P t) {
		response(delegate(t));
	}

	public abstract R delegate(P t);

	public static void execute(InetAddress serverAddress, Function function, Object parameter, Channel channel) {

		if (parameter instanceof NodeRequest && serverAddress != null) {
			((NodeRequest) parameter).setRemoteAddress(serverAddress.getHostAddress());
		}

		functions.get(function.getValue()).setChannel(channel).accept(parameter);
	}

	public static ClusterFunction<Object, Object> get(Function function) {
		return functions.get(function.getValue());
	}

}
