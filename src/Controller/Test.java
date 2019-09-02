package Controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        try {
            URL drive = new URL("https://drive.google.com/uc?export=download&id=1Xqd8JzANoUTQi-QP4u6su1Hva5k7pX6k");
            HttpURLConnection conn =  (HttpURLConnection)drive.openConnection();
//            conn.setConnectTimeout(1200);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty("Content-Language", "en-US");
            conn.setDoInput(true);
            conn.connect();

            int status = conn.getResponseCode();

            System.out.println(status);

//            if (status == HttpURLConnection.HTTP_OK) {
                for(Map.Entry<String, List<String>> header:  conn.getHeaderFields().entrySet()){
                    System.out.println(header);
                }
//            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
