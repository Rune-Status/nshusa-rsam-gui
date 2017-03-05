# RS2 Asset Manager (RSAM)
Is a tool to help manage your game assets

## Stable release: 1.00
[Download](http://www.mediafire.com/file/5wa8p10nbabsunq/rsam-1.00.jar)

## Features
* Fully open-source
* Very low memory usage, all data from within the cache is random accessed instead of read into memory.
* Program is fast and efficient
* Rename stores and archives (these are stored in a file called archives.txt and stores.txt on your computer)
    * The reason these are stored on your computer is because the cache doesn't contain this information, it's all referenced to keep your game assets hidden away from the user.
* Add files (single and multiple files)
* Remove files
* Dump files
* Program will not corrupt your cache
* Search file stores
* Search file store entries

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

* The program doesn't perform any compression or decompression on your data it fully preserves it
* If you add multiple files and the files are named like this (35.gz, 39.gz, 47.gz) the program will add these files into slots 35 for the first, 39 for the second, and 47 for the last one. If the file is not named with an index such as some_file.gz, this file will be added as the last index in the file store. The file will be named as the last index in the file store.

* Files added into **archive store NEEDS to be compressed using BZIP2**
* **All other file stores NEED to be compressed using GZIP**
* When naming archives, the program stores this piece of information in a file called archives.txt located in your user.home in a folder called .rsam
    * Format #:string
        * '#' is the index the file is located within the file store
        * ':' is only used to separate the data 
        * 'string' is the name you want that file to be called.
        
```
0:media.jag
1:title screen.jag
2:config.jag
3:interface.jag
4:2d graphics.jag
5:version list.jag
6:textures.jag
7:chat system.jag
8:sound effects.jag
```
* When naming stores, the program stores this in a file called stores.txt located in your user.home in a folder called .rsam
    * Format #:string
    * '#' is the index the store is located within the cache
    * ':' is only used to separate the data
    * 'string' is the name you want the index to be called
```
0:archive
1:model
2:animation
3:music
4:map
```