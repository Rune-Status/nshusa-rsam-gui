# RS2 Asset Manager (RSAM)
Is a tool designed to help you access and modify the 2005-2006 RuneScape file system

## Stable release: 1.0.6
[Download](https://github.com/nshusa/rsam-gui/releases/tag/1.0.6)

## Features
* Fully open-source
* Very low memory usage
* Fast and efficient
* Will **NOT** corrupt your cache
* Search files by their name or index
* Import and Export files
* Rename files
* Identify hashes
* Tons check it out

![image1](https://i.imgur.com/vanxiNy.png)
![image2](https://i.imgur.com/1rbgGao.png)

## Notes
* Your cache must be properly named for this program to read it.
 ```
 main_file_cache.dat
main_file_cache.idx0
main_file_cache.idx1
main_file_cache.idx2
main_file_cache.idx3
main_file_cache.idx4
```
* Files added into **archives index 0 NEED to be compressed using BZIP2**
* **All files inside file stores (indexes) 1-4 need to be compressed using GZIP**
* The program does not perform any compression on your data it fully preserves it
* If you add multiple files and the files are named like this (35.gz, 39.gz, 47.gz) the program will add these files into slots 35 for the first, 39 for the second, and 47 for the last one. If the file is not named with an index such as some_file.gz, this file will be added as the last index in the file store. The file will be named as the last index in the file store.

## Meta information
RS2 Asset Manager stores information in your home directory in a folder called ".rsam".
* archives.json
* stores.json
* hash_names.txt