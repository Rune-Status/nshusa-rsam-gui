package io.nshusa;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import io.nshusa.model.ArchiveMeta;
import javafx.scene.image.Image;

public final class AppData {
	
	public static final Path resourcePath = Paths.get(System.getProperty("user.home") + File.separator + ".rsam");
	
	public static final Path storeResourcePath = resourcePath.resolve("stores.txt");
	
	public static final Path archiveResourcePath = resourcePath.resolve("archives.txt");
	
	public static final Path hashResourcePath = resourcePath.resolve("hashes.txt");
	
	public static final Image indexIcon = new Image(App.class.getResourceAsStream("/images/index_icon.png"));

	public static final Image datIcon = new Image(App.class.getResourceAsStream("/images/dat_icon.png"));

	public static final Image idxIcon = new Image(App.class.getResourceAsStream("/images/idx_icon.png"));

	public static final Image textIcon = new Image(App.class.getResourceAsStream("/images/text_icon.png"));

	public static final Image midiIcon = new Image(App.class.getResourceAsStream("/images/midi_icon.png"));

	public static final Image pngIcon = new Image(App.class.getResourceAsStream("/images/dat_icon.png"));

	public static final Image fileIcon = new Image(App.class.getResourceAsStream("/images/file_icon.png"));
	
	public static final Image gzipIcon = new Image(App.class.getResourceAsStream("/images/gzip_icon.png"));
	
	public static final Image addIcon = new Image(App.class.getResourceAsStream("/images/action_add.png"));
	
	public static final Image deleteIcon = new Image(App.class.getResourceAsStream("/images/action_delete.png"));
	
	public static final Image exportFileIcon16 = new Image(App.class.getResourceAsStream("/images/export_file_icon_16.png"));
	
	public static final Image clearIcon24 = new Image(App.class.getResourceAsStream("/images/clear_icon_24.png"));

	public static final Image clearIcon16 = new Image(App.class.getResourceAsStream("/images/clear_icon_16.png"));
	
	private AppData() {
		
	}
	
	public static final Map<Integer, String> storeNames = new HashMap<>();
	
	public static final Map<Integer, ArchiveMeta> archiveMetas = new HashMap<>();
	
	public static final Map<Integer, String> commonHashNames = new HashMap<>();
	
	public static Image getIcon(String extension) {
		
		switch (extension) {		
		
			case "dat":
				return datIcon;
			
			case "idx":
				return idxIcon;
				
			case "txt":
				return textIcon;
				
			case "midi":
				return midiIcon;
		
			case "png":
				return pngIcon;
				
			case "gz":
				return gzipIcon;

		}
		
		return fileIcon;
		
	}	

}
