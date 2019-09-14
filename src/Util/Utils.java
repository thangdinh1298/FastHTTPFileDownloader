
package Util;

//import Downloaders.DownloadEntry;
import Downloaders.DownloadInfo;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Utils {
    public static void writeResponse(HttpExchange httpExchange, String msg, int code){
        OutputStream os = httpExchange.getResponseBody();

        byte[] b = msg.getBytes(Charset.defaultCharset());
        try {
            httpExchange.sendResponseHeaders(code, b.length);
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
    public static void writeResponse(HttpExchange httpExchange, ArrayList<DownloadInfo> infos, int code) throws IOException {
        OutputStream os = new BufferedOutputStream(httpExchange.getResponseBody());
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        ObjectOutputStream oos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            for(DownloadInfo info: infos) {
                oos.writeObject(info);
            }
            oos.flush();

            httpExchange.sendResponseHeaders(code, baos.size());
            bais = new ByteArrayInputStream(baos.toByteArray());

            int c;
            while( (c = bais.read()) != -1){
                os.write(c);
            }
            os.flush();
//            os.toString().length();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(baos != null)
                    baos.close();
                if(bais != null)
                    bais.close();
                if(oos != null)
                    oos.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeResponse(HttpExchange httpExchange, DownloadInfo info, int code) throws IOException {
        OutputStream os = new BufferedOutputStream(httpExchange.getResponseBody());
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        ObjectOutputStream oos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(info);
            oos.flush();

            httpExchange.sendResponseHeaders(code, baos.size());
            bais = new ByteArrayInputStream(baos.toByteArray());

            int c;
            while( (c = bais.read()) != -1){
                os.write(c);
            }
            os.flush();
//            os.toString().length();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(baos != null)
                    baos.close();
                if(bais != null)
                    bais.close();
                if(oos != null)
                    oos.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}