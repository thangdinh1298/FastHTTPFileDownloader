package Client;

import Util.Utils;
import java.util.HashMap;

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
            default:
                System.out.println("Operation not supported");
        }
    }
}
