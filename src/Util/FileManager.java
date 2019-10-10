package Util;

import Downloaders.DownloadEntry;

import java.io.File;

public class FileManager {
    public static void delete(DownloadEntry entry){
        if(entry.isResumable()) {
            String filename = entry.getFileName();
            int i = 0;
            while(i < entry.getThreadNum()){
                if(new File(String.format("%s/%s%d", entry.getDownloadDir(), entry.getFileName(), i)).delete()){
                    System.out.printf("delete %s segment %d successfully!\n", entry.getFileName(), i);
                }
                else{
                    System.out.printf("segment %d do not exist!!\n", i);
                }
                i++;
            }
        }

    }
}