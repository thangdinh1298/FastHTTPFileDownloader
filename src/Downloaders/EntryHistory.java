package Downloaders;

import java.io.*;
import java.util.ArrayList;

public class EntryHistory {
//    private String fileName = "dowloadDir/history.dat";
//    public EntryHistory(String fileName) {
//        this.fileName = fileName;
//    }

    public static ArrayList<DownloadEntry> loadHistory(String fileName) throws IOException
    {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
        ArrayList<DownloadEntry> entries = new ArrayList<>();

        DownloadEntry de;

        try {
            while(true) {
                de = (DownloadEntry) ois.readObject();
                entries.add(de);
            }
        }catch (EOFException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println(entries);
            return entries;
        }
    }

    public static void writeHistory(String fileName, ArrayList<DownloadEntry> entries) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));

        int count = 0;
        for( DownloadEntry e: entries ){
            System.out.println(e);
            oos.writeObject(e);
            count++;
        }
        oos.flush();
        oos.close();

        System.out.println(String.format("Wrote %d entries to file %s", count, fileName));
    }
}
