package com.softgate.fs;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Cache implements Closeable {
	
	private final Path root;
	
	private final FileStore[] stores = new FileStore[255];
	
	private Cache(Path root) {
		this.root = root;
	}
	
	public static Cache init(Path root, boolean readOnly) throws IOException {
		Cache cache = new Cache(root);
		
		Path dataPath = root.resolve("main_file_cache.dat");
		
		if (!Files.exists(dataPath)) {
			throw new IOException("could not locate data file");
		}
		
		RandomAccessFile dataRaf = new RandomAccessFile(dataPath.toFile(), readOnly ? "r" : "rw");
		
		for (int i = 0; i < 255; i++) {			
			Path indexPath = root.resolve("main_file_cache.idx" + i);			
			if (Files.exists(indexPath)) {				
				cache.stores[i] = new FileStore(i + 1, dataRaf, new RandomAccessFile(indexPath.toFile(), readOnly ? "r" : "rw"));
			}			
		}
		return cache;
	}
	
	public FileStore getStore(int storeId) {
		if (storeId < 0 || storeId >= stores.length) {
			throw new IllegalArgumentException(String.format("storeId=%d out of range=[0, 254]", storeId));
		}
		
		return stores[storeId];
	}

	public Path getRoot() {
		return root;
	}
	
	public int getStoreCount() {		
		int count = 0;		
		for (int i = 0; i < 255; i++) {			
			Path indexPath = root.resolve("main_file_cache.idx" + i);			
			if (Files.exists(indexPath)) {				
				count++;
			}			
		}
		
		return count;
	}

	@Override
	public void close() throws IOException {
		for (FileStore store : stores) {
			if (store == null) {
				continue;
			}
			
			synchronized(store) {
				store.getDataRaf().close();
				store.getIndexRaf().close();
			}
			
		}
	}

}
