package io.softgate.model;

import io.softgate.AppData;
import io.softgate.util.StringUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.ImageView;

public class StoreEntryWrapper {
	
	private ObservableValue<Integer> id;
	
	private SimpleStringProperty name;
	
	private SimpleStringProperty extension;
	
	private ObservableValue<String> size;
	
	private ImageView image;	
	
	public StoreEntryWrapper(int id, String name, int size) {
		this.id = new SimpleIntegerProperty(id).asObject();
		this.name = new SimpleStringProperty(name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name);
		this.extension = new SimpleStringProperty(name.contains(".") ? name.substring(name.lastIndexOf(".") + 1, name.length()) : "none");
		this.size = new SimpleStringProperty(StringUtils.readableFileSize(size));
		this.image = new ImageView(AppData.getIcon(name.contains(".") ? name.substring(name.lastIndexOf(".") + 1, name.length()) : "none"));
	}
	
	public ObservableValue<Integer> idProperty() {
		return id;
	}
	
	public int getId() {
		return id.getValue();
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
