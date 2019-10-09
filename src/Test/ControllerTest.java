package Test;

import Controller.Controller;
import Downloaders.DownloadEntry;
import Util.Configs;
import org.junit.Assert;


import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CancellationException;

public class ControllerTest {
    Controller controller;

    public ControllerTest() {
        this.controller = Controller.getInstance();
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
    public void initDownload(){
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

    public void cleanUpDownloadEntry(int index){
        try{
            controller.deleteDownload(0);
        } catch (Exception e){
            Assert.fail("Could not clean up download");
        }
    }

    public boolean paused(DownloadEntry de){
        try {
            de.pause();
        } catch (CancellationException e) {
            return true;
        } catch (Exception e) {

        }
        return false;
    }
    //Test that when pausing a finished download, nothing actually happened
    public void testPauseFinishedDownload(){
        initDownload();
        DownloadEntry de =controller.getEntryAt(0);
        while(de.getState() != DownloadEntry.State.COMPLETED){
            System.out.println(de.getState());
        }

        Assert.assertFalse(paused(de));
        cleanUpDownloadEntry(0);
    }

    public void testSuccessfulPause() {
        initDownload();
        DownloadEntry de =controller.getEntryAt(0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.fail("Failed while downloading");
        }

        Assert.assertTrue(paused(de));
    }

    public interface Test{
        void run();
    }

    public static void main(String[] args) {
        ControllerTest testController = new ControllerTest();
        testController.testPauseFinishedDownload();
        Test[] tests = new Test[] {
                new Test() {
                    @Override
                    public void run() {
                        testController.testPauseFinishedDownload();
                    }
                },
                new Test() {
                    @Override
                    public void run() {
                        testController.testSuccessfulPause();
                    }
                }
        };

        for (Test t: tests){
            testController.deleteFolder(new File("downloadDir"));
            t.run();
        }
    }
}
