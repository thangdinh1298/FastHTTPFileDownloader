package Downloaders;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SingleThreadedDownloader extends DownloadEntry{

    //todo: close streams!!!! by handling error inside download function
    public SingleThreadedDownloader(URL url, String downloadDir, String fileName) throws IOException{
        super(url, downloadDir, fileName, false);
    }

    @Override
    public void pause() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Pause is not available for single-threaded downloads");
    }

    @Override
    public void resume() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    private void download() throws IOException {
        HttpURLConnection conn = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            conn = (HttpURLConnection) downloadLink.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty("Content-Language", "en-US");
            conn.connect();

            is = conn.getInputStream();

            os = new BufferedOutputStream(new FileOutputStream(getAbsolutePath()));
            int c;
            int count = 0;
            while ((c = is.read()) != -1){
                count++;
                os.write(c);
            }
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
            conn.disconnect();
        }
    }
}
