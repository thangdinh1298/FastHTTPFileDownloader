package Downloaders;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;

public class SingleThreadedDownloader {
    URL url;
    String path = "downloadDir/test.pdf";

    public SingleThreadedDownloader(URL url) {
        this.url = url;
        try {
            download();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void download() throws IOException {
        HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.connect();

        InputStream is = conn.getInputStream();

        OutputStream os = new BufferedOutputStream(new FileOutputStream(path));
        int c;
        int count = 0;
        while ((c = is.read()) != -1){
            count++;
            os.write(c);
        }
        System.out.println(count);

    }
}
