package Daemon;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class StoppableServer implements Runnable{
    private HttpServer server;

    public StoppableServer() {
        this.server = null;
    }

    public void init() throws IOException {
        InetAddress localHost = InetAddress.getLoopbackAddress();
        System.out.println(localHost);
        InetSocketAddress sockAddr = new InetSocketAddress(localHost, 8080);
        this.server = HttpServer.create(sockAddr, 0);
    }

    public void createContext(String path, HttpHandler httpHandler){
        this.server.createContext(path, httpHandler);
    }

    public void setExecutor(Executor executor){
        this.server.setExecutor(executor);
    }


    @Override
    public void run() {
        this.server.start();
    }

    public void stop(){
        this.server.stop(0);
    }
}
