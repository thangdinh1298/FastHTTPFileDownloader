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
        int index = 0;
        DownloadEntry downloadEntry;
        while(true){
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(this.entries.size() == 0){
                continue;
            }
            if(this.downloadInfos == null
                    || start_time == null
                    || this.downloadInfos.length != this.entries.size()){
                System.out.println("resizing....");
                byte_num = new long[this.entries.size()];
                start_time = new long[this.entries.size()];
                this.downloadInfos = new String[this.entries.size()];
            }

            try {
                for(i = 0; i < byte_num.length; ++i){
                    downloadEntry = this.entries.get(i);

                    if (downloadEntry != null) {
                        start_time[i] = System.nanoTime();
                        byte_num[i] = downloadEntry.getNumberOfDownloadedBytes();
                    }
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(i = 0; i < byte_num.length; i++){
                    downloadEntry = this.entries.get(i);
                    if (downloadEntry != null) {
                        temp = downloadEntry.getNumberOfDownloadedBytes();
                        speed = (temp - byte_num[i]) * 1000000000 /
                                (float) (System.nanoTime() - start_time[i]);
                        temp = temp * 100 / downloadEntry.getFileSize();

                        if(downloadEntry.getState() == DownloadEntry.State.COMPLETED) {
                            temp = 100;
                            speed = 0;
                        }
                        index = (int)temp/10;
                        this.downloadInfos[i] = String.format("%3d %20s %10.2f kB/s  %11s  %4d%%  %11s",
                                i, downloadEntry.getFileName(), speed / 1024,
                                progress_bar[index], temp,
                                downloadEntry.getState());
                    } else {
                        this.downloadInfos[i] = "";
                    }

                }
            } catch (IndexOutOfBoundsException e){
                //do nothing
            }
        }
    }


    @Override
    public String toString(){
        if(this.downloadInfos == null || this.downloadInfos.length == 0){
            return "";
        }
        StringBuilder builder = new StringBuilder(String.format("%3s %20s %10s kB/s  %12s  %5s  %11s\n",
                "No.", "File name", "Speed", "Progress", "%", "Status"));
        int i = 0;
        for(String s : this.downloadInfos){
            if( s != null)
                builder.append(s).append("\n");
        }
        return builder.toString();
    }
}
