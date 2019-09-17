package Controller;

import Downloaders.DownloaderFactory;
import Downloaders.DownloadEntry;
import Util.BackupManager;
import Util.Configs;

import java.io.IOException;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.ArrayList;

public class Controller {
    private static BackupManager backupManager;
    private static ArrayList<DownloadEntry> entries;
    private static Controller controller = null;

    private Controller() {
    }

    public static Controller getInstance() {
        if (controller == null) {
            controller = new Controller();
            backupManager = new BackupManager();
            entries = new ArrayList<>();
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
        System.out.println(Controller.entries);
        try{
            boolean resumable = pollForRangeSupport(url);
            Long fileSize = pollForFileSize(url);

            System.out.println(resumable + " "  + fileSize);

            DownloadEntry de = DownloaderFactory.getDownloadEntry(resumable,
                    fileSize, url, downloadDir, fileName);
            de.initDownload(); //todo: should throw error if de is null
            System.out.println("adding entries");
            Controller.getInstance().addToEntryList(de);

            System.out.println("returning from add download");

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private synchronized void updateAt(int index, DownloadEntry entry){
        DownloadEntry oldEntry = entries.get(index);
        oldEntry = entry;
    }

    private synchronized void addToEntryList(DownloadEntry entry){
        entries.add(entry);
        BackupManager.backup(entries);
    }

    private synchronized void removeAt(int index) throws IndexOutOfBoundsException{
        entries.remove(index);
        BackupManager.backup(entries);
    }

    public DownloadEntry getEntryAt(int idx){
        return entries.get(idx);
    }

    public ArrayList<DownloadEntry> getEntries(){
        return entries;
    }

    public static void main(String[] args) {
//        try {
//            Controller controller = new Controller();
//            controller.addDownload( new URL("https://cdimage.kali.org/kali-2019.3/kali-linux-2019.3-amd64.iso"));
//
//        } catch (MalformedURLException e) {
//            System.out.println(e.getMessage());
//        }
    }
}
