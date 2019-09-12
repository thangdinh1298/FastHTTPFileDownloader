package Downloaders;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiThreadedDownloader extends DownloadEntry implements Runnable{
    transient private Thread[] threads;
    transient private int completedThread;
    transient private Pair<Long, Long>[] segment;
    transient private boolean downloading = false;

    //todo: close streams!!!! by handling error inside download function
    public MultiThreadedDownloader(URL url, long fileSize, String downloadDir, String fileName, int THREAD_NUM) throws IOException{
        super(url, downloadDir, fileName, fileSize, true);
        this.THREAD_NUM = THREAD_NUM;
        this.completedThread = 0;
//        download(de);
    }

    public MultiThreadedDownloader(URL url, long fileSize, String downloadDir, String fileName) throws IOException{
        super(url, downloadDir, fileName, fileSize, true);
        this.fileSize = fileSize;
        this.THREAD_NUM = 8;
        this.completedThread = 0;
//        download(de);
    }

    public void pause() {
        if (this.downloading) {
            this.downloading = false;
            System.out.println("Pausing");
            for (Thread t : this.threads) {
                if (t != null)
                    t.interrupt();
            }
        }
        else
            System.out.println("Paused!!");
    }

    public void setUp(){
        System.out.println("set up");
        threads = new Thread[this.THREAD_NUM];
        this.segment = new Pair[this.THREAD_NUM];

        long segmentSize = this.fileSize / this.THREAD_NUM;
        long leftOver = this.fileSize % this.THREAD_NUM;
        long startByte = 0;
        long first = 0;
        long second = 0;

        for(int i = 0; i < this.THREAD_NUM; i++) {
            first = startByte;
            if (i < this.THREAD_NUM - 1) {
                second = startByte + segmentSize - 1;
            } else {
                second = startByte + segmentSize + leftOver - 1;
            }
            this.segment[i] = new Pair<>(first, second);
            System.out.println(this.segment[i]);
            startByte += segmentSize;
        }
    }

    public void download() throws IOException {
        for (int i = 0; i < this.THREAD_NUM; ++i) {
            if (this.segment[i].first <= this.segment[i].second) {
                this.threads[i] = new Thread(new DownloadThread(i, this.segment[i].first, this.segment[i].second));
                threads[i].start();
            }
            else
                this.threads[i] = null;
        }

        for (int i = 0; i < this.THREAD_NUM; i++) {
            if(this.threads[i] != null) {
                try {
                    threads[i].join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Thread " + i + " interrupted");
                }
            }
        }
        this.mergeFile();
    }

    public void resume() throws IOException {
        if(!this.downloading) {
            this.downloading = true;
            System.out.println("resume downloading!!");
            this.loadSegment();
        }
        else
            System.out.println("downloading!!");
    }

    private void mergeFile() throws IOException {
        if(this.completedThread == this.THREAD_NUM) {

            //join files
            long count = 0;
            OutputStream os = null;
            InputStream is = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(getAbsolutePath()));
                for (int i = 0; i < this.THREAD_NUM; i++) {
                    System.out.println("Opening file " + getAbsolutePath() + i + " for reading");
                    is = new BufferedInputStream(new FileInputStream(getAbsolutePath() + i));
                    int c;

                    while ((c = is.read()) != -1) {
                        count++;
                        os.write(c);
                    }
                    os.flush();

                }
                System.out.println("File size is " + count);
            } catch (IOException e) {
                throw new IOException("Can't open file for merging");
            } finally {
                if (os != null) os.close();
                if (is != null) is.close();
            }
        }
        else{
            System.out.println("Something went wrong!!");
        }
    }

    @Override
    public void run() {
        try {
            this.downloading = true;
            this.download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class DownloadThread implements Runnable {
        private long startByte;
        private long segmentSize; //num bytes to download including the start byte
        private int threadID;

        public DownloadThread(long segmentSize, long startByte, int threadID) {
            this.segmentSize = segmentSize;
            this.startByte = startByte;
            this.threadID = threadID;
        }

        public DownloadThread(int threadID, long startByte, long endByte){
            this.threadID = threadID;
            this.startByte = startByte;
            this.segmentSize = endByte - startByte +1;
        }

        /*
            todo: Figureout a way to propagate incompleted downloads downward in the call stack
         */
        @Override
        public void run() {
            System.out.println("Thread " + this.threadID + " is downloading from " + this.startByte + " to " + (this.startByte + this.segmentSize - 1) );
            HttpURLConnection conn = null;
            InputStream is = null;
            OutputStream os = null;

            long count = 0;
            try {
                conn = (HttpURLConnection) downloadLink.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("Content-Language", "en-US");
                conn.setRequestProperty("Range", "bytes=" + startByte  + "-" + (startByte + fileSize - 1));
                conn.connect();

                is = conn.getInputStream();

                os = new BufferedOutputStream(new FileOutputStream(downloadDir + "/" + fileName + this.threadID, true));
                int c;

                while(count < this.segmentSize && (c = is.read()) != -1 && !Thread.interrupted()){
//                    System.out.println("Still downloading");
                    count++;
                    os.write(c);
                }
                System.out.println("Thread " + this.threadID + " downloaded "  + count + " bytes");
                os.flush();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally{
                if(Thread.interrupted())
                    System.out.println("Interrupted");
                synchronized (this){
                    segment[this.threadID].first = startByte+count;
                    if(segment[this.threadID].first >  segment[this.threadID].second)
                        completedThread++;
                }
//                System.out.println(segment[this.threadID]);
                if (conn != null)
                    conn.disconnect();
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

    public void loadSegment() throws IOException {
        System.out.println("thread num: "+this.THREAD_NUM);
        System.out.println("file size: "+this.fileSize);
        int i = 0;
        this.completedThread = 0;
        this.downloading = false;
        this.segment = new Pair[this.THREAD_NUM];
        this.threads = new Thread[this.THREAD_NUM];
        long segmentSize = this.fileSize/this.THREAD_NUM;
        long startByte = -segmentSize;
        long endByte = 0;
        long bytesDownloaded = 0;
        while(i<this.THREAD_NUM){
            bytesDownloaded = new File(String.format("%s/%s%d", this.downloadDir, this.fileName, i)).length();

            startByte += segmentSize;
            if(i != this.THREAD_NUM-1){
                endByte = startByte+segmentSize-1;
            }
            else
                endByte = this.fileSize-1;

            if(startByte + bytesDownloaded >  endByte)
                this.completedThread++;
            this.segment[i] = new Pair<>(startByte+bytesDownloaded, endByte);

            ++i;
        }
    }
}
