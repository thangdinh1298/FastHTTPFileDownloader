package Util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
        OutputStream os = null;
//        System.out.println("hi");
        Socket socket = null;
        while(true){
            try {
                socket = this.ss.accept();
                System.out.println("connected!");
                os = socket.getOutputStream();
                while(!socket.isClosed()){
                    if(downloadSpeed != null)
                        os.write(downloadSpeed.toString().getBytes());
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
            } catch (IOException e) {
                System.out.println("Broken pipe1");
            } catch (InterruptedException e) {
                System.out.println("InterruptException!");
            } finally {
                try {
                    if (os != null)
                        os.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e){
                    //do nothing
                }
            }
        }
    }
}
