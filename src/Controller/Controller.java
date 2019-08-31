package Controller;

import Downloaders.SingleThreadedDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;

public class Controller {
    URL url;

    public Controller(URL url) {
        //todo: handle malform url from the main function
        this.url = url;

        try{
            boolean supportRange = pollForRangeSupport();
            Integer fileSize = pollForFileSize();

            System.out.println(supportRange + " " + fileSize);

            if (supportRange == true && fileSize != -1){
                //initialize multithreaded download
                System.out.println("This supports range");
            }else {
//                initialize single threaded download
                SingleThreadedDownloader sTD = new SingleThreadedDownloader(this.url);
            }

        } catch (NoRouteToHostException e){ // redundant catch, NoRouteToHost is IOException
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private boolean pollForRangeSupport() throws IOException {
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

    private Integer pollForFileSize() throws IOException {
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
            Controller controller = new Controller(new URL("https://drive.google.com/uc?export=download&id=1Xqd8JzANoUTQi-QP4u6su1Hva5k7pX6k"));
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }
}
