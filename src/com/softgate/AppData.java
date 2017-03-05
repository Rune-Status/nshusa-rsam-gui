package com.softgate;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class AppData {
	
	public static final Path resourcePath = Paths.get(System.getProperty("user.home") + File.separator + ".rsam");
	
	public static final Path storeResourcePath = resourcePath.resolve("stores.txt");
	
	public static final Path archiveResourcePath = resourcePath.resolve("archives.txt");
	
	private AppData() {
		
	}
	
	public static final Map<Integer, String> storeNames = new HashMap<>();
	
	public static final Map<Integer, String> archiveNames = new HashMap<>();

}
