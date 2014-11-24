Ace Combat 3 Header Replacer for TIM files v1.1
by Dashman

0) What's new?

I had forgotten there were 12 bytes after the CLUTs and was copying those from the edited TIMs like they were image data. Turns out they weren't.

Not sure if those 12 bytes were that important, considering the insertion worked, but hey, better safe than sorry.

1) How to use this thing.

First of all, this is a Java applet. If you don't have Java, install the latest JRE.
Second, this is a command line application. To execute it, you'll need to open up a console/shell window, get to the folder where the program is and execute this:

java -jar ac3hr.jar <original_TIMs_folder> <edited_TIMs_folder> <output_folder>


2) Stuff to note

* You'll have noticed the parameters of the program are three folders. That's because this program will work with every TIM file you have in said folders.

* The folders' paths are taken relative to the folder where the program is. So, for example, you can save the original TIMs in 'C:/AC3/FolderA', the edited TIMs in 'C:/AC3/FolderB' and the program in 'C:/AC3'; then execute it with 'java -jar ac3hr.jar FolderA FolderB FolderC'

* It doesn't look into subfolders. I could change this later.

* The output_folder doesn't need to exist previously. The program will create it if it doesn't find it.

* The program assumes the TIM files in original_TIMs_folder and edited_TIMs_folder are named the same. If the names are different, the editing *may* happen (if you have the same number of TIM files in both folders), but most probably the resulting files will be wrong. Respect the filenames.

* The program admits different numbers of TIM files in the original and edited folder (for example, 10 in original and 5 in edited or the other way around). In that case, it will try to find the corresponding original - edited pairs by name. One more reason to respect the filenames.

* In case you're wondering what this program does: having the original TIM 'A' and the edited TIM 'B', it creates a final TIM 'C' that has:
	- The 20 first bytes of A (header without the CLUTs)
	- The CLUTs found in A (32 bytes x number of CLUTs in A)
	- The image data in B
	
* I've only tested this for folders with 1 file and have implemented very little security measures, so if something explodes please let me know. 

* And no, don't worry, your computer is not going to explode. It was a manner of speech.