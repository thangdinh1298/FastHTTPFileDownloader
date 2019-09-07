package Downloaders;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiThreadedDownloader extends DownloadEntry implements Runnable{
    private int THREAD_NUM = 8; // default thread num is 8
    private int fileSize;
    public Thread[] threads;

    //todo: close streams!!!! by handling error inside download function
    public MultiThreadedDownloader(URL url, int fileSize, String downloadDir, String fileName, int THREAD_NUM) throws IOException{
        super(url, downloadDir, fileName, true);
        this.fileSize = fileSize;
        this.THREAD_NUM = THREAD_NUM;
//        download(de);
    }

    public void pause() {
        System.out.println("Pausing");
        for(Thread t: this.threads){
            t.interrupt();
        }
    }

    public void download() throws IOException {
        System.out.println("Downloading");
        threads = new Thread[this.THREAD_NUM];
        int segmentSize = this.fileSize / this.THREAD_NUM;
        int leftOver = this.fileSize % this.THREAD_NUM;
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
                System.out.println("Thread " + i + " interrupted");
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
    public void run() {
        try {
            this.download();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            InputStream is = null;
            OutputStream os = null;

            try {
                conn = (HttpURLConnection) downloadLink.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("Content-Language", "en-US");
                conn.setRequestProperty("Range", "bytes=" + startByte  + "-" + (startByte + fileSize - 1));
                conn.connect();

                is = conn.getInputStream();

                os = new BufferedOutputStream(new FileOutputStream(downloadDir + "/" + fileName + this.threadID));
                int c;
                int count = 0;
                while((c = is.read()) != -1 && !Thread.interrupted()){
                    System.out.println("Still downloading");
                    count++;
                    os.write(c);
                }
                System.out.println("Thread " + this.threadID + " downloaded "  + count + " bytes");
                os.flush();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally{
                System.out.println("Interrupted");
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
