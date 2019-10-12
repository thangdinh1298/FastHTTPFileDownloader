package Util;

import Downloaders.DownloadEntry;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DownloadSpeed implements Runnable{
    private String[] downloadInfos;
    private ArrayList<DownloadEntry> entries;

    public DownloadSpeed(ArrayList<DownloadEntry> entries) {
        this.entries = entries;
    }

    @Override
    public void run() {
        long[] start_time = null;
        long[] byte_num = null;

        float speed;
        long temp;

        String[] progress_bar = {
                "[          ]",
                "[#         ]",
                "[##        ]",
                "[###       ]",
                "[####      ]",
                "[#####     ]",
                "[######    ]",
                "[#######   ]",
                "[########  ]",
                "[######### ]",
                "[##########]"
        };

        int i = 0;
        DownloadEntry downloadEntry;
        while(true){
            if(this.entries.size() == 0){
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if(this.downloadInfos == null
                    || start_time == null
                    || this.downloadInfos.length != this.entries.size()){
                byte_num = new long[this.entries.size()];
                start_time = new long[this.entries.size()];
                this.downloadInfos = new String[this.entries.size()];
            }

            for(i = 0; i < byte_num.length; ++i){
                downloadEntry = this.entries.get(i);
                if(downloadEntry != null){
                    start_time[i] = System.nanoTime();
                    byte_num[i] = downloadEntry.getNumberOfDownloadedBytes();
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(i = 0; i < byte_num.length; i++){
                downloadEntry = this.entries.get(i);
                if(downloadEntry != null) {
                    temp = downloadEntry.getNumberOfDownloadedBytes();
                    speed = (temp - byte_num[i]) * 1000000000 /
                            (float) (System.nanoTime() - start_time[i]);
                    temp = temp * 100 / downloadEntry.getFileSize();
//                    this.downloadInfos[i] = String.format("%20s20.2%f %s %4d%% %s",
//                            downloadEntry.getFileName(), speed,
//                            progress_bar[(int) temp / 10], temp,
//                            downloadEntry.getState());
                    this.downloadInfos[i] = String.format("%20s %10.2f kB/s %s %4d%% %s     ",
                            downloadEntry.getFileName(), speed/1024,
                            progress_bar[(int) temp / 10], temp,
                            downloadEntry.getState());
                }
                else{
                    this.downloadInfos[i] = "None";
                }
            }
        }
    }


    @Override
    public String toString(){
        if(this.downloadInfos == null || this.downloadInfos.length == 0){
            return "";
        }
        StringBuilder builder = new StringBuilder("");
        int i = 0;
        for(String s : this.downloadInfos){
            builder.append(s).append("\n");
        }
        return builder.toString();
    }
}
