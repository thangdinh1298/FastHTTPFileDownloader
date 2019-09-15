package Downloaders;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.URL;

public class DownloaderFactory {
    public DownloaderFactory() {

    }

    public static DownloadEntry getDownloadEntry(boolean supportRange, Long fileSize, URL url, String downloadDir, String fileName){
        try{
            if (supportRange == true && fileSize != -1){
                //initialize multithreaded download
                System.out.println("This supports range");
                return new MultiThreadedDownloader(url, fileSize, downloadDir, fileName);
            }else {
                if (supportRange == true){
                    return new SingleThreadedDownloader(url, downloadDir, fileName, true);
                }
                else{
//                initialize single threaded download
                    return new SingleThreadedDownloader(url, downloadDir, fileName, false);
                }
            }

        } catch (NoRouteToHostException e){ // redundant catch, NoRouteToHost is IOException
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

}
