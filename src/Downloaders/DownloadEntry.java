package Downloaders;

import javax.naming.OperationNotSupportedException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Paths;

public class DownloadEntry implements Serializable {
    private static final long serialVersionUID = -5615651863970612799l;
    protected URL downloadLink;
    protected String downloadDir;
    protected String fileName;
    protected long fileSize;
    protected int threadNum;
    protected boolean resumable;
//    protected State state;

    public DownloadEntry(URL downloadLink, String downloadDir, String fileName,
                         boolean resumable) {
        this.downloadLink = downloadLink;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.resumable = resumable;
//        this.state = State.PAUSED;
    }

//    public enum State {
//        PAUSED,
//
//        DOWNLOADING,
//
//        COMPLETED
//    }

    public void initDownload() {}

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
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

    public void pause() throws OperationNotSupportedException {}

    @Override
    public String toString() {
        return String.format("%s\t", this.getAbsolutePath());
    }

    public String getAbsolutePath() {
        return String.valueOf(Paths.get(downloadDir, fileName));
    }

}
