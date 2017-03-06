package com.softgate.util;

import java.nio.ByteBuffer;

public final class BufferUtils {
	
	private BufferUtils() {
		
	}
	
	public static void putMedium(ByteBuffer buffer, int value) {
		buffer.put((byte) (value >> 16)).put((byte) (value >> 8)).put((byte) value);
	}
	
	public static int getUMedium(ByteBuffer buffer) {
		return (buffer.getShort() & 0xFFFF) << 8 | buffer.get() & 0xFF;
	}
	
	public static int getUShort(ByteBuffer buffer) {
		return buffer.getShort() & 0xffff;
	}

}
