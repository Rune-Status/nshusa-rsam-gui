package io.nshusa.model;

import io.nshusa.AppData;
import io.nshusa.rsam.binary.Archive;
import io.nshusa.util.StringUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.ImageView;

public final class ArchiveEntryWrapper {
	
	private ObservableValue<Integer> hash;
	
	private SimpleStringProperty name;
	
	private SimpleStringProperty extension;
	
	private ObservableValue<String> size;
	
	private ImageView image;	
	
	public ArchiveEntryWrapper(Archive.ArchiveEntry entry) {
		this.hash = new SimpleIntegerProperty(entry.getHash()).asObject();
		this.name = new SimpleStringProperty(StringUtils.getCommonName(entry).contains(".") ? StringUtils.getCommonName(entry).substring(0, StringUtils.getCommonName(entry).lastIndexOf(".")) : StringUtils.getCommonName(entry));
		this.extension = new SimpleStringProperty(StringUtils.getCommonName(entry).contains(".") ? StringUtils.getCommonName(entry).substring(StringUtils.getCommonName(entry).lastIndexOf(".") + 1, StringUtils.getCommonName(entry).length()) : "none");
		this.size = new SimpleStringProperty(StringUtils.readableFileSize(entry.getCompresseedSize()));
		this.image = new ImageView(AppData.getIcon(StringUtils.getCommonName(entry).contains(".") ? StringUtils.getCommonName(entry).substring(StringUtils.getCommonName(entry).lastIndexOf(".") + 1, StringUtils.getCommonName(entry).length()) : "none"));
	}
	
	public ObservableValue<Integer> idProperty() {
		return hash;
	}
	
	public int getHash() {
		return hash.getValue();
	}
	
	public SimpleStringProperty nameProperty() {
		return name;
	}
	
	public String getName() {
		return name.get();
	}
	
	public SimpleStringProperty getExtensionProperty() {
		return extension;
	}
	
	public String getExtension() {
		return extension.get();
	}
	
	public String getSize() {
		return size.getValue();
	}
	
	public void setIcon(ImageView icon) {
		this.image = icon;
	}
	
	public ImageView getImage() {
		return image;
	}

	public ObservableValue<String> sizeProperty() {
		return size;
	}

}
