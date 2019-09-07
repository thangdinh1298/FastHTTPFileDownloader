package Downloaders;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.URL;

public class DownloaderFactory {
    public DownloaderFactory() {

    }

    public static DownloadEntry getDownloadEntry(boolean supportRange, Integer fileSize, URL url, String downloadDir, String fileName){
        try{
            System.out.println(supportRange + " " + fileSize);
            if (supportRange == true && fileSize != -1){
                //initialize multithreaded download
                System.out.println("This supports range");
                return new MultiThreadedDownloader(url, fileSize, "downloadDir", "test.pdf");
            }else {
//                initialize single threaded download
                return new SingleThreadedDownloader(url, "test.pdf", "downloadDir");
            }

        } catch (NoRouteToHostException e){ // redundant catch, NoRouteToHost is IOException
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
