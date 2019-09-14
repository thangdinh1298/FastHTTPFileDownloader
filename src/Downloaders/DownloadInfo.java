package Downloaders;

import java.io.Serializable;

public class DownloadInfo implements Serializable {
    private int id;
    private String downloadLink;
    private String downloadDir;
    private String fileName;
    private Long fileSize;
    private Integer THREAD_NUM;
    private boolean resumable;
    private DownloadState state;

    public DownloadInfo(int id, String downloadLink, String downloadDir, String fileName,
                        Long fileSize, Integer THREAD_NUM, boolean resumable, DownloadState state) {
        this.id = id;
        this.downloadLink = downloadLink;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.THREAD_NUM = THREAD_NUM;
        this.resumable = resumable;
        this.state = state;
    }

    public DownloadInfo(int id, DownloadEntry entry){
        this.id = id;
        this.downloadLink = entry.getDownloadLink().toString();
        this.downloadDir = entry.getDownloadDir();
        this.fileName = entry.getFileName();
        this.fileSize = entry.getFileSize();
        this.THREAD_NUM = entry.getTHREAD_NUM();
        this.resumable = entry.isResumable();
        this.state = entry.getState();
    }

    public static DownloadInfo createDownloadInfo(int id, DownloadEntry entry){
        return new DownloadInfo(id, entry);
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Integer getTHREAD_NUM() {
        return THREAD_NUM;
    }

    public boolean isResumable() {
        return resumable;
    }

    public DownloadState getState() {
        return state;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%4d %10.50s %10.40s %10dB %10.15s", this.id, this.downloadDir,
                this.fileName, this.fileSize, this.state);
    }
}
