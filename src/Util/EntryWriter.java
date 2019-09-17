package Util;

import Downloaders.DownloadEntry;
import Downloaders.DownloaderFactory;

import javax.imageio.IIOException;
import java.io.*;
import java.util.ArrayList;

public class EntryWriter {
    public static void writeToFile(String fileName, DownloadEntry entry) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("history.dat"));
        outputStream.writeObject(entry);
        outputStream.flush();
        outputStream.close();
    }

    public static void writeToFile(String fileName, ArrayList<DownloadEntry> entries) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("history.dat"));
        for(DownloadEntry entry : entries){
            if(entry != null)
                outputStream.writeObject(entry);
        }
        outputStream.flush();
        outputStream.close();
    }

    public static ArrayList<DownloadEntry> readFromFile(String fileName) throws IOException {
        ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream("history.dat")));
        DownloadEntry entry;
        ArrayList<DownloadEntry> entries = new ArrayList<>();
        try {
            while ((entry = (DownloadEntry) inputStream.readObject()) != null) {
                DownloadEntry de = DownloaderFactory.getDownloadEntry(entry.isResumable(),
                        entry.getFileSize(), entry.getDownloadLink(), entry.getDownloadDir(), entry.getFileName());

                if (de != null){
                    entries.add(de);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e){
            System.out.println("EOF");
        }
        return entries;
    }
}
