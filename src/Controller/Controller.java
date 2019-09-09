package Controller;

import Downloaders.MultiThreadedDownloader;
import Downloaders.SingleThreadedDownloader;
import Downloaders.DownloadEntry;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.util.ArrayList;

public class Controller {
    private ArrayList<DownloadEntry> entries;
    private static Controller controller = null;

    private Controller() {
        //initialize list of download entries
        this.entries = new ArrayList<>();
    }

    public static Controller getInstance(){
        if(Controller.controller == null){
            Controller.controller = new Controller();
        }
        return Controller.controller;
    }

    private void download(URL url){}

    private void resume(URL url){}

    private void pause(URL url){}

    //todo: handle malform url from the main function
    private void addDownload(URL url) {
        try{
            boolean supportRange = pollForRangeSupport(url);
            Long fileSize = pollForFileSize(url);

            System.out.println(supportRange + " " + fileSize);

            if (supportRange && fileSize != -1){
                //initialize multithreaded download
                System.out.println("This supports range");
                MultiThreadedDownloader mTD = new MultiThreadedDownloader(url, fileSize, "downloadDir", "test.pdf");
                entries.add(mTD);
                Thread t = new Thread(mTD);
                mTD.setUp();
                t.start();

//                System.out.println("Main thread sleeping");
                Thread.sleep(1800);
//                System.out.println("Main thread resumes");
//
                mTD.pause();
                Thread.sleep(3000);
                mTD.resume();
            }else {
//                initialize single threaded download
                SingleThreadedDownloader sTD = new SingleThreadedDownloader(url, "test.pdf", "downloadDir");
                entries.add(sTD);
            }

        }
        catch (NoRouteToHostException e){ // redundant catch, NoRouteToHost is IOException
            System.out.println(e.getMessage());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private boolean pollForRangeSupport(URL url) throws IOException {
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

    private Long pollForFileSize(URL url) throws IOException {
        HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.connect();

        int status = conn.getResponseCode();

        if (status == HttpURLConnection.HTTP_OK ) {
            if (conn.getHeaderFields().containsKey("Content-Length")) {
                try {
                    Long size = Long.parseLong(conn.getHeaderFields().get("Content-Length").get(0));
                    return size;
                } catch (NumberFormatException e) {
                    return (long)-1;
                }
            }
        }
        return (long)-1;
    }

    public static void main(String[] args) {
        try {
//            Controller controller = new Controller();
//            controller.addDownload( new URL("https://cdimage.kali.org/kali-2019.3/kali-linux-2019.3-amd64.iso"));
//            //https://drive.google.com/uc?export=download&id=1Xqd8JzANoUTQi-QP4u6su1Hva5k7pX6k"));
            Controller.getInstance().addDownload(new URL("https://vnso-zn-5-tf-mp3-s1-zmp3.zadn.vn/119a1af43eb3d7ed8ea2/4147490096273996622?authen=exp=1568127320~acl=/119a1af43eb3d7ed8ea2/*~hmac=b6fa932e7256eb7707da6a3dc53220ea&filename=Buoc-Qua-Doi-Nhau-Le-Bao-Binh.mp3"));

        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }
}
