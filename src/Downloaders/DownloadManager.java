package Downloaders;

import Util.Configs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager {
    private static ExecutorService executorService;
    private static ArrayList<DownloadEntry> entries;
    private static DownloadManager downloadManager = null;

    private DownloadManager(){

    }
    public static DownloadManager getInstance(){
        if (downloadManager == null) {
            downloadManager = new DownloadManager();
            executorService = Executors.newFixedThreadPool(Configs.THREAD_POOL_SIZE);
//            entries = EntryWriter.readHistory();
            entries = new ArrayList<>();
        }
        return downloadManager;
    }

    public synchronized void addDownload(URL url, String fileName, String downloadDir) throws IOException{
        boolean resumable = pollForRangeSupport(url);
        Long fileSize = pollForFileSize(url);

        System.out.println(resumable + " "  + fileSize);

        DownloadEntry de = DownloaderFactory.getDownloadEntry(resumable,
                fileSize, url, downloadDir, fileName);
        if (de != null){
            entries.add(de);
            executorService.submit(de);
        }
    }

    public synchronized void pauseDownload(int index) throws IndexOutOfBoundsException, InterruptedException, ExecutionException, CancellationException {
        if (index >= entries.size()) {
            throw new IndexOutOfBoundsException();
        }
        DownloadEntry de = entries.get(index);
        try {
            de.pause();
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e;
        } catch (CancellationException e) {
            throw e;
        }
    }

    public synchronized void resumeDownload(int index) throws IndexOutOfBoundsException{
        if (index >= entries.size()) {
            throw new IndexOutOfBoundsException();
        }
        DownloadEntry de = entries.get(index);
        executorService.submit(de);
    }

    public synchronized void deleteDownload(int index) throws IndexOutOfBoundsException, InterruptedException, ExecutionException, CancellationException {
        if (index >= entries.size()) {
            throw new IndexOutOfBoundsException();
        }
        DownloadEntry de = entries.get(index);
        try{
            de.pause();
        }catch (InterruptedException e){
            throw e;
        } catch (ExecutionException e){
            throw e;
        } catch (CancellationException e) {
            throw e;
        } //remove the download even if the pausing fails???
        finally {
            entries.remove(index);
        }
    }

    public ExecutorService getExecutorService(){
        return executorService;
    }

    public ArrayList<DownloadEntry> getEntries(){
//        ArrayList<DownloadEntry> clone = new ArrayList<>();
//        clone.addAll(entries);
//        return clone;
        return entries;
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

}
