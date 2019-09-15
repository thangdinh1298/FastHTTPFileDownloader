package Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    private static ExecutorService threadPoolInstance = null;

    private ThreadPool() {
    }

    public static ExecutorService getInstance() {
        if (threadPoolInstance == null){
            threadPoolInstance = Executors.newFixedThreadPool(8); //Read from config
        }
        return threadPoolInstance;
    }
}
