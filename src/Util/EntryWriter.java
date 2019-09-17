package Util;

import Downloaders.DownloadEntry;
import Downloaders.DownloaderFactory;

import javax.imageio.IIOException;
import java.io.*;
import java.util.ArrayList;

public class EntryWriter {
    public static void writeToFile(String fileName, DownloadEntry entry) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
        outputStream.writeObject(entry);
        outputStream.flush();
        outputStream.close();
    }

    public static void writeAllToFile(String fileName, ArrayList<DownloadEntry> entries) throws IOException{
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
        for (DownloadEntry entry: entries){
            outputStream.writeObject(entry);
        }
        outputStream.flush();
        outputStream.close();
    }

    public static ArrayList<DownloadEntry> readFromFile(String fileName) throws IOException {
        ObjectInputStream inputStream = null;
        DownloadEntry entry;
        ArrayList<DownloadEntry> entries = new ArrayList<>();
        File history = new File(Configs.history);
        if (!history.exists()) {
            history.createNewFile();
        }
        try {
            inputStream =  new ObjectInputStream(new FileInputStream(history));
            while ((entry = (DownloadEntry) inputStream.readObject()) != null) {
                DownloadEntry de = DownloaderFactory.getDownloadEntry(entry.isResumable(),
                        entry.getFileSize(), entry.getDownloadLink(), entry.getDownloadDir(), entry.getFileName());

                if (de != null){
                    entries.add(de);
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null )
                inputStream.close();
        }
        return entries;
    }
}
