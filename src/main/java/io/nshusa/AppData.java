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

	public static final Path archiveResourcePath = resourcePath.resolve("archives.json");
	
	public static final Path hashResourcePath = resourcePath.resolve("hashes.txt");

	public static final Image datIcon = new Image(App.class.getResourceAsStream("/icons/dat_icon.png"));

	public static final Image idxIcon = new Image(App.class.getResourceAsStream("/icons/idx_icon.png"));

	public static final Image txtIcon = new Image(App.class.getResourceAsStream("/icons/txt_icon.png"));

	public static final Image fileStoreIcon = new Image(App.class.getResourceAsStream("/icons/file_store_icon.png"));

	public static final Image midiIcon = new Image(App.class.getResourceAsStream("/icons/midi_icon.png"));

	public static final Image pngIcon = new Image(App.class.getResourceAsStream("/icons/png_icon.png"));

	public static final Image fileIcon = new Image(App.class.getResourceAsStream("/icons/file_icon.png"));
	
	public static final Image gzipIcon = new Image(App.class.getResourceAsStream("/icons/gzip_icon.png"));
	
	public static final Image addIcon = new Image(App.class.getResourceAsStream("/icons/action_add.png"));
	
	public static final Image deleteIcon = new Image(App.class.getResourceAsStream("/icons/action_delete.png"));

	public static final Image clearIcon24 = new Image(App.class.getResourceAsStream("/icons/clear_icon_24.png"));

	public static final Image clearIcon16 = new Image(App.class.getResourceAsStream("/icons/clear_icon_16.png"));

	public static final Image saveIcon16 = new Image(App.class.getResourceAsStream("/icons/save_16.png"));

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
				return txtIcon;
				
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
