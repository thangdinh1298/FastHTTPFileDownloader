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
    public MultiThreadedDownloader(URL url, Long fileSize, String downloadDir, String fileName) throws IOException{
        super(url, downloadDir, fileName, true);
        this.fileSize = fileSize;
        this.threadNum = Configs.THREAD_NUM;
        //todo: this is a temporay solution to fix NullPointerException when entries are loaded from files and futures are not initialized. For a sound solution, State should be implemented
//        futures = new Future[this.threadNum];
    }

    @Override
    public void run() {
        try {
            this.download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void download() throws IOException {
        this.setState(State.DOWNLOADING);
        if(this.futures == null){
            futures = new Future[this.threadNum];
        }

        long segmentSize = this.fileSize / this.threadNum;
        long chunkStartByte = 0, startByte = 0, endByte = 0;
        for(int i = 0; i < this.threadNum; i++){
            long bytesDownloaded =  new File(String.valueOf(Paths.get(this.downloadDir, this.fileName + i))).length();
//            System.out.println("==============================THREAD " + i + "==================================");
//            System.out.println("Numbytes downloaded for thread " + i + " is " + bytesDownloaded);
            startByte = chunkStartByte + bytesDownloaded;
            endByte = chunkStartByte + segmentSize - 1;
            if (i == this.threadNum - 1) endByte = this.fileSize - 1;
//            System.out.println(startByte + " " + chunkSize);
//
//            System.out.println("Now downloading from " + startByte + " to " + (startByte + chunkSize - 1));
//
//            System.out.println("================================================================");

            chunkStartByte += segmentSize;
            if (startByte >= endByte) continue;

            futures[i] = Controller.getInstance().getExecutorService().submit(new DownloadThread(startByte, endByte, i));
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
    }

}
