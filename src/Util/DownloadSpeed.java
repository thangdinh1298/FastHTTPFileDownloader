package Util;

import Downloaders.DownloadEntry;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DownloadSpeed implements Runnable{
    private String[] downloadInfo;
    private float[] downloadSpeeds;
    private ArrayList<DownloadEntry> entries;

    public DownloadSpeed(ArrayList<DownloadEntry> entries) {
        this.entries = entries;
    }

    private String progress_bar(int length, int percent){
        int count = percent*length;
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        int i = 0;
        while (i++ < count) {
            builder.append("#");
        }

        while (i++ < length){
            builder.append(" ");
        }

        builder.append("]");
        return builder.toString();
    }

    private ArrayList<Pair<Long, Long>> getBytesDownloadedAndTimeStamp(){
        ArrayList<Pair<Long, Long>> bytesDownloadedAndTimeStamp = new ArrayList<>();

        int i = 0;
        int size = this.entries.size();
        while(i++ < size){
            bytesDownloadedAndTimeStamp.add(
                    new Pair<Long, Long>(
                            this.entries.get(i).getNumberOfDownloadedBytes(),
                            System.nanoTime()
                    )
            );
        }

        if(bytesDownloadedAndTimeStamp.size() != 0)
            return bytesDownloadedAndTimeStamp;
        return null;
    }

    private int calcPercent(long num1, long num2){
        return (int)(num1*100/num2);
    }

    private String calcTimeLeft(float speed, long downloadedBytes, long fileSize, DownloadEntry.State state){
        if (state == DownloadEntry.State.WAITING){
            return "INF";
        }
        if (state == DownloadEntry.State.PAUSED || state == DownloadEntry.State.COMPLETED){
            return "00:00:00";
        }
        if(fileSize == -1){
            return "N/A";
        }
        return TimeFormatter.secondToHMS((long)((fileSize-downloadedBytes)/speed));
    }

    private float calcSpeed(long t1, long bytes1, long t2, long bytes2)
    {
        return (bytes1-bytes2)*1000000000/(float)(t1-t2); //( byte / second )
    }

    private void calcSpeedDownloads(ArrayList<Pair<Long, Long>> start, ArrayList<Pair<Long, Long>> finish){
        if(start == null || finish == null || finish.size() != start.size())
            return;

        int size = start.size();
        float[] downloadSpeeds = new float[size];
        int i = 0;
        while (i++ < size){
            downloadSpeeds[i] = this.calcSpeed(
                    start.get(i).second, start.get(i).first,
                    finish.get(i).second, finish.get(i).first
            );
        }
        this.downloadSpeeds = downloadSpeeds;
    }

    @Override
    public void run() {
        long[] start_time = null;
        long[] byte_num = null;
        long temp;

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
            if(this.downloadInfo == null
                    || start_time == null
                    || this.downloadInfo.length != this.entries.size()){
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
                            time_left = TimeFormatter.secondToHMS((long)(byte_remaining/speeds[i]));
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

                        downloadInfo[i] = String.format("%3d %20s %10.2f kB/s  %12s  %11s  %4d%%  %11s",
                                i, downloadEntry.getFileName(), speeds[i] / 1024, time_left,
                                progress_bar(10, (int)temp), temp,
                                downloadEntry.getState());
                    } else {
                        downloadInfo[i] = "";
                    }
                    this.downloadInfo = downloadInfo;
                    this.downloadSpeeds = speeds;
                }
            } catch (IndexOutOfBoundsException e){
                //do nothing
            }
        }
    }


    @Override
    public String toString(){
        if(this.downloadInfo == null || this.downloadInfo.length == 0){
            return "";
        }
        StringBuilder builder = new StringBuilder(String.format("%3s %20s %10s kB/s  %12s  %12s  %5s  %11s\n",
                "No.", "File name", "Speed", "Time left","Progress", "%", "Status"));
        int i = 0;
        for(String s : this.downloadInfo){
            if( s != null)
                builder.append(s).append("\n");
        }
        return builder.toString();
    }

    public String getDetailDownload(int index){
        if(index >= this.downloadInfo.length || index < 0)
            return "  ";
        DownloadEntry entry = this.entries.get(index);
        long bytes_downloaded = entry.getNumberOfDownloadedBytes();
        String time_left = "";
        float speed = this.downloadSpeeds[index];

        int percent = 0;
        if(entry.getFileSize() != -1)
            percent = (int)(bytes_downloaded*100/entry.getFileSize());
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
            time_left = TimeFormatter.secondToHMS((long)((entry.getFileSize()-bytes_downloaded)/speed));

        StringBuilder builder = new StringBuilder("");
        builder.append("File name: ").append(entry.getFileName()).append("\n");
        builder.append("URL: ").append(entry.getDownloadLink()).append("\n\n");

        if(percent != -1){
            builder.append(this.progress_bar(20, percent)).append(percent).append(" %\n\n");
        }
        builder.append("File size :").append(entry.getFileSize()/1024).append(" kB\n");
        builder.append("Downloaded : ").append(bytes_downloaded/1024).append(" kB\n");
        builder.append("Download Speed : ").append(speed/1024).append(" kB/s\n");
        builder.append("Time left : ").append(time_left).append("\n");

        return builder.toString();
    }
}
