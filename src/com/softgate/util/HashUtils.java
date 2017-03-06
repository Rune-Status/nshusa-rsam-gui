package com.softgate.util;

public final class HashUtils {
	
	private HashUtils() {
		
	}
	
	public static int nameToHash(String name) {
		int hash = 0;
		name = name.toUpperCase();
		for (int i = 0; i < name.length(); i++) {
			hash = (hash * 61 + name.charAt(i)) - 32;
		}
		return hash;
	}

}
