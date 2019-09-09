package Util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
}
