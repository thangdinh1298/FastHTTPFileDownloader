package Test;

import Downloaders.DownloadEntry;
import Downloaders.DownloadManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class DownloadManagerTest {

    @Before
    public void initTest(){
        DownloadManager.getInstance();
        deleteFolder(new File("downloadDir"));

    }

    @After
    public void tearDown(){
        cleanUpDownloadEntry();
    }

    public void deleteFolder(File folder){
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

    public void cleanUpDownloadEntry(){
        try{
            DownloadManager.getInstance().deleteDownload(0);
        } catch (Exception e){
//            Assert.fail("Could not clean up download: " + e.printStackTrace());
            e.printStackTrace();
        }
    }

    // pause test suite
    public void initDownload(){
        //Single-threaded download
        String fileURL = "https://az764295.vo.msecnd.net/stable/b37e54c98e1a74ba89e03073e5a3761284e3ffb0/VSCode-win32-x64-1.38.1.zip";
        String fileName = "test.pdf";
        String fileDir = "downloadDir";

        try {
            DownloadManager.getInstance().addDownload(new URL (fileURL), fileName, fileDir);
        } catch (IOException e) {
            Assert.fail("Could not create download");
        }

        //Multi-threaded download
    }

    @Test
    //Test that when pausing a finished download, nothing actually happened
    public void testPauseFinishedDownload(){
        initDownload();
        DownloadEntry de = DownloadManager.getInstance().getEntries().get(0);
        while(de.getState() != DownloadEntry.State.COMPLETED){
            System.out.println("state is: " + de.getState());
        }

        try {
            DownloadManager.getInstance().pauseDownload(0);
        } catch (InterruptedException | ExecutionException | CancellationException e) {
            Assert.fail();
        }
    }
    @Test (expected = CancellationException.class)
    public void testSuccessfulPause() {
        initDownload();
        DownloadEntry de = DownloadManager.getInstance().getEntries().get(0);
        try {
            Thread.sleep(1000);
            DownloadManager.getInstance().pauseDownload(0);
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail("Test failed unexpectedly");
        }


    }

//    public interface Test{
//        void run();
//    }

//    public static void main(String[] args) {
//        DownloadManagerTest testController = new DownloadManagerTest();
//        Test[] tests = new Test[] {
////                new Test() {
////                    @Override
////                    public void run() {
////                        testController.testPauseFinishedDownload();
////                    }
////                },
//                new Test() {
//                    @Override
//                    public void run() {
//                        testController.testSuccessfulPause();
//                    }
//                }
//        };
//
//        for (Test t: tests){
//            testController.deleteFolder(new File("downloadDir"));
//            t.run();
//        }
//    }
}