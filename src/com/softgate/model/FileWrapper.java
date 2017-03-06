package com.softgate.model;

import java.io.File;

import javafx.beans.property.SimpleStringProperty;

public final class FileWrapper {
	
	private SimpleStringProperty name;
	
	private File file;
	
	public FileWrapper(File file) {		
		this.name = new SimpleStringProperty(file.getName());	
		this.file = file;
	}
	
	public String getName() {
		return name.get();
	}
	
	public SimpleStringProperty nameProperty() {
		return name;
	}
	
	public File getFile() {
		return file;
	}

}
