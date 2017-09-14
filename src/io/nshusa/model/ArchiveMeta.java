package io.nshusa.model;

import java.util.Objects;

public class ArchiveMeta {
	
	private final int id;
	
	private final String name;
	
	private final boolean imageArchive;

	public ArchiveMeta(int id, String name, boolean imageArchive) {
		this.id = id;
		this.name = name;
		this.imageArchive = imageArchive;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isImageArchive() {
		return imageArchive;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, name, imageArchive);
	}
	
	public boolean equals(Object o) {
		if (o instanceof ArchiveMeta) {
			ArchiveMeta meta = (ArchiveMeta) o;
			
			return meta.hashCode() == hashCode();
		}
		
		return false;
	}
	
}
