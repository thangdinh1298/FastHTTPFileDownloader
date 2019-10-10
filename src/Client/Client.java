package Client;

import Util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Client {
    private final String DAEMON_ADDR = "http://localhost:8080";

    public Client() {

    }

    public void newDownload(String fileName, String downloadDir, String url){
        String endpoint = DAEMON_ADDR +  "/" + "download";
        HashMap<String, String> headers = new HashMap<>();

        headers.put("file-name", fileName);
        headers.put("download-dir", downloadDir);

        Utils.doPost(endpoint, headers, url);
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

    public void getDownloadSpeed(){
        System.out.println("here");
        String ip = "localhost";
        int port = 6969;
        try (Socket socket = new Socket(ip, port)){
            System.out.println("connected!!");
            byte[] buff = new byte[1024];
            InputStream is = socket.getInputStream();
            int timeout = 5;
            String temp = "";
            String str;
            while(true){
                if(timeout <= 0){
                    break;
                }
                if(is.available() == 0){
                    TimeUnit.MILLISECONDS.sleep(1000);
                    timeout--;
                    continue;
                }
                timeout = 5;
                this.clear();
                is.read(buff);
                str = new String(buff).trim();
                System.out.println(str);
                if(str.equals(temp))
                    timeout--;
                temp = str;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void clear(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {
        Client client = new Client();

        System.out.println(args.length);
        System.out.println(args[0]);

        String index;

        switch (args[0]){
            case "newdownload":
                String url = args[1];
                String downloadDir = args[2];
                String fileName = args[3];
                client.newDownload(fileName, downloadDir, url);
                break;
            case "getdownload":
                client.getAllDownloads();
                break;
            case "pausedownload":
                index = args[1];
                client.pauseDownload(index);
                break;
            case "resumedownload":
                index = args[1];
                client.resumeDownload(index);
                break;
            case "deletedownload":
                index = args[1];
                client.deleteDownload(index);
                break;
            case "getDownloadSpeed":
                client.getDownloadSpeed();
                break;
            default:
                System.out.println("Operation not supported");
        }
    }
}
