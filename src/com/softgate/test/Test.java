package com.softgate.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.softgate.fs.Archive;
import com.softgate.fs.ArchiveEntry;
import com.softgate.util.CompressionUtil;
import com.softgate.util.HashUtils;

public class Test {

	public static void main(String[] args) throws IOException {
		decode();
	}
	
	private static void decode() throws IOException {
		File file = new File("C:\\Users\\Chad\\Downloads\\test.jag");

		if (file.exists()) {

			 Archive archive = Archive.decode(Files.readAllBytes(file.toPath()));

			 for (ArchiveEntry entry : archive.getEntries()) {
				 
				 if (HashUtils.nameToHash("main_file_sprites.dat") == entry.getHash()) {
					 System.out.println("found main_file_sprites.dat!");
					 break;
				 }
				 
				 //System.out.println(entry.getHash() + entry.getOrigSize() + " " + entry.getOnDiskSize());
			 }

		}
	}
	
	private static void encode() throws IOException {
		File file = new File("C:\\Users\\Chad\\Downloads\\config.jag");

		if (file.exists()) {

			 Archive archive = Archive.decode(Files.readAllBytes(file.toPath()));

			 File file2 = new File("C:\\Users\\Chad\\Downloads\\main_file_sprites.dat");
			 
			 byte[] data = Files.readAllBytes(file2.toPath());
			 
			 int hash = HashUtils.nameToHash(file2.getName());
			 
			 byte[] bzipped = CompressionUtil.bzip2(data);
			 
			 int uncompressedSize = data.length;
			 
			 int compressedSize = bzipped.length;
			 
			 ArchiveEntry entry = new ArchiveEntry(hash, uncompressedSize, compressedSize, bzipped);
			 
			 archive.getEntries().add(entry);
			 
			 byte[] encoded = archive.encode();
			 
			 try(FileOutputStream fos = new FileOutputStream(new File(file2.getParentFile(), "test.jag"))) {
				 fos.write(encoded);
			 }

		}
	}

}
