package com.re.paas.internal.clustering.odyssey;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.clustering.events.NodeServerStartEvent;
import com.re.paas.internal.errors.ClusteringError;
import com.re.paas.internal.events.EventBus;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Sockets {

	// Server Channel Handlers
	protected static String INBOUND_FRAME_BUFFER = "INBOUND_FRAME_BUFFER";
	protected static String INBOUND_HEADER_PARSER = "INBOUND_HEADER_PARSER";
	protected static String INBOUND_BODY_PARSER = "INBOUND_BODY_PARSER";
	protected static String INBOUND_BUSINESS_HANDLER = "INBOUND_BUSINESS_HANDLER";

	// Client Channel Handlers
	protected static String OUTBOUND_REQUEST_HANDLER = "OUTBOUND_REQUEST_HANDLER";

	// K: Remote Address, V: TransactionContexts
	protected static Map<String, TransactionContext> ServerTransactionsRT = Collections
			.synchronizedMap(new HashMap<String, TransactionContext>());

	
	public static void newServer(InetAddress host, Integer port) {
		Thread serverThread = new Thread(() -> {
				newOdysseyServer(host, port);
		});
		serverThread.start();
	}

	private static void newOdysseyServer(InetAddress host, Integer port) {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(INBOUND_FRAME_BUFFER, InboundFrameBuffer.getInstance())
									.addLast(INBOUND_HEADER_PARSER, InboundHeaderParser.getInstance())
									.addLast(INBOUND_BODY_PARSER, InboundBodyParser.getInstance())
									.addLast(INBOUND_BUSINESS_HANDLER, InboundBusinessHandler.getInstance());
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			// Bind and start to accept incoming connections.
			ChannelFuture channelFuture = b.bind(host, port).sync().addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future) {

					// Post to Event Bus
					EventBus.dispatch(new NodeServerStartEvent(future.channel()));
				} 
			});

			// Wait until the server socket is closed.
			channelFuture.channel().closeFuture().sync();

		} catch (InterruptedException e) {
			Exceptions.throwRuntime(
					PlatformException.get(ClusteringError.ERROR_OCCURED_WHILE_STARTING_SERVER_SOCKET, host, port));
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}

	}
}
