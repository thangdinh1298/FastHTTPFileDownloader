package Daemon;

import Controller.Controller;
import Downloaders.DownloadEntry;
import Util.Utils;
//import Util.ThreadPool;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Daemon {
    public Daemon() throws IOException {
        Controller.getInstance(); //Initialize the controller to avoid thread-safe problems
//        ThreadPool.getInstance();

        //try different ports
        InetAddress localHost = InetAddress.getLoopbackAddress();
        System.out.println(localHost);
        InetSocketAddress sockAddr = new InetSocketAddress(localHost, 8080);
        HttpServer server = HttpServer.create(sockAddr, 0);
        server.createContext("/download", new downloadHandler());
        server.createContext("/pause", new pauseHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class downloadHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")){
                String fileName = httpExchange.getRequestHeaders().getFirst("file-name");
                String downloadDir = httpExchange.getRequestHeaders().getFirst("download-dir");

                System.out.println(fileName + " " + downloadDir);
                if (fileName == "" || downloadDir == "" || fileName == null || downloadDir == null){
                    Utils.writeResponse(httpExchange, "Please specify the download directory and file name", 400);
                }
                InputStream is = httpExchange.getRequestBody();

                int c;
                StringBuilder body = new StringBuilder();
                while((c = is.read()) != -1){
                    body.append((char) c);
                }
                System.out.println(body.toString());
                try{
                    URL url = new URL(body.toString());
                    Controller.getInstance().addDownload(url, fileName, downloadDir);
                    httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
                    Utils.writeResponse(httpExchange, "Download added successfully", 200);
                } catch (MalformedURLException e){
                    Utils.writeResponse(httpExchange, "Invalid URL", 400);
                    return;
                }
            }
            else if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")){
                ArrayList<DownloadEntry> entries = Controller.getEntries();

                System.out.println(entries);

                StringBuilder response = new StringBuilder();

                for(int i = 0; i < entries.size(); i++){
                    response.append(i + "\t" + entries.get(i).toString() + '\n');
                }

                Utils.writeResponse(httpExchange, response.toString(), 200);
            }
        }
    }

    static class pauseHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String index = httpExchange.getRequestHeaders().getFirst("index");

            if (index == null){
                Utils.writeResponse(httpExchange, "An index was not provided", 400);
            }

            try{
                int idx = Integer.parseInt(index);
                DownloadEntry de = Controller.getEntryAt(idx);
                de.pause();
                Utils.writeResponse(httpExchange, "paused successfully", 200);
            }catch (NumberFormatException e){
                e.printStackTrace();
                Utils.writeResponse(httpExchange, "Index provided wasn't valid", 400);
            } catch (OperationNotSupportedException e) {
                e.printStackTrace();
                Utils.writeResponse(httpExchange, "Index provided wasn't valid", 500);
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Daemon();
        } catch (IOException e) {
//            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
