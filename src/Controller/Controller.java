package Controller;

import Downloaders.DownloadManager;
import Util.EntryWriter;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class Controller {

    //todo: handle malformed url from the main function
    public static void addDownload(URL url, String fileName, String downloadDir) throws IOException {
        DownloadManager.getInstance().addDownload(url,fileName, downloadDir);
    }

    public static void deleteDownload(int index) throws IndexOutOfBoundsException, ExecutionException, InterruptedException {
        //todo: add logic: delete all segments, if merging then somehow delete the merging file
        DownloadManager.getInstance().deleteDownload(index);
    }

    public static void pauseDownload(int index) throws IndexOutOfBoundsException, ExecutionException, InterruptedException {
        DownloadManager.getInstance().pauseDownload(index);
    }
    //todo:warn user that u can't resume some downloads
    public static void resumeDownload(int index) throws IndexOutOfBoundsException {
        DownloadManager.getInstance().resumeDownload(index);
    }





    public void backup (){
        try {
            EntryWriter.writeAllHistory(DownloadManager.getInstance().getEntries());
        } catch (IOException e) {
            System.out.println("Could not back up to file: " + e.getMessage());
        }
    }

//    private synchronized void updateAt(int index, DownloadEntry entry){
//        DownloadEntry oldEntry = entries.get(index);
//        oldEntry = entry;
//    }
//
//    private synchronized void addToEntryList(DownloadEntry entry){
//        entries.add(entry);
//        Future future = executorService.submit(entry);
//
//        backup();
//    }
//
//    private synchronized void removeAt(int index) throws IndexOutOfBoundsException{
//        entries.remove(index);
//
//        backup();
//    }
//
//    public DownloadEntry getEntryAt(int idx) throws IndexOutOfBoundsException{
//        return entries.get(idx);
//    }

//    public ArrayList<DownloadEntry> getEntries(){
//        return entries;
//    }

//    public static void main(String[] args) {
//        try {
//            Controller controller = new Controller();
//            controller.addDownload( new URL("https://drive.google.com/uc?export=download&id=1Xqd8JzANoUTQi-QP4u6su1Hva5k7pX6k"),"test.pdf","downloadDir");
//
//        } catch (MalformedURLException e) {
//            System.out.println(e.getMessage());
//        }
//    }
}
