package com.softgate.util;

import java.util.zip.GZIPInputStream;

/**
 * A static-utility class that contains useful methods for GZip.
 * 
 * @author Freyr
 */
public final class GZipUtils {
	
	private GZipUtils() {
		
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
	public static boolean isGZipped(byte[] bytes) {
		if ((bytes == null) || (bytes.length < 2)) {
			return false;
		} else {
			return ((bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
					&& (bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)));
		}
	}

}
