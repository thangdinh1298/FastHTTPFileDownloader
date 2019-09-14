package Client;

import Util.Utils;

import java.nio.file.Paths;
import java.util.HashMap;

public class Client {
    private final String DAEMON_ADDR = "localhost:8080";

    public Client() {

    }

    public void newDownload(String fileName, String downloadDir, String url){
        String endpoint = Paths.get(DAEMON_ADDR, "download").toString();
        HashMap<String, String> headers = new HashMap<>();

        headers.put("file-name", fileName);
        headers.put("download-dir", downloadDir);

        Utils.doPost(endpoint, headers, url);
    }

    public void getAllDownloads(){
        String endpoint = Paths.get(DAEMON_ADDR, "download").toString();
        Utils.doGet(endpoint, new HashMap<>());
    }

    public void pauseDownload(String index){
        String endpoint = Paths.get(DAEMON_ADDR, "pause").toString();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("index", index);
        Utils.doGet(endpoint, headers);
    }

    public static void main(String[] args) {
        Client client = new Client();

        switch (args[1]){
            case "newdownload":
                String url = args[2];
                String downloadDir = args[3];
                String fileName = args[4];
                client.newDownload(fileName, downloadDir, url);
                break;
            case "getdownload":
                client.getAllDownloads();
                break;
            case "pausedownload":
                String index = args[2];
                client.pauseDownload(index);
                break;
            default:
                System.out.println("Operation not supported");
        }
    }
}
