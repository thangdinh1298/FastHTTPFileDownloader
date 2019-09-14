package Downloaders;

import javax.naming.OperationNotSupportedException;
import java.net.URL;

public abstract class DownloadEntry {
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

    public void initDownload() {};

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

    public void resume() throws OperationNotSupportedException {}

    public void pause() throws OperationNotSupportedException { }


    public String getAbsolutePath() {
        return downloadDir + "/" + fileName;
    }

    protected void setCompleted(){
        completed = true;
    }

}
