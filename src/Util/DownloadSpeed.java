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
        int count = percent*length/100;
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        int i = 0;
        while (i < count) {
            builder.append("#");
            ++i;
        }

        while (i < length){
            builder.append(" ");
            ++i;
        }

        builder.append("]");
        return builder.toString();
    }

    private ArrayList<Pair<Long, Long>> getBytesDownloadedAndTimeStamp(){
        ArrayList<Pair<Long, Long>> bytesDownloadedAndTimeStamp = new ArrayList<>();

        int i = 0;
        int size = this.entries.size();
        while(i < size){
            bytesDownloadedAndTimeStamp.add(
                    new Pair<Long, Long>(
                            this.entries.get(i).getNumberOfDownloadedBytes(),
                            System.nanoTime()
                    )
            );
            i++;
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
        return (bytes2-bytes1)*1000000000/(float)(t2-t1); //( byte / second )
    }

    private float[] calcDownloadSpeeds(ArrayList<Pair<Long, Long>> start, ArrayList<Pair<Long, Long>> finish){
        if(start == null || finish == null || finish.size() != start.size())
            return null;

        int size = start.size();
        float[] downloadSpeeds = new float[size];
        int i = 0;
        while (i < size){
            downloadSpeeds[i] = this.calcSpeed(
                    start.get(i).second, start.get(i).first,
                    finish.get(i).second, finish.get(i).first
            );
            i++;
        }
        return downloadSpeeds;
    }

    private String[] getDownloadInfo(float[] downloadSpeeds){
        if(downloadSpeeds == null)
            return null;
        String[] downloadInfo = new String[downloadSpeeds.length];

        int i = 0;
        int ratio;
        DownloadEntry entry;
        String time_left;
        long downloadedBytes;
        while(i < downloadInfo.length){
            entry = this.entries.get(i);
            downloadedBytes = entry.getNumberOfDownloadedBytes();

            time_left = this.calcTimeLeft(
                    downloadSpeeds[i],
                    downloadedBytes,
                    entry.getFileSize(),
                    entry.getState()
            );
            ratio = this.calcPercent(downloadedBytes, entry.getFileSize());

            downloadInfo[i] = String.format(
                    "%3d %20s %10.2f kB/s  %12s  %11s  %4d%%  %11s",
                    i, entry.getFileName(), downloadSpeeds[i] / 1024, time_left,
                    progress_bar(10, ratio), ratio,
                    entry.getState()
            );
            i++;
        }
        return downloadInfo;
    }

    @Override
    public void run() {
        String[] _downloadInfo = null;
        ArrayList<Pair<Long, Long>> start = null;
        ArrayList<Pair<Long, Long>> end = null;

        end = this.getBytesDownloadedAndTimeStamp();
        while(true){
            try {
                start = end;
                end = this.getBytesDownloadedAndTimeStamp();
                this.downloadSpeeds = this.calcDownloadSpeeds(start, end);
                _downloadInfo = this.getDownloadInfo(this.downloadSpeeds);
                if(_downloadInfo != null)
                    this.downloadInfo = _downloadInfo;

                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (IndexOutOfBoundsException e){
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
