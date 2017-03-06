package com.softgate.model;

import com.softgate.AppData;
import com.softgate.fs.ArchiveEntry;
import com.softgate.util.StringUtils;

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
	
	public ArchiveEntryWrapper(ArchiveEntry entry) {
		this.hash = new SimpleIntegerProperty(entry.getHash()).asObject();
		this.name = new SimpleStringProperty(entry.getCommonName().contains(".") ? entry.getCommonName().substring(0, entry.getCommonName().lastIndexOf(".")) : entry.getCommonName());
		this.extension = new SimpleStringProperty(entry.getCommonName().contains(".") ? entry.getCommonName().substring(entry.getCommonName().lastIndexOf(".") + 1, entry.getCommonName().length()) : "none");
		this.size = new SimpleStringProperty(StringUtils.readableFileSize(entry.getCompresseedSize()));
		this.image = new ImageView(AppData.getIcon(entry.getCommonName().contains(".") ? entry.getCommonName().substring(entry.getCommonName().lastIndexOf(".") + 1, entry.getCommonName().length()) : "none"));
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
