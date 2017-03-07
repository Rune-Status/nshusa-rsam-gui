package com.softgate.util;

import java.text.DecimalFormat;

import com.softgate.AppData;
import com.softgate.fs.ArchiveEntry;

public final class StringUtils {
	
	private StringUtils() {
		
	}
	
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	public static String getCommonName(ArchiveEntry entry) {
		return AppData.commonHashNames.containsKey(entry.getHash()) ? AppData.commonHashNames.get(entry.getHash())
				: Integer.toString(entry.getHash());
	}

}
