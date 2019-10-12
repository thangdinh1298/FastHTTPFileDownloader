package Downloaders;

import Util.Configs;
import Util.EntryWriter;

import java.util.ArrayList;
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

    public synchronized void addDownload(DownloadEntry entry){
        entries.add(entry);
        executorService.submit(entry);
    }

    public synchronized void pauseDownload(int index) throws IndexOutOfBoundsException, InterruptedException, ExecutionException {
        if (index >= entries.size()) {
            throw new IndexOutOfBoundsException();
        }
        DownloadEntry de = entries.get(index);
        try {
            de.pause();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public synchronized void resumeDownload(int index) throws IndexOutOfBoundsException{
        if (index >= entries.size()) {
            throw new IndexOutOfBoundsException();
        }
        DownloadEntry de = entries.get(index);
        executorService.submit(de);
    }

    public synchronized void deleteDownload(int index) throws IndexOutOfBoundsException, InterruptedException, ExecutionException {
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
        } finally {
            entries.remove(index);
        }
    }

    public ExecutorService getExecutorService(){
        return executorService;
    }

    public ArrayList<DownloadEntry> getEntries(){
        System.out.println("ACB");
        ArrayList<DownloadEntry> clone = new ArrayList<>();
        clone.addAll(entries);
        return clone;
    }
}
