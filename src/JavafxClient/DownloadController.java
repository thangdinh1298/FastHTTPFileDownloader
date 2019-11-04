package JavafxClient;

import Util.Utils;
import Util.Window;
import com.sun.javafx.fxml.builder.JavaFXSceneBuilder;
import javafx.beans.property.SimpleStringProperty;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DownloadController {
//    public static ArrayList<DownloadModel> downloadModels = new ArrayList<>();

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

        try {
            Utils._doPost(endpoint, headers, url);
        } catch (Exception e) {
            e.printStackTrace();
            Dialog.showDialog("Warning", e.getMessage());
        }
//        Controller.updateTable();
    }

    public void getAllDownloads(){
        String endpoint = DAEMON_ADDR +  "/" + "download";
        System.out.println(endpoint);
        try {
            Utils._doGet(endpoint, new HashMap<>());
        } catch (IOException e) {
            e.printStackTrace();
            Dialog.showDialog("Warning", e.getMessage());
        }
    }

    public void pauseDownload(String index){
        String endpoint = DAEMON_ADDR +  "/" + "pause";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("index", index);
        try {
            Utils._doGet(endpoint, headers);
        } catch (IOException e) {
            e.printStackTrace();
            Dialog.showDialog("Warning", e.getMessage());
        }
    }

    public void resumeDownload(String index){
        String endpoint = DAEMON_ADDR +  "/" + "resume";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("index", index);
        try {
            Utils._doGet(endpoint, headers);
        } catch (IOException e) {
            e.printStackTrace();
            Dialog.showDialog("Warning", e.getMessage());
        }
    }

    public void deleteDownload(String index){
        String endpoint = DAEMON_ADDR +  "/" + "deleteDownload";
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("index", index);
        try {
            Utils._doGet(endpoint, headers);
        } catch (IOException e) {
            e.printStackTrace();
            Dialog.showDialog("Warning", e.getMessage());
        }
    }

    public void getDownloadSpeed(String str_index){
        String ip = "localhost";
        int port = 6969;
        try (Socket socket = new Socket(ip, port)){
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

            String[] downloads = str.split("\n");

            for(int i = 1; i < downloads.length; i++) {
                String download = downloads[i];
                System.out.println(download);
                String downloadInfos[] = download.split("\\s+");
                String id = downloadInfos[1];
                String fileName = downloadInfos[2];
                String speed = downloadInfos[3] +" "+ downloadInfos[4];
                String timeLeft = downloadInfos[downloadInfos.length - 5];
                String status = downloadInfos[downloadInfos.length - 1];
                String px = downloadInfos[downloadInfos.length -2];
                DownloadModel downloadModel = new DownloadModel(id, fileName, speed, timeLeft,px, status);
                Controller.addOrUpdate(i -1, downloadModel);
            }
        } catch (IOException e) {
            System.out.println("IOException!");
        }

    }

    public static void main(String[] args) {
        DownloadController.getInstance().getDownloadSpeed("-1");
    }
}
