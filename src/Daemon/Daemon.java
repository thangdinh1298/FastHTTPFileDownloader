
package Daemon;

import Controller.Controller;
import Downloaders.DownloadEntry;
import Downloaders.DownloadInfo;
import Downloaders.EntryHistory;
import Util.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Daemon {
    private static ArrayList<DownloadEntry> entries = new ArrayList<>();
    private static ExecutorService ex = Executors.newSingleThreadExecutor();
    private static HttpServer server;
    public Daemon() throws IOException {
        //try different ports
        Controller.getInstance();//loads the file
        InetAddress localHost = InetAddress.getLoopbackAddress();
        System.out.println(localHost);
        InetSocketAddress sockAddr = new InetSocketAddress(localHost, 8080);
        server = HttpServer.create(sockAddr, 0);
        server.createContext("/download", new downloadHandler());
        server.createContext("/stop", new stopHandler());
        server.createContext("/downloadInfo", new Info());
        server.setExecutor(ex); // creates a default executor
        server.start();
    }

    static class Info implements HttpHandler{

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            ArrayList<DownloadInfo> infos = Controller.getInstance().getDownloadInfo();
            httpExchange.getResponseHeaders().add("Content-Type", "text/html");
            Utils.writeResponse(httpExchange, infos, 200);
        }
    }

    static class stopHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Attempting to shutdown ");
            Daemon.server.stop(0);
            Daemon.ex.shutdown();
            System.out.println("Saving your progress");
            Controller.getInstance().pause();
            Controller.getInstance().writeHistory();
            System.out.println("Done");

//            Utils.writeResponse(httpExchange, "Shutdown", 200);
        }
    }

    static class downloadHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String fileName = httpExchange.getRequestHeaders().getFirst("file-name");
                String downloadDir = httpExchange.getRequestHeaders().getFirst("download-dir");
                String action = httpExchange.getRequestHeaders().getFirst("action");
                String strID = httpExchange.getRequestHeaders().getFirst("id");

                System.out.println(fileName + " " + downloadDir);
                if (fileName == "" || downloadDir == "") {
                    Utils.writeResponse(httpExchange, "Please specify the download directory and file name", 400);
                }
                InputStream is = httpExchange.getRequestBody();

                int c;
                StringBuilder body = new StringBuilder();
                while ((c = is.read()) != -1) {
                    body.append((char) c);
                }
                System.out.println(body.toString());

                URL url;
                try {
                    url = new URL(body.toString());
                } catch (MalformedURLException e) {
                    Utils.writeResponse(httpExchange, "Invalid URL", 400);
                    return;
                }


                if (action.equals("download"))
                    strID = "0";

                DownloadInfo info = (DownloadInfo) Controller.getInstance().handler(Integer.parseInt(strID), url, fileName, downloadDir, action);

                httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                Utils.writeResponse(httpExchange, info, 200);
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