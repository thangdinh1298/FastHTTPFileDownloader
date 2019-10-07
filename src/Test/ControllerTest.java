package Test;

import Controller.Controller;
import Downloaders.DownloadEntry;
import Util.Configs;
import org.junit.Assert;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CancellationException;

public class ControllerTest {
    Controller controller;

    public ControllerTest() {
        this.controller = Controller.getInstance();
        this.deleteFolder(new File("downloadDir"));
    }

    public void deleteFolder(File folder){
        File downDir = new File(Configs.history);
        downDir.delete();
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    // pause test suite
    public void initPauseSuite(){
        //Single-threaded download
        String fileURL = "http://mirrors.evowise.com/archlinux/iso/2019.09.01/archlinux-bootstrap-2019.09.01-x86_64.tar.gz";
        String fileName = "test.pdf";
        String fileDir = "downloadDir";
        try {
            controller.addDownload(new URL(fileURL), fileName, fileDir);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //Multi-threaded download
    }
    public void pauseFinished(){
        initPauseSuite();
        DownloadEntry de =controller.getEntryAt(0);
        while(de.getState() != DownloadEntry.State.COMPLETED){
            System.out.println(de.getState());
        }
        try {
            de.pause();
        } catch (CancellationException e) {
            Assert.fail("No cancellation exception was supposed to be thrown because all threads have stopped");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        System.out.println("Done");
    }

    public static void main(String[] args) {
        ControllerTest test = new ControllerTest();
        test.pauseFinished();
    }
}
