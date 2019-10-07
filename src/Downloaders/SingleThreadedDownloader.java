package Downloaders;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Future;

public class SingleThreadedDownloader extends DownloadEntry{

    //todo: close streams!!!! by handling error inside download function
    public SingleThreadedDownloader(URL url, String downloadDir, String fileName, boolean resumable) throws IOException{
        super(url, downloadDir, fileName, resumable);
        this.threadNum = 1;
    }

    @Override
    public String toString() {
        return "SingleThreaded downloader";
    }

    @Override
    public void download() throws IOException {
        this.setState(State.DOWNLOADING);
        if (this.futures == null){
            futures = new Future[this.threadNum];
        }
        if (this.tasks == null) {
            this.tasks = new DownloadThread[this.threadNum];
        }
        if (this.resumable == true){
            long bytesDownloaded = new File(this.getAbsolutePath()).length();
            this.tasks[0] = new DownloadThread(bytesDownloaded);
        } else{
            this.tasks[0] = new DownloadThread(0);
        }

    }

}
