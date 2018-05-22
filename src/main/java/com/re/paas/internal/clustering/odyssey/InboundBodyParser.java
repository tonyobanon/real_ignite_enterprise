package com.re.paas.internal.clustering.odyssey;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InboundBodyParser extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		// len = 18 --> [0] - [17]
		ByteBuf in = (ByteBuf) msg;
	
		String remoteAddress = ctx.channel().remoteAddress().toString();
		
		// Update Body Content
		boolean done = Sockets.ServerTransactionsRT.get(remoteAddress).add(in);
		
		if (done) {
			
			//Fire the business handler
			ctx.fireChannelRead(Sockets.ServerTransactionsRT.remove(remoteAddress));

		} else {
			// Do nothing, no channel handler is fired
		}

	}

	protected static InboundBodyParser getInstance() {
		return new InboundBodyParser();
	}

}
