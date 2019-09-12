package Controller;

import Downloaders.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;
//import java.util.Queue;

public class Controller {
    private ArrayList<DownloadEntry> entries = new ArrayList<>();
    private String historyFile = "downloadDir/history.dat";
    private static Controller controller = null;

    private Stack<Integer> avaiableID;

    private Controller() {
        //initialize list of download entries
        try {
            this.entries = DownloadEntry.loadHistory(this.historyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.avaiableID = new Stack<>();
    }

    public ArrayList<DownloadEntry> getEntries() {
        return entries;
    }

    public void handler(){
        //your code here
    }

    synchronized private int genID(){
        if(this.avaiableID.isEmpty()){
            return this.entries.size();
        }
        return this.avaiableID.pop();
    }

    public static Controller getInstance(){
        if(Controller.controller == null){
            Controller.controller = new Controller();
        }
        return Controller.controller;
    }

    private MultiThreadedDownloader getDownloadEntry(int id){
        return (MultiThreadedDownloader) this.entries.get(id);
    }

    public void download(URL url, String filename, String downloadDir) throws IOException {
        int id = this.addDownload(url, filename, downloadDir);
        DownloadEntry entry = this.entries.get(id);
        if(entry.isResumable()) {
            Thread t = new Thread();
            ((MultiThreadedDownloader) entry).setUp();
            t.start();
        }
        else {
            ((SingleThreadedDownloader) entry).download();
        }

    }

    public void resume(int id) throws IOException {
        this.getDownloadEntry(id).resume();
    }

    public void pause(int id){
        this.getDownloadEntry(id).pause();
    }

    public void remove(int id){
        this.entries.set(id, null);
    }

    public void writeHistory() throws IOException {
        DownloadEntry.writeHistory(this.historyFile, this.entries);
    }

    private ArrayList<Pair<String, Integer>> getListDownloadEntry(){
        if(this.entries.size() == 0)
            return null;
        ArrayList<Pair<String, Integer>> listDownloadEntry = new ArrayList<>();
        DownloadEntry downloadEntry;
        for(int i = 0; i < this.entries.size(); ++i){
            downloadEntry = this.entries.get(i);
            if(downloadEntry != null)
                listDownloadEntry.add(new Pair<String, Integer>(downloadEntry.getDownloadLink().toString(), i));
        }
        return listDownloadEntry;
    }

    //todo: handle malform url from the main function
    synchronized private int addDownload(URL url, String filename, String downloadDir) {
        int id = this.genID();
        try{
            boolean supportRange = pollForRangeSupport(url);
            Long fileSize = pollForFileSize(url);

            System.out.println(supportRange + " " + fileSize);

            DownloadEntry entry;
            if (supportRange && fileSize != -1){
                //initialize multithreaded download
                System.out.println("This supports range");
                entry = new MultiThreadedDownloader(url, fileSize, downloadDir, filename);


//                System.out.println("Main thread sleeping");
//                Thread.sleep(1800);
////                System.out.println("Main thread resumes");
////
//                mTD.pause();
//                Thread.sleep(3000);
//                mTD.resume();
            }else {
//                initialize single threaded download
                entry = new SingleThreadedDownloader(url, "test.pdf", "downloadDir", fileSize);

            }
            if(id < this.entries.size()){
                this.entries.set(id, entry);
            }
            else
                this.entries.add(entry);
        }
        catch (NoRouteToHostException e){ // redundant catch, NoRouteToHost is IOException
            System.out.println(e.getMessage());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return id;
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
//
    public static void main(String[] args) {
        try {
//            Controller controller = new Controller();
//            controller.addDownload(
//            new URL("https://cdimage.kali.org/kali-2019.3/kali-linux-2019.3-amd64.iso"));
//            //https://drive.google.com/uc?export=download&id=1Xqd8JzANoUTQi-QP4u6su1Hva5k7pX6k"));
            Controller.getInstance().addDownload(new URL("https://vnno-vn-6-tf-mp3-s1-zmp3.zadn.vn/ed5d01312576cc289567/5313514683599977435?authen=exp=1568218398~acl=/ed5d01312576cc289567/*~hmac=abdff2d14d92f63936e774946234ac57&filename=Ngay-Cho-Thang-Nho-Nam-Thuong-OSAD.mp3"),
                    "buocquadoinhau.mp3", "downloadDir/");

        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }
}
