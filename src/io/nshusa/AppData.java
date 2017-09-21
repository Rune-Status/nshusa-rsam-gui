package io.nshusa;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import io.nshusa.meta.ArchiveMeta;
import javafx.scene.image.Image;

public final class AppData {

	public static final Path RESOURCE_PATH = Paths.get(System.getProperty("user.home") + File.separator + ".rsam");

	public static Image datIcon;

	public static Image idxIcon;

	public static Image txtIcon;

	public static Image fileStoreIcon;

	public static Image midiIcon;

	public static Image pngIcon;

	public static Image file32Icon;
	
	public static Image gzipIcon;
	
	public static Image addIcon;
	
	public static Image deleteIcon;

	public static Image clearIcon24;

	public static Image clearIcon16;

	public static Image saveIcon16;

	public static Image renameIcon16;

	public static Image import16Icon;

	public static Image openFolder16Icon;

	public static Image replace16Icon;

	public static Image identify16Icon;

	public static Image view16Icon;

	public static Image folder32Icon;

	public static Image checksum16Icon;

	public static final Map<Integer, String> storeNames = new HashMap<>();
	
	public static final Map<Integer, ArchiveMeta> archiveMetas = new HashMap<>();
	
	public static final Map<Integer, String> commonHashNames = new HashMap<>();

	private AppData() {

	}

	public static void load() {
		try {
			datIcon = new Image(App.class.getResourceAsStream("/icons/dat_icon.png"));
			idxIcon = new Image(App.class.getResourceAsStream("/icons/idx_icon.png"));
			txtIcon = new Image(App.class.getResourceAsStream("/icons/txt_icon.png"));
			fileStoreIcon = new Image(App.class.getResourceAsStream("/icons/file_store_icon.png"));
			midiIcon = new Image(App.class.getResourceAsStream("/icons/midi_icon.png"));
			pngIcon = new Image(App.class.getResourceAsStream("/icons/png_icon.png"));
			file32Icon = new Image(App.class.getResourceAsStream("/icons/file_32.png"));
			gzipIcon = new Image(App.class.getResourceAsStream("/icons/gzip_icon.png"));
			addIcon = new Image(App.class.getResourceAsStream("/icons/action_add.png"));
			deleteIcon = new Image(App.class.getResourceAsStream("/icons/action_delete.png"));
			clearIcon16 = new Image(App.class.getResourceAsStream("/icons/clear_icon_16.png"));
			clearIcon24 = new Image(App.class.getResourceAsStream("/icons/clear_icon_24.png"));
			saveIcon16 = new Image(App.class.getResourceAsStream("/icons/save_16.png"));
			renameIcon16 = new Image(App.class.getResourceAsStream("/icons/rename_16.png"));
			import16Icon = new Image(App.class.getResourceAsStream("/icons/import_16.png"));
			openFolder16Icon = new Image(App.class.getResourceAsStream("/icons/open_folder_16.png"));
			replace16Icon = new Image(App.class.getResourceAsStream("/icons/replace_16.png"));
			identify16Icon = new Image(App.class.getResourceAsStream("/icons/identify_16.png"));
			view16Icon = new Image(App.class.getResourceAsStream("/icons/view_16.png"));
			folder32Icon = new Image(App.class.getResourceAsStream("/icons/folder_32.png"));
			checksum16Icon = new Image(App.class.getResourceAsStream("/icons/checksum_16.png"));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Failed to load icons.");
		}
	}
	
	public static Image getIcon(String extension) {
		
		switch (extension) {

			case "gz":
				return gzipIcon;
		
			case "dat":
				return datIcon;
			
			case "idx":
				return idxIcon;

			case "jag":
				return folder32Icon;
				
			case "txt":
				return txtIcon;
				
			case "midi":
				return midiIcon;
		
			case "png":
				return pngIcon;

		}
		
		return file32Icon;
		
	}	

}
