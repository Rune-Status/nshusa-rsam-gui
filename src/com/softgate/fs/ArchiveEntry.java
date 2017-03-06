package com.softgate.fs;

import com.softgate.AppData;

public final class ArchiveEntry {
	
	private final int hash;
	private final int uncompressedSize;			
	private final int compressedSize;		
	private final byte[] bzipped;		
	
	public ArchiveEntry(int hash, int uncompressedSize, int compressedSize, byte[] bzipped) {
		this.hash = hash;
		this.uncompressedSize = uncompressedSize;
		this.compressedSize = compressedSize;
		this.bzipped = bzipped;
	}
	
	public int getHash() {
		return hash;
	}

	public int getUncompressedSize() {
		return uncompressedSize;
	}

	public int getCompresseedSize() {			
		return compressedSize;
	}
	
	public String getCommonName() {
		return AppData.commonHashNames.containsKey(hash) ? AppData.commonHashNames.get(hash)
				: Integer.toString(hash);
	}

	public byte[] getData() {
		return bzipped;
	}
	
}
