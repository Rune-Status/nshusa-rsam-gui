package io.nshusa.meta;

public class ArchiveMeta {

    private final int id;

    private final String name;

    private final boolean isImageArchive;

    public ArchiveMeta(int id, String name, boolean isImageArchive) {
        this.id = id;
        this.name = name;
        this.isImageArchive = isImageArchive;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isImageArchive() {
        return isImageArchive;
    }

}
