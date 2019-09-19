package Downloaders;

import Controller.Controller;
import Util.Configs;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MultiThreadedDownloader extends DownloadEntry{
    private static final long serialVersionUID = -7221155498655620062l;
    private transient Future[] futures;
    //todo: close streams!!!! by handling error inside download function
    public MultiThreadedDownloader(URL url, Long fileSize, String downloadDir, String fileName) throws IOException{
        super(url, downloadDir, fileName, true);
        this.fileSize = fileSize;
        this.threadNum = Configs.THREAD_NUM;
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
                System.out.println("Job cancelled mid execution");
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (CancellationException e){
                System.out.println("Job cancelled before execution");
                e.printStackTrace();
            }
        }
    }

    private void download() throws IOException {
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

            futures[i] = Controller.getInstance().getExecutorService().submit(new DownloadThread(chunkSize, startByte, i));
            System.out.println("Submitting task " + i);

        }

        for(int i = 0; i < this.threadNum; i++) {
            try {
                if (futures[i] != null) futures[i].get();
            } catch (InterruptedException e) {
                //immediately returns if download thread is interrupted
                e.printStackTrace();
                return;
            } catch (CancellationException e) {
                e.printStackTrace();
                return;
            } catch (ExecutionException e) {
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
            }
            System.out.println("File size is " + count);
        } catch (IOException e) {
            throw new IOException("Can't open file for merging");
        } finally{
            if (os != null) os.close();
            if (is != null) is.close();
        }
    }

    private class DownloadThread implements Runnable {
        private long startByte;
        private long chunkSize; //num bytes to download including the start byte
        private int threadID;

        public DownloadThread(long chunkSize, long startByte, int threadID) {
            this.chunkSize = chunkSize;
            this.startByte = startByte;
            this.threadID = threadID;
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
                conn.setRequestProperty("Range", "bytes=" + this.startByte  + "-" + (this.startByte + this.chunkSize - 1));
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
