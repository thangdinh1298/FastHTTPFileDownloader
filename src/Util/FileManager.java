package Util;

import Downloaders.DownloadEntry;
import Downloaders.DownloaderFactory;

import javax.imageio.IIOException;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileManager {
    public static void writeToFile(String fileName, DownloadEntry entry) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
        outputStream.writeObject(entry);
        outputStream.flush();
        outputStream.close();
    }

    public static void writeAllHistory(ArrayList<DownloadEntry> entries) throws IOException{
        File history = new File(Configs.history);
        //check if file exists, if not, attempt to create and write to it.
        if (!history.exists()){
            try{
                history.createNewFile();
            } catch (IOException e){ //returns immediately if the file could not be created
                return;
            }

        }

        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(history));
        for (DownloadEntry entry: entries){
            outputStream.writeObject(entry);
        }
        outputStream.flush();
        outputStream.close();
    }

    public static ArrayList<DownloadEntry> readHistory(){
        ObjectInputStream inputStream = null;
        DownloadEntry entry;
        ArrayList<DownloadEntry> entries = new ArrayList<>();
        try {
            // if history file does not exist yet return empty arrayList
            File history = new File(Configs.history);
            if (!history.exists()) {
                return entries;
            }

            inputStream =  new ObjectInputStream(new FileInputStream(history));
            while (true) {
                //read entry
                entry = (DownloadEntry) inputStream.readObject();
                System.out.println("Entry is: " + entry);
                //Parse entry
                DownloadEntry de = DownloaderFactory.getDownloadEntry(entry.isResumable(),
                        entry.getFileSize(), entry.getDownloadLink(), entry.getDownloadDir(), entry.getFileName());
                // If parsing was successful, adjust state appropriately and add to entrty list
                if (de != null){
                    if (entry.getState() == DownloadEntry.State.DOWNLOADING){
                        de.setState(DownloadEntry.State.PAUSED);
                    } else{
                        de.initState(entry.getState());
                    }
                    entries.add(de);
                }
            }
        } catch (EOFException e){
            System.out.println("EOF reached");
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null ) try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(entries.size());
        return entries;
    }

    public static void delete(DownloadEntry entry){
        if(entry.isResumable()) {
            int i = 0;
            while(i < entry.getThreadNum()){
                if(new File(String.valueOf(Paths.get(entry.getDownloadDir(), entry.getFileName() + i))).delete()){
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
