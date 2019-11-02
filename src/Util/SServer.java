package Util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class SServer implements Runnable{
    private ServerSocket ss;
    private final int port = 6969;
    private DownloadSpeed downloadSpeed;
    private SServer sServer = null;

    public SServer(DownloadSpeed downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
        try {
            this.ss = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        OutputStream os;
//        System.out.println("hi");
        while(true){
            try {
                Socket socket = this.ss.accept();
                System.out.println("connected!");
                os = socket.getOutputStream();
                while(!socket.isClosed()){
                    if(downloadSpeed != null)
                        os.write(downloadSpeed.toString().getBytes());
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
