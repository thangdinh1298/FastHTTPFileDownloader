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
import java.util.List;
import java.util.Map;

public class Controller {
    private static ArrayList<DownloadEntry> entries = new ArrayList<>();
    private static Controller controller = null;

    private Controller() {
    }

    public static Controller getInstance() {
        if (controller == null) {
            controller = new Controller();
            //initialize entries list
        }
        return controller;
    }

    //todo: handle malformed url from the main function
    public static void addDownload(URL url) {
        try{
            boolean supportRange = pollForRangeSupport(url);
            Integer fileSize = pollForFileSize(url);

            System.out.println(supportRange + " "  + fileSize);

            DownloadEntry de = DownloaderFactory.getDownloadEntry(supportRange,
                    fileSize, url, "downloadDir", "test.pdf");
            System.out.println("adding entries");
            entries.add(de);
            System.out.println("returning from add download");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean pollForRangeSupport(URL url) throws IOException {
        HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setRequestProperty("Range", "bytes=10-20");
        conn.connect();

        int status = conn.getResponseCode();
        System.out.println("Status code is: "+ status);

        if (status == HttpURLConnection.HTTP_PARTIAL) {
            return true;
        }

        for (Map.Entry<String, List<String>> m: conn.getHeaderFields().entrySet()){
            System.out.println(m);
        }

        return false;
    }

    private static Integer pollForFileSize(URL url) throws IOException {
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
