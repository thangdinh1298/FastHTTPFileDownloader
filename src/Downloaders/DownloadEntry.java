package Downloaders;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DownloadEntry implements Serializable, Runnable {
    private static final long serialVersionUID = -5615651863970612799l;
    protected URL downloadLink;
    protected String downloadDir;
    protected String fileName;
    protected long fileSize;
    protected int threadNum;
    protected boolean resumable;
    protected State state;
    protected transient Future[] futures;
    protected transient DownloadThread[] tasks;

    public DownloadEntry(URL downloadLink, String downloadDir, String fileName,
                         boolean resumable) {
        this.downloadLink = downloadLink;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.resumable = resumable;
        this.state = State.WAITING;
    }

    @Override
    public void run() {
        try {
            this.download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum State {
        WAITING,

        PAUSED,

        DOWNLOADING,

        COMPLETED,
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

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

    public void resume() throws OperationNotSupportedException {
        if (this.resumable == false){
            throw new OperationNotSupportedException();
        } else {
            System.out.println("Attempting to resume");
        }
    }


    public void pause() throws OperationNotSupportedException{
        if(this.getState() == State.COMPLETED){
            System.out.println("download completed!");
            return;
        }
        synchronized (this){
            for(Future f: this.futures){
                if (f != null && !f.isCancelled() && !f.isDone()) f.cancel(true);
            }
        }

        // wait for all jobs to enter terminated state
        for(int i = 0; i < this.futures.length; i++){
            try {
                if (this.futures[i] != null) this.futures[i].get();

            } catch (InterruptedException e) {
//                System.out.println("Job cancelled mid execution");
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (CancellationException e){
                System.out.println("Job cancelled");
//                e.printStackTrace();
            }
        }
    }

    public void download() throws IOException{

    }

    public long getNumberOfDownloadedBytes(){
        if(this.tasks == null)
            return 0l;
        Long downloadedBytes = 0L;
        for(int i = 0; i < this.futures.length; ++i){
            if(this.tasks[i] != null)
                downloadedBytes += this.tasks[i].getCount();
        }
        return downloadedBytes;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s", this.getAbsolutePath(), this.getState());
    }

    public String getAbsolutePath() {
        return String.valueOf(Paths.get(downloadDir, fileName));
    }


    protected class DownloadThread implements Runnable {
        private long startByte;
        private long endByte; //num bytes to download including the start byte
        private int threadID;
        private long count;
//        private boolean completed;
//        private

        public DownloadThread(long startByte, long endByte, int threadID) {
            this.endByte = endByte;
            this.startByte = startByte;
            this.threadID = threadID;
            this.count = 0;
        }

        public DownloadThread(long startByte, long endByte, long bytesDownloaded, int threadID) {
            this.endByte = endByte;
            this.startByte = startByte;
            this.threadID = threadID;
            this.count = bytesDownloaded;
        }

        public DownloadThread(long startByte) {
            this.endByte = -1;
            this.startByte = startByte;
            this.count = 0;
        }

        /*
            todo: Figureout a way to propagate incompleted downloads downward in the call stack
         */

        @Override
        public void run() {
//            System.out.println("Thread " + this.threadID + " is downloading from " + this.startByte + " to " + (this.startByte + this.chunkSize - 1) );
            HttpURLConnection conn = null;
            InputStream is = null;
            OutputStream os = null;

            try {
                conn = (HttpURLConnection) downloadLink.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("Content-Language", "en-US");
                if (endByte != -1){
                    conn.setRequestProperty("Range", "bytes=" + this.startByte  + "-" + this.endByte);
                } else {
                    conn.setRequestProperty("Range", "bytes=" + this.startByte  + "-" );
                }
                conn.connect();

                is = conn.getInputStream();
                os = new BufferedOutputStream(new FileOutputStream(String.valueOf(Paths.get(downloadDir, fileName + this.threadID)), true));
                int c;

                while((c = is.read()) != -1 && !Thread.interrupted()){
//                    System.out.println(fileName + ": Thread "+ this.threadID + " is downloading");
                    synchronized (this) {
                        count++;
                    }
                    os.write(c);
                }
                System.out.println("Thread " + this.threadID + " downloaded "  + count + " bytes");
                os.flush();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally{
//                System.out.println("Interrupted");
                if (conn != null) conn.disconnect();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        public long getCount(){
            return this.count;
        }
    }
}
