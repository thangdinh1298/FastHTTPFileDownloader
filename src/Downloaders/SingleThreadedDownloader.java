package Downloaders;

import javax.naming.OperationNotSupportedException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SingleThreadedDownloader extends DownloadEntry implements Runnable{
    private Thread thisThread;

    //todo: close streams!!!! by handling error inside download function
    public SingleThreadedDownloader(URL url, String downloadDir, String fileName, boolean resumable) throws IOException{
        super(url, downloadDir, fileName, resumable);
        thisThread = new Thread(this);
    }

    @Override
    public void run() {
        try {
            this.download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initDownload() {
        thisThread.start();
    }

    @Override
    public void pause() throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Pause is not available for single-threaded downloads");
    }

    @Override
    public void resume() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    @Override
    public String toString() {
        return "SingleThreaded downloader";
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
            long count = 0;
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
