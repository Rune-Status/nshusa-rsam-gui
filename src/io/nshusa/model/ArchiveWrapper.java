package io.nshusa.model;

import io.nshusa.AppData;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public final class ArchiveWrapper {
	
	private SimpleStringProperty name;
	
	private SimpleIntegerProperty id;
	
	public ArchiveWrapper(int id) {	
		this.id = new SimpleIntegerProperty(id);
		this.name = new SimpleStringProperty(AppData.archiveMetas.get(id) == null ? Integer.toString(id) : AppData.archiveMetas.get(id).getDisplayName());
	}
	
	public int getId() {
		return id.get();
	}
	
	public SimpleIntegerProperty idProperty() {
		return id;
	}
	
	public String getName() {
		return name.get();
	}
	
	public SimpleStringProperty nameProperty() {
		return name;
	}

}
