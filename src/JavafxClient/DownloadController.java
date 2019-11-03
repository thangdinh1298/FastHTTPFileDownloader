package JavafxClient;

import Util.Utils;
import Util.Window;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DownloadController {
    public static ArrayList<DownloadModel> downloadModels = new ArrayList<>();

    private final String DAEMON_ADDR = "http://localhost:8080";

    private static final DownloadController INSTANCE = new DownloadController();

    private DownloadController() {}

    public static DownloadController getInstance() {
        return INSTANCE;
    }

    public void newDownload(String fileName, String downloadDir, String url){
        String endpoint = DAEMON_ADDR +  "/" + "download";
        HashMap<String, String> headers = new HashMap<>();

        headers.put("file-name", fileName);
        headers.put("download-dir", downloadDir);

        Utils.doPost(endpoint, headers, url);
//        Controller.updateTable();
    }

    public void getAllDownloads(){
        String endpoint = DAEMON_ADDR +  "/" + "download";
        System.out.println(endpoint);
        Utils.doGet(endpoint, new HashMap<>());
    }

    public void pauseDownload(String index){
        String endpoint = DAEMON_ADDR +  "/" + "pause";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("index", index);
        Utils.doGet(endpoint, headers);
    }

    public void resumeDownload(String index){
        String endpoint = DAEMON_ADDR +  "/" + "resume";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("index", index);
        Utils.doGet(endpoint, headers);
    }

    public void deleteDownload(String index){
        String endpoint = DAEMON_ADDR +  "/" + "deleteDownload";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("index", index);
        Utils.doGet(endpoint, headers);
    }

    public void getDownloadSpeed(String str_index){
        String ip = "localhost";
        int port = 6969;
        try (Socket socket = new Socket(ip, port)){
            System.out.println("connected!!");
            byte[] buff = new byte[1024];
            OutputStream os = socket.getOutputStream();
            os.write(str_index.getBytes());

            InputStream is = socket.getInputStream();
            String str;
            int last_index = 0;
            is.read(buff);
            str = new String(buff);//.replaceAll("\\s+$", "");
            last_index = str.lastIndexOf('\n');
            if(last_index >= 0)
                str = String.valueOf(str.toCharArray(), 0, last_index);
            else
                str = "";

            System.out.println("gag"+str);

            String[] downloads = str.split("\n");
            downloadModels.clear();
            System.out.println(str.length());
            if (str.length() < 1) return;
            for(int i = 1; i < downloads.length; i++) {
                String download = downloads[i];
                String downloadInfos[] = download.split("\\s+");
                for(String s: downloadInfos) {
                    System.out.println(s);
                }
                String id = downloadInfos[1];
                String fileName = downloadInfos[2];
                String speed = downloadInfos[3] +" "+ downloadInfos[4];
                String timeLeft = downloadInfos[downloadInfos.length - 4];
                String status = downloadInfos[downloadInfos.length - 2].equalsIgnoreCase("100%") ? "COMPLETED" :downloadInfos[downloadInfos.length - 2];
                DownloadModel downloadModel = new DownloadModel(id, fileName, speed, timeLeft, status);
                downloadModels.add(downloadModel);
            }
            Controller.downloadModels.setAll(downloadModels);
        } catch (IOException e) {
            System.out.println("IOException!");
        }

    }

    public static void main(String[] args) {
        DownloadController.getInstance().getDownloadSpeed("-1");
    }
}
