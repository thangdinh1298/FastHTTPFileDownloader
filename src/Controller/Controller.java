package Controller;

import Downloaders.DownloaderFactory;
import Downloaders.DownloadEntry;
import Util.BackupManager;
import Util.Configs;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Controller {
    private static ExecutorService executorService;
    private static BackupManager backupManager;
    private static ArrayList<DownloadEntry> entries;
    private static Controller controller = null;

    private Controller() {
    }

    public static Controller getInstance() {
        if (controller == null) {
            controller = new Controller();
            backupManager = new BackupManager();
            executorService = Executors.newFixedThreadPool(Configs.THREAD_POOL_SIZE);
            //initialize entries list
            try {
                entries = Util.EntryWriter.readFromFile(Configs.history);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return controller;
    }

    //todo: handle malformed url from the main function
    public void addDownload(URL url, String fileName, String downloadDir) {
        try{
            boolean resumable = pollForRangeSupport(url);
            Long fileSize = pollForFileSize(url);

            System.out.println(resumable + " "  + fileSize);

            DownloadEntry de = DownloaderFactory.getDownloadEntry(resumable,
                    fileSize, url, downloadDir, fileName);
            if (de != null){
                Controller.getInstance().addToEntryList(de);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteDownload(int index) throws IndexOutOfBoundsException {
        DownloadEntry de = Controller.getInstance().getEntryAt(index);
        try {
            de.pause();
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
        System.out.println("Exception");
        //todo: add logic: delete all segments, if merging then somehow delete the merging file
        Controller.getInstance().removeAt(index);
    }

    public void pauseDownload(int index) throws OperationNotSupportedException {

        DownloadEntry de = Controller.getInstance().getEntryAt(index);

        de.pause();
    }

    public void resumeDownload(int index) throws OperationNotSupportedException {
        DownloadEntry de = Controller.getInstance().getEntryAt(index);
//        futures.set(index, executorService.submit(de));
        executorService.submit(de);
    }

    private boolean pollForRangeSupport(URL url) throws IOException {
        HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setRequestProperty("Range", "bytes=10-20");
        conn.connect();

        int status = conn.getResponseCode();
        System.out.println("Status code is: "+ status);

        if (status == HttpURLConnection.HTTP_PARTIAL) {
            return true;
        }

        return false;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    private Long pollForFileSize(URL url) throws IOException {
        HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.connect();

        int status = conn.getResponseCode();

        if (status == HttpURLConnection.HTTP_OK ){
            if (conn.getHeaderFields().containsKey("Content-Length")) {
                try {
                    Long size = Long.parseLong(conn.getHeaderFields().get("Content-Length").get(0));
                    return size;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return -1l;
                }
            }
        }
        return -1l;
    }

    public void backup (){
        BackupManager.backup(entries);
    }

    private synchronized void updateAt(int index, DownloadEntry entry){
        DownloadEntry oldEntry = entries.get(index);
        oldEntry = entry;
    }

    private synchronized void addToEntryList(DownloadEntry entry){
        entries.add(entry);
        Future future = executorService.submit(entry);

        backup();
    }

    private synchronized void removeAt(int index) throws IndexOutOfBoundsException{
        entries.remove(index);

        backup();
    }

    public DownloadEntry getEntryAt(int idx) throws IndexOutOfBoundsException{
        return entries.get(idx);
    }

    public ArrayList<DownloadEntry> getEntries(){
        return entries;
    }

    public static void main(String[] args) {
        try {
            Controller controller = new Controller();
            controller.addDownload( new URL("https://drive.google.com/uc?export=download&id=1Xqd8JzANoUTQi-QP4u6su1Hva5k7pX6k"),"test.pdf","downloadDir");

        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
    }
}
