package com.softgate.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

public final class FileUtils {

	private FileUtils() {

	}

	public static byte[] readFile(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}

	/**
	 * Determines if a byte array is compressed. The java.util.zip GZip
	 * implementaiton does not expose the GZip header so it is difficult to
	 * determine if a string is compressed.
	 * 
	 * @param bytes an array of bytes
	 * 
	 * @return true if the array is compressed or false otherwise
	 * 
	 * @throws java.io.IOException if the byte array couldn't be read
	 */
	public static boolean isCompressed(byte[] bytes) {
		if ((bytes == null) || (bytes.length < 2)) {
			return false;
		} else {
			return ((bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
					&& (bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)));
		}
	}

}
