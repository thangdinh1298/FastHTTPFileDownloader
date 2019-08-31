package Downloaders;

import jdk.internal.util.xml.impl.Input;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiThreadedDownloader {
    private URL url;
    private String path = "downloadDir";
    private String fileName = "test";
    private final int THREAD_NUM = 8; // default thread num is 8
    private int fileSize;

    public MultiThreadedDownloader(URL url, int fileSize) {
        this.url = url;
        this.fileSize = fileSize;
        try {
            download();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void download() throws IOException{
        Thread[] threads = new Thread[this.THREAD_NUM];
        int segmentSize = fileSize / THREAD_NUM;
        int leftOver = fileSize % THREAD_NUM;
        int startByte = 0;
        for(int i = 0; i < this.THREAD_NUM; i++){
            if (i < this.THREAD_NUM - 1) threads[i] = new Thread(new DownloadThread(segmentSize, startByte, i));
            else threads[i] = new Thread(new DownloadThread(segmentSize + leftOver, startByte, i));
            startByte += segmentSize;
            threads[i].start();
        }

        for(int i = 0; i < this.THREAD_NUM; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //join files
        int count = 0;
        OutputStream os = new BufferedOutputStream(new FileOutputStream(path + "/" + fileName));
        for (int i = 0; i < this.THREAD_NUM; i++) {
            System.out.println("Opening file " + path + "/" + fileName + i  + " for reading");
            InputStream is = new BufferedInputStream(new FileInputStream(path + "/" + fileName + i));
            int c;

            while((c = is.read()) != -1){
                count++;
                os.write(c);
            }
            os.flush();

        }
        System.out.println("File size is " + count);
    }

    private class DownloadThread implements Runnable {
        private int startByte;
        private int fileSize; //num bytes to download including the start byte
        private int threadID;

        public DownloadThread(int fileSize, int startByte, int threadID) {
            this.fileSize = fileSize;
            this.startByte = startByte;
            this.threadID = threadID;
        }

        /*
            todo: Figureout a way to propagate incompleted downloads downward in the call stack
         */
        @Override
        public void run() {
            System.out.println("Thread " + this.threadID + " is downloading from " + this.startByte + " to " + (startByte + fileSize - 1) );
            HttpURLConnection conn = null;

            try {
                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("Content-Language", "en-US");
                conn.setRequestProperty("Range", "bytes=" + startByte  + "-" + (startByte + fileSize - 1));
                conn.connect();

                InputStream is = conn.getInputStream();

                OutputStream os = new BufferedOutputStream(new FileOutputStream(path + "/" + fileName + this.threadID));
                int c;
                while((c = is.read()) != -1){
                    os.write(c);
                }
                os.flush();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

}
