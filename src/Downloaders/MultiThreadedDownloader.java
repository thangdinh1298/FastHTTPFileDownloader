package Downloaders;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiThreadedDownloader extends DownloadEntry implements Runnable{
    private int THREAD_NUM = 8; // default thread num is 8
    private Long fileSize;
    private Thread[] threads;
    private Thread thisThread;

    //todo: close streams!!!! by handling error inside download function
    public MultiThreadedDownloader(URL url, Long fileSize, String downloadDir, String fileName) throws IOException{
        super(url, downloadDir, fileName, true);
        this.fileSize = fileSize;
        this.thisThread = new Thread(this);
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
    public void initDownload() {
        thisThread.start();
    }

    @Override
    public void resume() throws OperationNotSupportedException {
        thisThread.start();
    }

    @Override
    public void pause() throws OperationNotSupportedException{
        synchronized (this){
            for(Thread t: this.threads){
                if (t != null) t.interrupt();
            }
            thisThread.interrupt();
        }

        // wait for all threads to enter terminated state
        try {
            thisThread.join();
            for (Thread t: this.threads){
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void download() throws IOException {
        threads = new Thread[this.THREAD_NUM];

        long segmentSize = this.fileSize / this.THREAD_NUM;
        long leftOver = this.fileSize % this.THREAD_NUM;
        long chunkStartByte = 0;
        for(int i = 0; i < this.THREAD_NUM; i++){
            long bytesDownloaded =  new File(String.format("%s/%s%d", downloadDir, fileName, i)).length();
//            System.out.println("==============================THREAD " + i + "==================================");
//            System.out.println("Numbytes downloaded for thread " + i + " is " + bytesDownloaded);
            long startByte = chunkStartByte + bytesDownloaded;
            long chunkSize = segmentSize - bytesDownloaded;
            if (i == this.THREAD_NUM - 1) chunkSize += leftOver;
//            System.out.println(startByte + " " + chunkSize);
//
//            System.out.println("Now downloading from " + startByte + " to " + (startByte + chunkSize - 1));
//
//            System.out.println("================================================================");

            chunkStartByte += segmentSize;
            if (chunkSize == 0) continue;

            threads[i] = new Thread(new DownloadThread(chunkSize, startByte, i));

            threads[i].start();
        }

        for(int i = 0; i < this.THREAD_NUM; i++) {
            try {
                if (threads[i] != null) threads[i].join();
            } catch (InterruptedException e) {
                //immediately returns if download thread is interrupted
                return;
            }
        }


        //join files
        int count = 0;
        OutputStream os = null;
        InputStream is = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(getAbsolutePath()));
            for (int i = 0; i < this.THREAD_NUM; i++) {
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

    @Override
    public String toString() {
        return "Multithreaded downloader";
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

                os = new BufferedOutputStream(new FileOutputStream(downloadDir + "/" + fileName + this.threadID, true));
                int c;
                int count = 0;
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
