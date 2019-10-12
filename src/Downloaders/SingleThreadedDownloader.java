package Downloaders;

import Controller.Controller;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SingleThreadedDownloader extends DownloadEntry{

    //todo: close streams!!!! by handling error inside download function
    public SingleThreadedDownloader(URL url, String downloadDir, String fileName, boolean resumable) throws IOException{
        super(url, downloadDir, fileName, resumable);
        this.threadNum = 1;
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
    public String toString() {
        return "SingleThreaded downloader";
    }

    private void download() throws IOException {
        this.setState(State.DOWNLOADING);
        if(this.futures == null) {
            futures = new Future[this.threadNum];
        }
        if (this.tasks == null) {
            this.tasks = new DownloadThread[this.threadNum];
        }
        if (this.resumable == true){
            long bytesDownloaded = new File(this.getAbsolutePath()).length();
            this.futures[0] = /*Controller*/DownloadManager.getInstance().getExecutorService().submit(new DownloadThread(bytesDownloaded));
        } else {
            this.futures[0] = /*Controller*/DownloadManager.getInstance().getExecutorService().submit(new DownloadThread(0));
        }


        try {
            this.futures[0].get();
            this.setState(State.COMPLETED);
        } catch (InterruptedException e) {
            this.setState(State.PAUSED);
            this.futures[0].cancel(true);
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (CancellationException e) {
            this.setState(State.PAUSED);
            e.printStackTrace();
            return;
        }
        if (this.resumable == true){
            long bytesDownloaded = new File(this.getAbsolutePath()).length();
            this.tasks[0] = new DownloadThread(bytesDownloaded);
        } else{
            this.tasks[0] = new DownloadThread(0);
        }
    }

}
