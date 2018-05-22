package com.re.paas.internal.clustering.odyssey;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.re.paas.internal.base.core.Exceptions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

public class IOUtils {

	public static void writeAndFlush(Channel channel, Object responseBody) {
		writeAndFlush(channel, responseBody, -1);
	}

	public static Object readObject(final byte[] data) {

		try {

			ObjectInputStream in = new ObjectInputStream(new InputStream() {

				int start = -1;
				int end = data.length - 1;

				@Override
				public int read() throws IOException {

					if (start < end) {
						start++;
						return data[start];
					}

					return -1;
				}
			});

			Object o = in.readObject();

			in.close();

			return o;

		} catch (Exception e) {
			return Exceptions.throwRuntime(e);
		}
	}

	public static void writeAndFlush(Channel channel, Object responseBody, Integer responseBodyThreshold) {

		try {

			if (responseBody != null) {

				ByteBuf buf = responseBodyThreshold == -1 ? ByteBufAllocator.DEFAULT.directBuffer()
						: ByteBufAllocator.DEFAULT.directBuffer(responseBodyThreshold);

				// Buffer responseBody
				ObjectOutputStream stream = new ObjectOutputStream(new OutputStream() {
					@Override
					public void write(int b) throws IOException {
						buf.writeByte(b);
					}
				});

				// Write to buf
				stream.writeObject(responseBody);

				// close object stream
				stream.close();

				// content length
				channel.write(Unpooled.copyInt(buf.readableBytes()));

				// send bytes
				channel.write((ByteBuf) buf.retain());

				// release buffer
				buf.release();

			} else {

				// content length
				channel.write(Unpooled.copyInt(0));
			}

			// flush channel
			channel.flush();

			// close channel
			channel.close();

		} catch (IOException e) {
			Exceptions.throwRuntime(e);

		}
	}
}
