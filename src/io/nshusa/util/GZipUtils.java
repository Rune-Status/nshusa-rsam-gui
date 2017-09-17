package io.nshusa.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

public final class GZipUtils {
	
	private GZipUtils() {
		
	}

	public static boolean isGZipped(byte[] bytes) {
		if ((bytes == null) || (bytes.length < 2)) {
			return false;
		} else {
			return ((bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
					&& (bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8)));
		}
	}

	public static boolean isGZipped(File file) {
		int magic = 0;

		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}
		return magic == GZIPInputStream.GZIP_MAGIC;
	}

}
