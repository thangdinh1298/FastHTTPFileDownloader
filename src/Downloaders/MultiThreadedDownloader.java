package Downloaders;

import Controller.Controller;
import Util.Configs;
import Util.FileManager;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MultiThreadedDownloader extends DownloadEntry{
    private static final long serialVersionUID = -7221155498655620062l;
    private transient Future[] futures;
    private transient DownloadThread[] tasks;
//    private transient Long downloadedBytes;

    public MultiThreadedDownloader(URL url, Long fileSize, String downloadDir, String fileName) throws IOException{
        super(url, downloadDir, fileName, true);
        this.fileSize = fileSize;
        this.threadNum = Configs.THREAD_NUM;
        //todo: this is a temporay solution to fix NullPointerException when entries are loaded from files and futures are not initialized. For a sound solution, State should be implemented
        this.futures = new Future[this.threadNum];
        this.tasks = new DownloadThread[this.threadNum];
//        this.downloadedBytes = 0L;
    }

    @Override
    public void run() {
        try {
            this.download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resume() throws OperationNotSupportedException {
    }

    @Override
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

    private void download() throws IOException {
        this.setState(State.DOWNLOADING);
        futures = new Future[this.threadNum];

        long segmentSize = this.fileSize / this.threadNum;
        long leftOver = this.fileSize % this.threadNum;
        long chunkStartByte = 0;
        for(int i = 0; i < this.threadNum; i++){
            long bytesDownloaded =  new File(String.format("%s/%s%d", downloadDir, fileName, i)).length();
//            System.out.println("==============================THREAD " + i + "==================================");
//            System.out.println("Numbytes downloaded for thread " + i + " is " + bytesDownloaded);
            long startByte = chunkStartByte + bytesDownloaded;
            long chunkSize = segmentSize - bytesDownloaded;
            if (i == this.threadNum - 1) chunkSize += leftOver;
//            System.out.println(startByte + " " + chunkSize);
//
//            System.out.println("Now downloading from " + startByte + " to " + (startByte + chunkSize - 1));
//
//            System.out.println("================================================================");

            chunkStartByte += segmentSize;
            if (chunkSize == 0) continue;

            this.tasks[i] = new DownloadThread(chunkSize, startByte, i);
            this.futures[i] = Controller.getInstance().getExecutorService().submit(this.tasks[i]);
            System.out.println("Submitting task " + i);


        }

        for(int i = 0; i < this.threadNum; i++) {
            try {
                if (futures[i] != null) futures[i].get();
            } catch (InterruptedException e) {
                //immediately returns if download thread is interrupted
                this.setState(State.PAUSED);
                e.printStackTrace();
                return;
            } catch (CancellationException e) {
                this.setState(State.PAUSED);
                e.printStackTrace();
                return;
            } catch (ExecutionException e) {
                this.setState(State.PAUSED);
                e.printStackTrace();
                return;
            }
        }


        //join files
        long count = 0;
        OutputStream os = null;
        InputStream is = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(getAbsolutePath()));
            for (int i = 0; i < this.threadNum; i++) {
                System.out.println("Opening file " + getAbsolutePath() + i  + " for reading");
                is = new BufferedInputStream(new FileInputStream(getAbsolutePath() + i));
                int c;

                while((c = is.read()) != -1){
                    count++;
                    os.write(c);
                }
                os.flush();
                is.close(); /* ?? */

//                File.
            }
            System.out.println("File size is " + count);
            this.setState(State.COMPLETED);
        } catch (IOException e) {
            this.setState(State.PAUSED);
            throw new IOException("Can't open file for merging");
        } finally{
            if (os != null) os.close();
            if (is != null) is.close();
        }
        FileManager.delete(this);
    }

    public long getDownloadedBytes(){
        Long downloadedBytes = 0L;
        for(int i = 0; i < this.futures.length; ++i){
            downloadedBytes += this.tasks[i].getCount();
        }
        return downloadedBytes;
    }

    public double getDownloadSpeed(){
        Long last_downloaded = this.getDownloadedBytes();
        int period_of_time = 1000;
        try {
            TimeUnit.MILLISECONDS.sleep(period_of_time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long current_downloaded = this.getDownloadedBytes();
        return (current_downloaded-last_downloaded)*1000/((double)period_of_time*period_of_time);
    }

    private class DownloadThread implements Runnable {
        private long startByte;
        private long chunkSize; //num bytes to download including the start byte
        private int threadID;
        private long count;
//        private boolean completed;
//        private

        HttpURLConnection conn = null;
        InputStream is = null;
        OutputStream os = null;

        public DownloadThread(long chunkSize, long startByte, int threadID) {
            this.chunkSize = chunkSize;
            this.startByte = startByte;
            this.threadID = threadID;
            this.count = 0;

            try {
                conn = (HttpURLConnection) downloadLink.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("Content-Language", "en-US");
                conn.setRequestProperty("Range", "bytes=" + this.startByte  + "-" + (this.startByte + this.chunkSize - 1));
                conn.connect();

                is = conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /*
            todo: Figureout a way to propagate incompleted downloads downward in the call stack
         */

        @Override
        public void run() {
//            System.out.println("Thread " + this.threadID + " is downloading from " + this.startByte + " to " + (this.startByte + this.chunkSize - 1) );

            try {
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
