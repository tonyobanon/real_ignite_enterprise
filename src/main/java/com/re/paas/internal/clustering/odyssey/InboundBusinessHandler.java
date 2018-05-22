package com.re.paas.internal.clustering.odyssey;

import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.clustering.ClusterFunction;
import com.re.paas.internal.clustering.functions.BaseFunction;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InboundBusinessHandler extends ChannelInboundHandlerAdapter {

	public static InboundBusinessHandler getInstance() {
		return new InboundBusinessHandler();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		Channel channel = ctx.channel();
		
		TransactionContext context = (TransactionContext) msg;
		byte[] data = context.getBytes();

		
		try {

			// Run function
			ClusterFunction.execute(context.getServerAddress(), BaseFunction.from(context.getFunctionId()), IOUtils.readObject(data), channel);
	
		} catch (Exception e) {
			channel.newFailedFuture(e);
			Exceptions.throwRuntime(e);
		}
		
		//Disconnect from channel
		if (channel.isOpen()) {
			channel.disconnect();
		}
	}

}
