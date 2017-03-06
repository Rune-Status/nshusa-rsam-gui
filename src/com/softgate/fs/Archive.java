package com.softgate.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.softgate.util.BufferUtils;
import com.softgate.util.CompressionUtil;
import com.softgate.util.HashUtils;

public final class Archive {

	private boolean extracted;	
	
	private final List<ArchiveEntry> entries = new ArrayList<>();
	
	private Archive() {		

	}
	
	public static Archive create() {
		Archive archive = new Archive();
		
		return archive;
	}
	
	public static Archive decode(byte[] data) throws IOException {
		Archive archive = new Archive();
		
		ByteBuffer archiveBuf = ByteBuffer.wrap(data);		
		
		int uncompressedSize = BufferUtils.getUMedium(archiveBuf);			
		int compressedSize = BufferUtils.getUMedium(archiveBuf);		
		
		if (uncompressedSize != compressedSize) {
			byte[] tmp = new byte[uncompressedSize];
			CompressionUtil.debzip2(data, tmp);
			data = tmp;
			archiveBuf = ByteBuffer.wrap(data);
			archive.extracted = true;
		} else {
			archive.extracted = false;
		}
		
		int entries = BufferUtils.getUShort(archiveBuf);
		
		ByteBuffer entryBuf = ByteBuffer.wrap(data);
		
		entryBuf.position(archiveBuf.position() + entries * 10);
		
		int[] hashes = new int[entries];
		int[] decompressedSizes = new int[entries];
		int[] compressedSizes = new int[entries];
		
		for (int i = 0; i < entries; i++) {		
			
			hashes[i] = archiveBuf.getInt();
			decompressedSizes[i] = BufferUtils.getUMedium(archiveBuf);			
			compressedSizes[i] = BufferUtils.getUMedium(archiveBuf);
			
			byte[] compressed = new byte[compressedSizes[i]];
			
			ArchiveEntry entry = new ArchiveEntry(hashes[i], decompressedSizes[i], compressedSizes[i], compressed);			
			
			entryBuf.get(compressed, 0, entry.getCompresseedSize());
			
			archive.entries.add(entry);
		}
		
		return archive;
	}
	
	public synchronized byte[] encode() throws IOException {		
		
		int size = 2 + entries.size() * 10;
		
		for (ArchiveEntry file : entries) {			
			size += file.getCompresseedSize();
		}
		
		ByteBuffer buf;
		if (!extracted) {
			buf = ByteBuffer.allocate(size + 6);
			BufferUtils.putMedium(buf, size);
			BufferUtils.putMedium(buf, size);
		} else {
			buf = ByteBuffer.allocate(size);
		}
		
		buf.putShort((short)entries.size());
		
		for (ArchiveEntry entry : entries) {			
			buf.putInt(entry.getHash());
			BufferUtils.putMedium(buf, entry.getUncompressedSize());
			BufferUtils.putMedium(buf, entry.getCompresseedSize());
		}
		
		for (ArchiveEntry file : entries) {
			buf.put(file.getData(), 0, file.getCompresseedSize());
		}
		
		byte[] data;
		if (!extracted) {
			data = buf.array();
		} else {
			byte[] unzipped = buf.array();
			byte[] zipped = CompressionUtil.bzip2(unzipped);
			if (unzipped.length == zipped.length) {
				throw new RuntimeException("error zipped size matches original");
			}
			buf = ByteBuffer.allocate(zipped.length + 6);
			BufferUtils.putMedium(buf, unzipped.length);
			BufferUtils.putMedium(buf, zipped.length);
			buf.put(zipped, 0, zipped.length);
			data = buf.array();
		}
		
		return data;
		
	}
	
	public byte[] readFile(String name) {
		return readFile(HashUtils.nameToHash(name));
	}
	
	public byte[] readFile(int hash) {
		for (ArchiveEntry entry : entries) {
			if (entry.getHash() == hash) {
				return entry.getData();
			}
		}
		
		return null;
	}
	
	public ArchiveEntry getEntry(String name) {
		return getEntry(HashUtils.nameToHash(name));
	}
	
	public ArchiveEntry getEntry(int hash) {
		for (ArchiveEntry entry : entries) {
			if (entry.getHash() == hash) {
				return entry;
			}
		}
		return null;
	}
	
	public int indexOf(String name) {
		return indexOf(HashUtils.nameToHash(name));
	}
	
	public int indexOf(int hash) {
		for (int i = 0; i < entries.size(); i++) {
			ArchiveEntry entry = entries.get(i);			
			
			if (entry.getHash() == hash) {
				return i;
			}			
		}
		
		return -1;
	}
	
	public boolean contains(String name) {
		return contains(HashUtils.nameToHash(name));
	}
	
	public boolean contains(int hash) {
		for (ArchiveEntry entry : entries) {
			if (entry.getHash() == hash) {
				return true;
			}
		}
		return false;
	}
	
	public void remove(String name) {
		remove(HashUtils.nameToHash(name));
	}
	
	public void remove(int hash) {		
		for (int i = 0; i < entries.size(); i++) {
			ArchiveEntry entry = entries.get(i);			
			
			if (entry.getHash() == hash) {
				entries.remove(i);
				break;
			}			
		}
	}
	
	public List<ArchiveEntry> getEntries() {
		return entries;
	}

}