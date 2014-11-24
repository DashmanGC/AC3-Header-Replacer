/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ac3headerreplacer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.Path;

/**
 *
 * @author Jonatan
 */
public class Main {
    static File[] listOriginals;
    static File[] listEdited;
    static String destination;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String original;
        String edited;
        // TODO code application logic here
        String appname = new java.io.File(Main.class.getProtectionDomain()
                                          .getCodeSource()
                                          .getLocation()
                                          .getPath()).getName();

        // USE: ac3headerreplacer <original_TIM_folder> <edited_TIM_folder> <output_folder>
        if (args.length == 1 && args[0].equals("-h")){
            System.out.println("USE: java -jar " + appname + " <original_TIM_folder> <edited_TIM_folder> <output_folder>");
            return;
        }
        if (args.length != 3){
            System.out.println("ERROR: Wrong number of parameters: " + args.length);
            System.out.println("USE: java -jar " + appname + " <original_TIM_folder> <edited_TIM_folder> <output_folder>");
            return;
        }

        original = formatFolder(args[0]);
        edited = formatFolder(args[1]);

        if (original.equals(edited)){
            System.out.println("ERROR: The original and edited folders can't be the same!");
            return;
        }

        destination = formatFolder(args[2]);

        //System.out.println("First: " + original + " Last: " + edited);

        grabFolders(original, edited);

        fixHeaders(destination);
    }

    public static String formatFolder(String folderName){
        String result = "";

        if (folderName.equals("."))
            return folderName;

        if (folderName.substring(2, 3).equals(":/") || folderName.substring(2, 3).equals(":\\"))
            return folderName;

        if (folderName.startsWith("./") || folderName.startsWith(".\\"))
            return folderName;

        if (!folderName.startsWith("/") && !folderName.startsWith("\\"))
            result = "./" + folderName;
        else
            result = "." + folderName;

        return result;
    }

    public static void grabFolders(String originalFolder, String editedFolder){
        // Get the lists of files
        File timFolder = new File(originalFolder);
        listOriginals = timFolder.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".tim") || filename.endsWith(".TIM")); }
        });
        System.out.println("Original files: " + listOriginals.length);

        timFolder = new File(editedFolder);
        listEdited = timFolder.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".tim") || filename.endsWith(".TIM")); }
        });
        System.out.println("Edited files: " + listEdited.length);

        // Sort the folders by name
        Arrays.sort(listOriginals, new Comparator<File>(){
            public int compare(File f1, File f2)
            {
                return String.valueOf(f1.getName()).compareTo(f2.getName());
            } });

        Arrays.sort(listEdited, new Comparator<File>(){
            public int compare(File f1, File f2)
            {
                return String.valueOf(f1.getName()).compareTo(f2.getName());
            } });
    }

    public static void fixHeaders(String outputFolder){
        // Check if we have the same amount of files in each folder
        if (listOriginals.length == listEdited.length){
            // IMPORTANT: We're assuming the names are the same in both folders!
            for(int i = 0; i < listOriginals.length; i++){
                outputFile(listOriginals[i], listEdited[i]);
            }

            System.out.println(listOriginals.length + " file(s) created.");
        }
        else{
            int counter = 0;

            if (listOriginals.length > listEdited.length){
                for (int i = 0; i < listOriginals.length && counter < listEdited.length; i++){
                    if (listOriginals[i].getName().equals(listEdited[counter].getName())){
                        outputFile(listOriginals[i], listEdited[counter]);
                        counter++;
                    }
                }
            }
            else{
                for (int i = 0; i < listEdited.length && counter < listOriginals.length; i++){
                    if (listEdited[i].getName().equals(listOriginals[counter].getName())){
                        outputFile(listOriginals[counter], listEdited[i]);
                        counter++;
                    }
                }
            }

            System.out.println(counter + " file(s) created.");
        }
    }

    public static void outputFile(File original, File edited){
        try {
            /*
            Path path = Paths.get("path/to/file");
            byte[] data = Files.readAllBytes(path);
             */
            RandomAccessFile f1 = new RandomAccessFile(original, "r");
            RandomAccessFile f2 = new RandomAccessFile(edited, "r");

            // Obtain both headers
            byte[] header_original = new byte[20];
            byte[] header_edited = new byte[20];
            f1.read(header_original);
            f2.read(header_edited);

            int num_cluts1 = header_original[18];
            int num_cluts2 = header_edited[18];

            //System.out.println("Original: " + num_cluts1);
            //System.out.println("Edited: " + num_cluts2);
            //System.out.println("Original: " + header_original);
            //System.out.println("Edited: " + header_edited);
            /*for (int i = 0; i < 20; i++){
                System.out.println(i + " - Original:" + header_original[i] + " Edited: " + header_edited[i]);
            }*/

            int cluts_length = 32 * num_cluts1;
            
            byte[] cluts1 = new byte[cluts_length + 12]; // We add the extra 12 bytes after the CLUTs

            //System.out.println("CLUTS' length: " + cluts_length);

            f1.read(cluts1);    // Obtain the original CLUTs
            f1.close();

            /*for (int i = 0; i < cluts_length; i++){
                System.out.println((20 + i) + " - Original: " + cluts1[i]);
            }*/

            f2.skipBytes(32*num_cluts2);    // Skip the edited CLUTs
            f2.skipBytes(12);   // We skip the extra 12 bytes after the CLUTs

            long data_length = f2.length() - 32 - 32*num_cluts2;    // 32 = 20 at the beginning + 12 after the CLUTs

            byte[] data = new byte[(int)data_length];

            f2.read(data);  // Obtain the edited data
            f2.close();

            // Write the output file

            // First, make sure that the destination folder exists
            File outputFolder = new File(destination);
            if(!outputFolder.exists()){
                boolean success = false;

                if (destination.length() > 1){
                    if (destination.substring(2, destination.length() - 1).contains("/")
                            || destination.substring(2, destination.length() - 1).contains("\\"))
                        // There's several levels of subfolders
                        success = outputFolder.mkdirs();
                    else
                        // Only one subfolder
                        success = outputFolder.mkdir();
                }
                else{   // Only one character
                    if (!destination.equals("/") && !destination.equals("\\") && !destination.equals("."))
                        success = outputFolder.mkdir();
                }

                if (success){
                    System.out.println("Directory " + destination + " created.");
                }
            }

            String path = destination;
            if (!path.endsWith("/") && !path.endsWith("\\"))
                path += "/";

            path += original.getName();

            RandomAccessFile f3 = new RandomAccessFile(path, "rw");

            // The output file is: Original header + Original CLUTs + Edited data
            f3.write(header_original);
            f3.write(cluts1);
            f3.write(data);

            f3.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
