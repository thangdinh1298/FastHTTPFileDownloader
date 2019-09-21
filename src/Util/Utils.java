package Util;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class Utils {
    public static void writeResponse(HttpExchange httpExchange, String msg){
        OutputStream os = httpExchange.getResponseBody();

        byte[] b = msg.getBytes(Charset.defaultCharset());
        try {
            httpExchange.sendResponseHeaders(200, b.length);
            os.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void doPost(String url, HashMap<String, String> headers, String body) {
        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            //write headers
            con.setRequestMethod("POST");
            for (Map.Entry<String, String> entry: headers.entrySet()){
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }

            //write body
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(body);

            writer.close();
//            int responseCode = con.getResponseCode();

            //read return value
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while((line = in.readLine()) != null){
                builder.append(line);
            }
            in.close();
            System.out.println(builder.toString());

        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void doGet(String url, HashMap<String, String> headers) {
        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            //write headers
            con.setRequestMethod("GET");
            for (Map.Entry<String, String> entry: headers.entrySet()){
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }

//            int responseCode = con.getResponseCode();

            //read return value
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while((line = in.readLine()) != null){
                builder.append(line + "\n");
            }
            in.close();
            System.out.println(builder.toString());

        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
