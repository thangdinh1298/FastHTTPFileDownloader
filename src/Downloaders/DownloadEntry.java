package Downloaders;

import java.net.URL;

public class DownloadEntry {
    protected URL downloadLink;
    protected String downloadDir;
    protected String fileName;
    protected boolean completed;
    protected boolean resumable;

    public DownloadEntry(URL downloadLink, String downloadDir, String fileName, boolean resumable) {
        this.downloadLink = downloadLink;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.resumable = resumable;
    }

    public boolean isCompleted(){
        return completed;
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

    protected void setCompleted(){
        completed = true;
    }
}