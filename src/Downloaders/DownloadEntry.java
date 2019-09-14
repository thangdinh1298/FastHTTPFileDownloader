package Downloaders;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class DownloadEntry implements Serializable {
    protected URL downloadLink;
    protected String downloadDir;
    protected String fileName;
    protected Long fileSize;
    protected Integer THREAD_NUM;
    protected boolean resumable;
    protected DownloadState state = DownloadState.STOP;
    transient protected long timeRemainning;//seconds
    transient protected float speed;//kb / s
    transient protected long totalTimeDownloading;//seconds
    transient private Date date;

    public DownloadEntry(URL downloadLink, String downloadDir, String fileName, long fileSize, boolean resumable) {
        this.downloadLink = downloadLink;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.resumable = resumable;
        this.state = DownloadState.DOWNLOADING;
        this.THREAD_NUM = null;
        this.timeRemainning = -1;
        this.speed = 0;
        this.totalTimeDownloading = 0;
    }


    public boolean isResumable(){
        return resumable;
    }

    public URL getDownloadLink() {
        return downloadLink;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public String getFileName() {
        return fileName;
    }


    public String getAbsolutePath() {
        return downloadDir + "/" + fileName;
    }

    public long getTimeRemainning() {
        return timeRemainning;
    }

    public float getSpeed() {
        return speed;
    }

    public long getTotalTimeDownloading() {
        return totalTimeDownloading;
    }

    public static void writeHistory(String fileName, DownloadEntry entry) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName, true));
        oos.writeObject(entry);
        oos.flush();
        oos.close();
    }

    public static void writeHistory(String fileName, ArrayList<DownloadEntry> entries) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));

        int count = 0;
        for( DownloadEntry e: entries ){
            if(e != null) {
                System.out.println(e);
                oos.writeObject(e);
                count++;
            }
        }
        oos.flush();
        oos.close();

        System.out.println(String.format("Wrote %d entries to file %s", count, fileName));
    }

    public static ArrayList<DownloadEntry> loadHistory(String fileName) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
        ArrayList<DownloadEntry> entries = new ArrayList<>();

        DownloadEntry de;

        try {
            while(true) {
                de = (DownloadEntry) ois.readObject();
                if(de.isResumable() && de.state != DownloadState.COMPLETED){
                    ((MultiThreadedDownloader) de).loadSegment();
                }
                entries.add(de);
            }
        }catch (EOFException e){
            System.out.println("end of file!!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            ois.close();
            System.out.println(entries);
        }
        return entries;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Integer getTHREAD_NUM() {
        return THREAD_NUM;
    }

    public DownloadState getState() {
        return state;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return this.downloadLink+"----"+this.fileName+"----"+this.state;
    }
}
