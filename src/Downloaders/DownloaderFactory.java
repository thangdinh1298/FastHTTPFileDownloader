package Downloaders;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.URL;

public class DownloaderFactory {
    public DownloaderFactory() {

    }

    public static DownloadEntry getDownloadEntry(boolean resumable, Long fileSize, URL url, String downloadDir, String fileName){
        try{
            if (resumable == true && fileSize != -1){
                //initialize multithreaded download
                System.out.println("This supports range");
                return new SingleThreadedDownloader(url, downloadDir, fileName, false);
            }else {
                if (resumable == true){
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
