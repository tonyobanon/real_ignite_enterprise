package com.re.paas.internal.clustering.odyssey;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.clustering.ClusterFunction;
import com.re.paas.internal.clustering.ClusterNode;
import com.re.paas.internal.clustering.Function;
import com.re.paas.internal.clustering.NodeRequest;
import com.re.paas.internal.clustering.functions.BaseFunction;
import com.re.paas.internal.core.cron.Scheduler;
import com.re.paas.internal.errors.ClusteringError;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientBuilder<P, R> {

	private CompletableFuture<R> execute(Function function, P parameter) {
		
		final CompletableFuture<R> completableFuture = new CompletableFuture<R>();

		ClusterFunction<P, R> clusterFunction = ((ClusterFunction<P, R>) ClusterFunction.get(function));

		if (function.isAsync()) {

			Scheduler.now(() -> {
					R result = clusterFunction.delegate(parameter);
					completableFuture.complete(result);
			});

			return completableFuture;

		} else {

			R result = clusterFunction.delegate(parameter);
			completableFuture.complete(result);
			return completableFuture;
		}

	}
	
	public CompletableFuture<R> build(InetAddress host, Integer port, BaseFunction function, P parameter,
			Integer pThreshold) {

		final CompletableFuture<R> completableFuture = new CompletableFuture<R>();

		if (ClusterNode.getClusteringAddress().equals(host)) {
			return execute(function, parameter);
		}

		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {

			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {

					//Logger.debug("Making request to: " + host.getHostAddress());

					InetAddress serverAddress = null;
					
					if(parameter instanceof NodeRequest) {
						serverAddress = ClusterNode.getClusteringAddress();
					}
					
					ch.pipeline().addLast(Sockets.OUTBOUND_REQUEST_HANDLER, new ClientOutboundRequestHandler<R>(
							serverAddress, function.getValue(), parameter, pThreshold, completableFuture));
				}

				@Override
				public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
					Exceptions.throwRuntime(cause);
				}
			});

			
			// Start the client.
			ChannelFuture f = b.connect(new InetSocketAddress(host, port)).sync().addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future) {

				}
			});

			if (!function.isAsync()) {
				// Wait until the connection is closed.
				f.channel().closeFuture().sync();
			}

		} catch (InterruptedException e) {

			Exceptions.throwRuntime(
					PlatformException.get(ClusteringError.ERROR_OCCURED_WHILE_STARTING_CLIENT_SOCKET, host, port));
		} finally {
			workerGroup.shutdownGracefully();
		}

		return completableFuture;
	}

	public CompletableFuture<R> build(InetAddress host, Integer port, BaseFunction function, P parameter) {
		return build(host, port, function, parameter, -1);
	}

}
