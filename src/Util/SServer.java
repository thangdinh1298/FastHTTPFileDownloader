package Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
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
        InputStream is = null;
//        System.out.println("hi");
        Socket socket = null;
        int index = -1;
        byte[] buff = new byte[16];
        while(true){
            try {
                socket = this.ss.accept();
                System.out.println("connected!");
                os = socket.getOutputStream();
                is = socket.getInputStream();

                Arrays.fill(buff, (byte) 0);
                is.read(buff);
                System.out.println("|"+new String(buff).trim()+"|");
                index = Integer.parseInt(new String(buff).trim());
                while(!socket.isClosed()){
                    if(downloadSpeed != null) {
                        if(index == -1)
                            os.write(downloadSpeed.toString().getBytes());
                        else
                            os.write(downloadSpeed.getDetailDownload(index).getBytes());
                    }
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
