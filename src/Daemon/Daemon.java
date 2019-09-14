package Daemon;

import Controller.Controller;
import Downloaders.DownloadEntry;
import Util.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Daemon {
    public Daemon() throws IOException {
        Controller.getInstance(); //Initialize the controller to avoid thread-safe problems

        //try different ports
        InetAddress localHost = InetAddress.getLoopbackAddress();
        System.out.println(localHost);
        InetSocketAddress sockAddr = new InetSocketAddress(localHost, 8080);
        HttpServer server = HttpServer.create(sockAddr, 0);
        server.createContext("/download", new downloadHandler());
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
                    httpExchange.getResponseHeaders().add("Content-Type", "text/html");
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

                for(DownloadEntry entry: entries){
                    response.append(entry.toString() + '\n');
                }

                Utils.writeResponse(httpExchange, response.toString(), 200);
            }
        }
    }

    static class pauseHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

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
