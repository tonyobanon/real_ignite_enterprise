package com.re.paas.internal.clustering.odyssey;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.clustering.classes.Conditional;
import com.re.paas.internal.utils.ObjectUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * This class instruments all outgoing connections leaving this node, to another
 * node in the cluster. This is the reference client-side implementation of
 * Odyssey TCP protocol
 * 
 * @author Tony
 */
public class ClientOutboundRequestHandler<R> extends ChannelInboundHandlerAdapter {

	private ByteBuf responseLengthBuffer;
	private int responseLength = -1;

	private ResponseReader responseReader;
	private Thread responseReaderThread;

	private final Short functionId;
	private final Object requestBody;
	private final Integer requestBodyThreshold;

	private final CompletableFuture<R> future;
	private final InetAddress serverAddress;

	// @DEV
	long startTime;
	long endTime;

	protected ClientOutboundRequestHandler(InetAddress serverAddress, Short functionId, Object requestBody,
			Integer requestBodyThreshold, CompletableFuture<R> future) throws IOException {

		super();

		this.serverAddress = serverAddress;
		this.functionId = functionId;
		this.requestBody = requestBody;
		this.requestBodyThreshold = requestBodyThreshold;

		this.future = future;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		responseLengthBuffer = ByteBufAllocator.DEFAULT.directBuffer(4);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		sendRequest(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {

		startTime = System.nanoTime();

		ByteBuf buf = (ByteBuf) msg;

		Conditional hasContentLength = responseLength != -1 ? Conditional.TRUE : Conditional.FALSE;

		switch (hasContentLength) {

		case FALSE:

			int i = responseLengthBuffer.readableBytes();

			while (i < 4 && buf.readableBytes() > 0) {
				responseLengthBuffer.writeByte(buf.readByte());
				i++;
			}

			if (i < 4) {
				// No more bytes in buf to read
				buf.release();
				return;
			}

			// There are still more bytes to read
			responseLength = responseLengthBuffer.readInt();
			responseLengthBuffer.release();

			if (responseLength == 0) {
				future.complete(null);
				return;
			}

			// Setup our object stream
			setupStream(ctx);

			// Fall-through, to the next case

		case TRUE:

			// Add more bytes
			responseReader.update(buf);

			// Release buffer
			buf.clear();

			// Notify our ResponseReader
			responseReaderThread.interrupt();

			break;
		}
	}

	/**
	 * 
	 * This sets up the stream used to transform raw bytes into a POJO of Type T.
	 * Internally, this creates a nexus to the ResponseReader class that returns
	 * bytes of data as they become available from the server
	 * 
	 * After all bytes have been read, the future successfully completes.
	 * 
	 **/
	private void setupStream(ChannelHandlerContext ctx) {
		responseReader = this.new ResponseReader(responseLength);

		responseReaderThread = new Thread(() -> {
			try {
				ObjectInputStream responseStream = new ObjectInputStream(responseReader);
				Object o = responseStream.readObject();
				responseStream.close();

				// @DEV
				endTime = System.nanoTime();
				long timeElapsed = (endTime - startTime) / 1000;

				Logger.get().debug("Time elapsed for request: " + timeElapsed + " microseconds");

				// Call future callback
				future.complete((R) o);

			} catch (IOException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		});
		responseReaderThread.start();
	}

	class ResponseReader extends InputStream {

		private final ByteBuf buf;

		private final int totalSize;
		private int currentIndex;

		public ResponseReader(int bufSize) {
			totalSize = bufSize;
			buf = ByteBufAllocator.DEFAULT.directBuffer(bufSize);
		}

		@Override
		public int read() throws IOException {

			if (buf.isReadable()) {

				byte b = buf.readByte();
				currentIndex++;
				return b;

			} else {

				if (currentIndex >= totalSize) {

					// All bytes have been read
					buf.release();
					return -1;
				} else {

					try {
						// Wait for more data
						synchronized (this) {
							while (true)
								wait();
						}

					} catch (InterruptedException e) {
						// Yeah!, more data is now available
						return read();
					}

				}

			}

		}

		public void update(ByteBuf buf) {
			this.buf.writeBytes(buf);
		}

	}

	private void sendRequest(Channel channel) throws IOException {

		ByteBuf buf = requestBodyThreshold == -1 ? ByteBufAllocator.DEFAULT.directBuffer()
				: ByteBufAllocator.DEFAULT.directBuffer(requestBodyThreshold);

		// Buffer requestBody
		ObjectOutputStream stream = new ObjectOutputStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				buf.writeByte(b);
			}
		});

		// Write to buf
		stream.writeObject(requestBody);

		// close object stream
		stream.close();
		
		byte[] serverAddress = 	this.serverAddress != null ? this.serverAddress.getAddress() : ObjectUtils.toByteArray(-1);
		
		channel.write(Unpooled.copiedBuffer(serverAddress));

		// start headers

		channel.write(Unpooled.copyShort(Constants.HS1));
		channel.write(Unpooled.copyShort(Constants.HS2));
		channel.write(Unpooled.copyShort(Constants.HS3));

		// content length
		int length = buf.readableBytes();
		channel.write(Unpooled.copyInt(length));

		// function id
		channel.write(Unpooled.copyShort(functionId));

		// end headers
		channel.write(Unpooled.copyShort(Constants.HE1));
		channel.write(Unpooled.copyShort(Constants.HE2));
		channel.write(Unpooled.copyShort(Constants.HE3));
		

		
		// segment bytes
		int segments = length / (Constants.SOCKET_BUFFER_TOTAL_SIZE/* - 4*/);

		if (length % (Constants.SOCKET_BUFFER_TOTAL_SIZE/* - 4*/) > 0) {
			segments += 1;
		}

		for (int i = 0; i < segments; i++) {

			// Segment N

			int endIndex = (Constants.SOCKET_BUFFER_TOTAL_SIZE/* - 4*/) * (i + 1);
			int currentIndex = endIndex - (Constants.SOCKET_BUFFER_TOTAL_SIZE/* - 4*/);

			// Segment buffer
			ByteBuf cb = Unpooled.directBuffer(Constants.SOCKET_BUFFER_TOTAL_SIZE/* - 4*/);

			if (i <= segments - 2) {

				// i is a proper segment index

				while (currentIndex < endIndex) {
					byte b = buf.readByte();
					cb.writeByte(b);
					currentIndex++;
				}

			} else {

				boolean isBufferReadable = true;

				while (currentIndex < endIndex) {

					// i is a supplementary segment index

					if (!isBufferReadable) {
						// write random byte to complete segment
						cb.writeByte(0);
					} else {

						if (buf.isReadable()) {
							byte b = buf.readByte();
							cb.writeByte(b);
						} else {
							// write random byte to complete segment
							cb.writeByte(0);
							isBufferReadable = false;
						}
					}

					currentIndex++;
				}

			}
			
			// Write segment contents 
			channel.write(cb.retain());

			cb.release();
		}

		// flush all messages
		channel.flush();

		// release buffer
		buf.release();
	}

}
