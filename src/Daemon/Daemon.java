package Daemon;

import Controller.Controller;
import Downloaders.DownloadEntry;
import Downloaders.DownloadManager;
import Util.DownloadSpeed;
import Util.SServer;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Daemon {
    public Daemon() throws IOException {
//        ThreadPool.getInstance();
        DownloadManager dm = DownloadManager.getInstance();

        DownloadSpeed ds = new DownloadSpeed(dm.getEntries());
        Thread tds = new Thread(ds);
        tds.start();

        Thread tss = new Thread(new SServer(ds));
        tss.start();

        //todo: try different ports
        InetAddress localHost = InetAddress.getLoopbackAddress();
        System.out.println(localHost);
        InetSocketAddress sockAddr = new InetSocketAddress(localHost, 8080);
        HttpServer server = HttpServer.create(sockAddr, 0);
        server.createContext("/download", new downloadHandler());
        server.createContext("/pause", new pauseHandler());
        server.createContext("/resume", new resumeHandler());
        server.createContext("/delete", new deleteHandler());
        server.setExecutor(Executors.newSingleThreadExecutor());
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
                    Utils.writeResponse(httpExchange, "Please specify the download directory and file name");
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
                    Controller.addDownload(url, fileName, downloadDir);
                    httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
                    Utils.writeResponse(httpExchange, "Download added successfully");
                } catch (MalformedURLException e){
                    Utils.writeResponse(httpExchange, "Invalid URL");
                    return;
                }
            }
            else if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")){
                ArrayList<DownloadEntry> entries = DownloadManager.getInstance().getEntries();


                StringBuilder response = new StringBuilder();

                for(int i = 0; i < entries.size(); i++){
                    response.append(i + "\t" + entries.get(i).toString() + '\n');
                }

                System.out.println(response.toString());

                Utils.writeResponse(httpExchange, response.toString());
            }
        }
    }

    static class pauseHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String index = httpExchange.getRequestHeaders().getFirst("index");

            if (index == null){
                Utils.writeResponse(httpExchange, "An index was not provided");
            }

            try{
                int idx = Integer.parseInt(index);

                Controller.pauseDownload(idx);
            }catch (NumberFormatException e){
                e.printStackTrace();
                Utils.writeResponse(httpExchange, "Index provided isn't a valid number");
                return;
            } catch (IndexOutOfBoundsException e){
                Utils.writeResponse(httpExchange, "Index provided was out of bound");
                return;
            } /*catch (OperationNotSupportedException e) {
                e.printStackTrace();
                Utils.writeResponse(httpExchange, "Pause not supported for this download");
            } */catch (InterruptedException | ExecutionException e) {
                Utils.writeResponse(httpExchange, "Download was not successful");
            } catch (CancellationException e){
                Utils.writeResponse(httpExchange, "paused successfully");
                return;
            }
        }
    }

    static class resumeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Request received");
            String index = httpExchange.getRequestHeaders().getFirst("index");

            if (index == null){
                Utils.writeResponse(httpExchange, "An index was not provided");
            }

            try{
                int idx = Integer.parseInt(index);
                Controller.resumeDownload(idx);
                Utils.writeResponse(httpExchange, "resumed successfully");
            }catch (NumberFormatException e){
                e.printStackTrace();
                Utils.writeResponse(httpExchange, "Index provided wasn't valid");
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                Utils.writeResponse(httpExchange, "Index provided was out of bound");
            }
        }
    }

    static class deleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String index = httpExchange.getRequestHeaders().getFirst("index");

            if (index == null){
                Utils.writeResponse(httpExchange, "An index was not provided");
            }

            try{
                int idx = Integer.parseInt(index);
                Controller.deleteDownload(idx);
                Utils.writeResponse(httpExchange, "deleted successfully");
            }catch (NumberFormatException e){
                e.printStackTrace();
                Utils.writeResponse(httpExchange, "Index provided wasn't a number");
            }catch (IndexOutOfBoundsException e){
                System.out.println("Index out of bound");
                Utils.writeResponse(httpExchange, "Index provided wasn't valid");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
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
