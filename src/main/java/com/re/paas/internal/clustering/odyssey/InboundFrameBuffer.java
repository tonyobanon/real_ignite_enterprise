package com.re.paas.internal.clustering.odyssey;

import java.net.InetAddress;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class InboundFrameBuffer extends ByteToMessageDecoder {

	public static InboundFrameBuffer getInstance() {
		return new InboundFrameBuffer();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

		String remoteAddress = ctx.channel().remoteAddress().toString();

		// Create new Transaction Context
		Sockets.ServerTransactionsRT.put(remoteAddress, new TransactionContext());
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		String remoteAddress = ctx.channel().remoteAddress().toString();

		TransactionContext tCtx = Sockets.ServerTransactionsRT.get(remoteAddress);

		if (tCtx.getServerAddressBuf() != null && tCtx.getServerAddress() == null) {

			while (in.readableBytes() > 0 && tCtx.getServerAddressBuf().readableBytes() < 4) {
				byte b = in.readByte();
				tCtx.getServerAddressBuf().writeByte(b);
			}

			if (tCtx.getServerAddressBuf().readableBytes() == 4) {

				if (tCtx.getServerAddressBuf().getInt(0) == -1) {
					tCtx.getServerAddressBuf().readBytes(4);
					tCtx.setServerAddress((InetAddress) null);
				} else {
					tCtx.setServerAddress(tCtx.getServerAddressBuf().readBytes(4));
				}
			}

		} else {

			if (in.readableBytes() < Constants.SOCKET_BUFFER_TOTAL_SIZE) {
				return;
			}
			out.add(in.readBytes(Constants.SOCKET_BUFFER_TOTAL_SIZE));
		}

	}

}
