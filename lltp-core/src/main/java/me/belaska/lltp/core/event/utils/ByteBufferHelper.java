package me.belaska.lltp.core.event.utils;

import java.nio.ByteBuffer;

public class ByteBufferHelper {

	private ByteBufferHelper() {
	}

	public static String readString(ByteBuffer byteBuffer) {
		int len = byteBuffer.getInt();

		if (len < 0) {
			return null;
		}

		byte[] buf = new byte[len];
		byteBuffer.get(buf);

		return new String(buf);
	}

	public static void writeString(ByteBuffer byteBuffer, String value) {

		byte[] buf = value == null ? null : value.getBytes();

		int len = buf == null ? -1 : buf.length;

		byteBuffer.putInt(len);

		if (len > 0) {
			byteBuffer.put(buf);
		}
	}
}
