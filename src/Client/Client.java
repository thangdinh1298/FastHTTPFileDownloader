package Client;

//import org.apache.hc.client5.http.classic.methods.HttpGet;
import Downloaders.DownloadInfo;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class Client {
    private CloseableHttpClient httpClient;
//    private ArrayList<>

    private String severURL = "http://localhost:8080/download";
    private String stopServerURL = "http://localhost:8080/stop";

    public Client(){
        this.httpClient = HttpClients.createDefault();
    }

    public void stopServer(){
        this.post(this.stopServerURL, 0,"null", "null", "null", "null");
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
                    DownloadInfo info = (DownloadInfo) ois.readObject();
                    System.out.println(info);

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

    void download(){

    }

    void pause(int id){

    }

    void resume(int id){

    }

    void update(){

    }

    void run(){
        
    }

    public static void main(String[] args) {
        String url = "http://mirrors.evowise.com/archlinux/iso/2019.09.01/archlinux-bootstrap-2019.09.01-x86_64.tar.gz";
        String filename = "filename";
        String downloadDir = "downloadDir";
        String action = "download";

        Client client = new Client();
//        client.post(0, url, filename, downloadDir, action);
        client.stopServer();
        client.close();
    }
}