package Controller;

import Downloaders.DownloaderFactory;
import Downloaders.DownloadEntry;

import java.io.IOException;
import java.net.HttpURLConnection;

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
    public static void addDownload(URL url, String fileName, String downloadDir) {
        System.out.println(Controller.entries);
        try{
            boolean supportRange = pollForRangeSupport(url);
            Long fileSize = pollForFileSize(url);

            System.out.println(supportRange + " "  + fileSize);

            DownloadEntry de = DownloaderFactory.getDownloadEntry(supportRange,
                    fileSize, url, downloadDir, fileName);
            de.initDownload();
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

        return false;
    }

    private static Long pollForFileSize(URL url) throws IOException {
        HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.connect();

        int status = conn.getResponseCode();

        if (status == HttpURLConnection.HTTP_OK ){
            if (conn.getHeaderFields().containsKey("Content-Length")) {
                try {
                    Long size = Long.parseLong(conn.getHeaderFields().get("Content-Length").get(0));
                    return size;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return -1l;
                }
            }
        }
        return -1l;
    }

    public static ArrayList<DownloadEntry> getEntries(){
        return entries;
    }

    public static void main(String[] args) {
//        try {
//            Controller controller = new Controller();
//            controller.addDownload( new URL("https://cdimage.kali.org/kali-2019.3/kali-linux-2019.3-amd64.iso"));
//
//        } catch (MalformedURLException e) {
//            System.out.println(e.getMessage());
//        }
    }
}
