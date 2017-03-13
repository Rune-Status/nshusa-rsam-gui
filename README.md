# RS2 Asset Manager (RSAM)
Is a tool to help manage your game assets

## Stable release: 1.03
[Download](http://www.mediafire.com/file/03k9oamipk6zuhw/rsam-gui-1.03.jar)

![store editor](http://i.imgur.com/DujEQsw.png)
![archive editor](http://i.imgur.com/37ADCZN.png)

## Features
* Fully open-source
* Very low memory usage, all data from within the cache is random accessed instead of read into memory.
* Program is fast and efficient
* Program will not corrupt your cache
* **store features**
	* Rename stores and store entries (these are stored in a file called archives.txt and stores.txt on your computer)
	* Add store entries (single and multiple files)
	* Remove store entries (don't actually remove them, this will be fixed in the future)
	* Dump stores and store entries (with multi-selection)
	* Search stores or store entries (by name or index)
* **archive features**
	* Create new archives
	* Add archive entries
	* Remove archive entries (actually removes them)
	* Identify hashes
	* Rename archives and archive entries
	* Search archives or archive entries (by name or hash)

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
* Files added into **archives NEED to be compressed using BZIP2**
* **All files inside file stores need to be compressed using GZIP if they are not archives**
* The program doesn't perform any compression or decompression on your data it fully preserves it
* If you add multiple files and the files are named like this (35.gz, 39.gz, 47.gz) the program will add these files into slots 35 for the first, 39 for the second, and 47 for the last one. If the file is not named with an index such as some_file.gz, this file will be added as the last index in the file store. The file will be named as the last index in the file store.

## Cookies
* **Store Cookie**: located in a file called "stores.txt" in user.home in a folder called .rsam
```
0:archive
1:model
2:animation
3:music
4:map
```
* **Archive Cookie** located in a file called "archives.txt" in user.home in a folder called .rsam
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
* **Hash cookie**, located in a file called "hashes.txt" in user.home in a folder called .rsam
```
-1185264806:flo2.dat
-952192193:combaticons2.dat
182704353:mesanim.idx
1275835656:1451391714
-1667598946:obj.idx
-90207845:button_brown_big.dat
1986120039:keys.dat
-1929337337:index.dat
-654418698:sideicons2.dat
1165431679:number_button.dat
```
