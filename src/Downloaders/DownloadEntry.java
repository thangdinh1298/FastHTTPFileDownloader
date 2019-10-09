package Downloaders;

import Controller.Controller;

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

    public DownloadEntry(URL downloadLink, String downloadDir, String fileName,
                         boolean resumable) {
        this.downloadLink = downloadLink;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.resumable = resumable;
    }

    public enum State {
        PAUSED,

        DOWNLOADING,

        COMPLETED,
    }

    public State getState() {
        return state;
    }

    public void initState(State state) {
        this.state = state;
    }

    public void setState(State state) {
        this.state = state;
        Controller.getInstance().backup();
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

    }

    public void pause() throws OperationNotSupportedException {
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

    @Override
    public String toString() {
        return String.format("%s\t%s", this.getAbsolutePath(), this.getState());
    }

    public String getAbsolutePath() {
        return String.valueOf(Paths.get(downloadDir, fileName));
    }

    @Override
    public void run() {

    }

    protected class DownloadThread implements Runnable {
        private long startByte;
        private long endByte; //num bytes to download including the start byte
        private int threadID;

        public DownloadThread(long startByte, long endByte, int threadID) {
            this.startByte = startByte;
            this.endByte = endByte;
            this.threadID = threadID;
        }

        public DownloadThread(long startByte){
            this.startByte = startByte;
            this.endByte = -1;
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
                }
                else {
                    conn.setRequestProperty("Range", "bytes=" + this.startByte  + "-");
                }
                conn.connect();

                is = conn.getInputStream();

                os = new BufferedOutputStream(new FileOutputStream(String.valueOf(Paths.get(downloadDir, fileName + this.threadID)), true));
                int c;
                long count = 0;
                while((c = is.read()) != -1 && !Thread.interrupted()){
                    count++;
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
    }
}
