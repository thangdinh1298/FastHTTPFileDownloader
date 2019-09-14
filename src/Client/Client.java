package Client;

//import org.apache.hc.client5.http.classic.methods.HttpGet;
import Downloaders.DownloadInfo;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private CloseableHttpClient httpClient;
    private Map<Integer, DownloadInfo> downloadInfoMap;
    private Scanner input;

    private String severURL = "http://localhost:8080/download";
    private String stopServerURL = "http://localhost:8080/stop";

    public Client(){
        this.httpClient = HttpClients.createDefault();
        this.downloadInfoMap = new HashMap<>();
        this.input = new Scanner(System.in);
    }

    public void stopServer() {
        this.post(this.stopServerURL, 0, "null", "null", "null", "null");
    }

    public void post(int id, String url, String filename, String downloadDir, String action){
        this.post(this.severURL, id, url, filename, downloadDir, action);
    }

    public void post(String severURL, int id, String url, String filename, String downloadDir, String action){
        HttpPost httpPost = new HttpPost(severURL);
        httpPost.setHeader("file-name", filename);
        httpPost.setHeader("download-dir", downloadDir);
        httpPost.setHeader("action", action);
        httpPost.setHeader("id", id);

        CloseableHttpResponse response = null;
        try{
            StringEntity entity = new StringEntity(url);
            httpPost.setEntity(entity);
            response = this.httpClient.execute(httpPost);

            HttpEntity httpEntity = response.getEntity();
            if(httpEntity != null){
                try(InputStream inputStream = httpEntity.getContent()){
                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    DownloadInfo info;
                    try {
                        while ((info = (DownloadInfo) ois.readObject()) != null) {
                            this.downloadInfoMap.put(info.getId(), info);
                            this.displayDownload(info.getId());
                        }
                    }
                    catch (EOFException e){
                        System.out.println("end of file!");
                    }

//                    int i;
//                    StringBuilder builder = new StringBuilder();
//                    while((i = inputStream.read()) != -1){
//                        builder.append((char)i);
//                    }
//                    System.out.println(builder.toString());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(){
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayDownload(int id){
        System.out.println("-------------------------");
        System.out.printf("%4s %10.50s %10.40s %10sB %10.15s\n", "id", "download-dir",
                "file name", "file size", "state");
        System.out.println(this.downloadInfoMap.get(id));
        System.out.println("-------------------------");
    }

    private void displayDownloads(){
        System.out.println("-------------------------");
        System.out.printf("%.4s %.50s %.40s %.10sB %.15s\n", "id", "download-dir",
                "file name", "file size", "state");
        for(Integer key: this.downloadInfoMap.keySet()){
            System.out.println(this.downloadInfoMap.get(key));
        }
        System.out.println("-------------------------");
    }

    private void download(){
        String url = "http:pause//mirrors.evowise.com/archlinux/iso/2019.09.01/archlinux-bootstrap-2019.09.01-x86_64.tar.gz";
        String filename = "filename";
        String downloadDir = "downloadDir";
        String action = "download";
        this.post(0, url, filename, downloadDir, action);
    }

    private void pause(){
        System.out.println("enter id paused!");
        int id = input.nextInt();
        DownloadInfo info = this.downloadInfoMap.get(id);
        this.post(0, info.getDownloadLink(), info.getFileName(), info.getDownloadDir(), "pause");
    }

    private void resume(){
        System.out.println("enter id resumed!");
        int id = input.nextInt();
        DownloadInfo info = this.downloadInfoMap.get(id);
        this.post(0, info.getDownloadLink(), info.getFileName(), info.getDownloadDir(), "resume");
    }

    private void update(){
        System.out.println("updating ...");
        this.post(-1, "none", "none", "none", "update");
        this.displayDownloads();
        System.out.println("updated!!");
    }

    public void run(){
        int command;
        boolean exist = false;
        while(!exist){
            System.out.println("--------------options----------------");
            System.out.println("1: add download");
            System.out.println("2: pause");
            System.out.println("3: resume");
            System.out.println("4: print all history");
            System.out.println("5: update history//downloading info");
            System.out.println("6: close server");
            System.out.println("Enter number to execute command!");
            command = this.input.nextInt();
            switch (command){
                case 1:
                    this.download();
                    break;
                case 2:
                    this.pause();
                    break;
                case 3:
                    this.resume();
                    break;
                case 4:
                    this.displayDownloads();
                    break;
                case 5:
                    this.update();
                    break;
                case 6:
                    exist = true;
                    this.stopServer();
                    break;
                default:
                    System.out.println("*****out of commands!*********");
                    break;
            }
        }
    }



    public static void main(String[] args) {
        String url = "http://mirrors.evowise.com/archlinux/iso/2019.09.01/archlinux-bootstrap-2019.09.01-x86_64.tar.gz";
        String filename = "filename";
        String downloadDir = "downloadDir";
        String action = "download";

        Client client = new Client();
        client.post(0, url, filename, downloadDir, action);
//        client.stopServer();
        client.close();

//        Client client = new Client();
//        client.run();
//        client.close();
    }
}