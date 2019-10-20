package Util;

import Downloaders.DownloadEntry;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DownloadSpeed implements Runnable{
    private String[] downloadInfos;
    private float[] downloadSpeeds;
    private ArrayList<DownloadEntry> entries;

    public DownloadSpeed(ArrayList<DownloadEntry> entries) {
        this.entries = entries;
    }

    @Override
    public void run() {
        long[] start_time = null;
        long[] byte_num = null;
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
        long byte_remaining = 0;
        String time_left = "";
        DownloadEntry downloadEntry;

        String[] downloadInfo = null;
        float[] speeds = null;

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
                downloadInfo = new String[this.entries.size()];
                speeds = new float[this.entries.size()];
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
                        speeds[i] = (temp - byte_num[i]) * 1000000000 /
                                (float) (System.nanoTime() - start_time[i]);

                        byte_remaining = downloadEntry.getFileSize() - temp;
                        temp = temp * 100 / downloadEntry.getFileSize();

                        if(speeds[i] > 0){
                            time_left = TimeFormater.secondToHMS((long)(byte_remaining/speeds[i]));
                        }
                        if(downloadEntry.getState() == DownloadEntry.State.PAUSED ||
                                downloadEntry.getState() == DownloadEntry.State.WAITING){
                            time_left = "INF";
                        }
                        if(downloadEntry.getState() == DownloadEntry.State.COMPLETED) {
                            temp = 100;
                            speeds[i] = 0;
                            time_left = "00:00:00";
                        }

                        index = (int)temp/10;
                        downloadInfo[i] = String.format("%3d %20s %10.2f kB/s  %12s  %11s  %4d%%  %11s",
                                i, downloadEntry.getFileName(), speeds[i] / 1024, time_left,
                                progress_bar[index], temp,
                                downloadEntry.getState());
                    } else {
                        downloadInfo[i] = "";
                    }
                    this.downloadInfos = downloadInfo;
                    this.downloadSpeeds = speeds;
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
        StringBuilder builder = new StringBuilder(String.format("%3s %20s %10s kB/s  %12s  %12s  %5s  %11s\n",
                "No.", "File name", "Speed", "Time left","Progress", "%", "Status"));
        int i = 0;
        for(String s : this.downloadInfos){
            if( s != null)
                builder.append(s).append("\n");
        }
        return builder.toString();
    }

    public String getDetailDownload(int index){
        if(index >= this.entries.size())
            return "  ";
        DownloadEntry entry = this.entries.get(index);
        long bytes_downloaded = entry.getNumberOfDownloadedBytes();
        String time_left = "";
        float speed = this.downloadSpeeds[index];

        int percent = 0;
        if(entry.getFileSize() != -1)
            percent = (int)((entry.getFileSize()-bytes_downloaded)*100/entry.getFileSize());
        else
            percent = -1;
        if(entry.getState() == DownloadEntry.State.COMPLETED){
            time_left = "00:00:00";
        }
        else if(entry.getState() == DownloadEntry.State.PAUSED
                || entry.getState() == DownloadEntry.State.WAITING){
            time_left = "INF";
        }
        else
            time_left = TimeFormater.secondToHMS((long)((entry.getFileSize()-bytes_downloaded)/speed));

        StringBuilder builder = new StringBuilder("");
        builder.append("File name: ").append(entry.getFileName()).append("\n");
        builder.append("URL: ").append(entry.getDownloadLink()).append("\n\n");

        if(percent != -1){
            int i = 0;
            int length = 20;
            int scale_percent = percent/(100/length);
            builder.append("progress : [");
            while(i < length){
                builder.append("#");
                ++i;
            }
            builder.append("]  ").append(percent).append(" %\n\n");
        }
        builder.append("File size :").append(entry.getFileSize()/1024).append(" kB\n");
        builder.append("Downloaded : ").append(bytes_downloaded/1024).append(" kB\n");
        builder.append("Download Speed : ").append(speed).append(" kB/s\n");
        builder.append("Time left : ").append(time_left);




        return builder.toString();
    }
}
