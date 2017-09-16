package io.nshusa.meta;

import java.util.Objects;

public class ArchiveMeta {

    private final int id;

    private final String displayName;

    private final String fileName;

    private final boolean imageArchive;

    public ArchiveMeta(int id, String displayName, String fileName, boolean isImageArchive) {
        this.id = id;
        this.displayName = displayName;
        this.fileName = fileName;
        this.imageArchive = isImageArchive;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isImageArchive() {
        return imageArchive;
    }

    public String getExtension() {
        return fileName.lastIndexOf(".") != -1 ? fileName.substring(fileName.lastIndexOf(".") + 1 ,fileName.length()) : "dat";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, fileName, imageArchive);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ArchiveMeta) {
            ArchiveMeta meta = (ArchiveMeta) o;
            return meta.hashCode() == hashCode();
        }

        return false;
    }

}
