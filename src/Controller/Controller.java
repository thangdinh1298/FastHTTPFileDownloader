package Controller;

import Downloaders.MultiThreadedDownloader;
import Downloaders.SingleThreadedDownloader;
import Downloaders.DownloadEntry;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.util.ArrayList;

public class Controller {
    private ArrayList<DownloadEntry> entries;

    public Controller() {
        //initialize list of download entries
        entries = new ArrayList<>();

    }

    //todo: handle malform url from the main function
    public void addDownload(URL url) {
        try{
            boolean supportRange = pollForRangeSupport(url);
            Integer fileSize = pollForFileSize(url);

            System.out.println(supportRange + " " + fileSize);

            if (supportRange && fileSize != -1){
                //initialize multithreaded download
                System.out.println("This supports range");
                MultiThreadedDownloader mTD = new MultiThreadedDownloader(url, fileSize, "downloadDir", "test.pdf");
                entries.add(mTD);
                Thread t = new Thread(mTD);
                mTD.setUp();
                t.start();

//                System.out.println("Main thread sleeping");
                Thread.sleep(1800);
//                System.out.println("Main thread resumes");
//
                mTD.pause();
                Thread.sleep(3000);
                mTD.resume();
            }else {
//                initialize single threaded download
                SingleThreadedDownloader sTD = new SingleThreadedDownloader(url, "test.pdf", "downloadDir");
                entries.add(sTD);
            }

        }
        catch (NoRouteToHostException e){ // redundant catch, NoRouteToHost is IOException
            System.out.println(e.getMessage());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private boolean pollForRangeSupport(URL url) throws IOException {
        HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setRequestProperty("Range", "bytes=10-20");
        conn.connect();

        int status = conn.getResponseCode();

        if (status == HttpURLConnection.HTTP_PARTIAL) {
            return true;
        }

        return false;
    }

    private Integer pollForFileSize(URL url) throws IOException {
        HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.connect();

        int status = conn.getResponseCode();

        if (status == HttpURLConnection.HTTP_OK ) {
            if (conn.getHeaderFields().containsKey("Content-Length")) {
                try {
                    Integer size = Integer.parseInt(conn.getHeaderFields().get("Content-Length").get(0));
                    return size;
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        try {
            Controller controller = new Controller();
            controller.addDownload( new URL("https://drive.google.com/uc?export=download&id=1Xqd8JzANoUTQi-QP4u6su1Hva5k7pX6k"));

        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }
}
