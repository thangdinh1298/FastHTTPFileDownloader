package Controller;

import Downloaders.DownloaderFactory;
import Downloaders.MultiThreadedDownloader;
import Downloaders.SingleThreadedDownloader;
import Downloaders.DownloadEntry;

import javax.naming.OperationNotSupportedException;
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

            DownloadEntry de = DownloaderFactory.getDownloadEntry(supportRange,
                    fileSize, url, "downloadDir", "test.pdf");
            entries.add(de);

            Thread.sleep(800);

            de.pause();
//
//            Thread.sleep(1000);
//            ((MultiThreadedDownloader) de).download();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
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

        if (status == HttpURLConnection.HTTP_OK ){
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
