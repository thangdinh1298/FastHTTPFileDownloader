package Util;

import Downloaders.DownloadEntry;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DownloadSpeed implements Runnable{
    private String[] downloadInfo;
    private float[][] downloadSpeeds;
    private ArrayList<DownloadEntry> entries;

    public DownloadSpeed(ArrayList<DownloadEntry> entries) {
        this.entries = entries;
    }

    private String progress_bar(int length, int percent){
        if(percent < 0)
            return "";

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

    private ArrayList<Pair<Long[], Long>> getBytesDownloadedAndTimeStamp(){
        ArrayList<Pair<Long[], Long>> bytesDownloadedAndTimeStamp = new ArrayList<>();

        int i = 0, j = 0;
        int size = this.entries.size();
        Long[] numberOfDownloadedBytes;
        DownloadEntry downloadEntry;
        while(i < size){
            downloadEntry = this.entries.get(i);
            numberOfDownloadedBytes = new Long[downloadEntry.getThreadNum()];
            j = 0;
            while(j < numberOfDownloadedBytes.length){
                numberOfDownloadedBytes[j] = downloadEntry.getNumberOfDownloadedDownloadedBytesOfThread(j);
                ++j;
            }
            bytesDownloadedAndTimeStamp.add(
                    new Pair<Long[], Long>(
                            numberOfDownloadedBytes,
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
        if(num2 < 0)
            return -1;
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

    private float[][] calcDownloadSpeeds(ArrayList<Pair<Long[], Long>> start, ArrayList<Pair<Long[], Long>> end){
        if(start == null || end == null || end.size() != start.size())
            return null;

        int size = start.size();
        float[][] downloadSpeeds = new float[size][];
        int i = 0, j = 0;
        int thread_num;
        while (i < size){
            thread_num = start.get(i).first.length;
            downloadSpeeds[i] = new float[thread_num];
            j = 0;
            while(j < downloadSpeeds[i].length){
                downloadSpeeds[i][j] = this.calcSpeed(start.get(i).second, start.get(i).first[j],
                        end.get(i).second, end.get(i).first[j]);
                ++j;
            }
            i++;
        }
        return downloadSpeeds;
    }

    private String[] getDownloadInfo(){
        if(downloadSpeeds == null)
            return null;
        String[] downloadInfo = new String[downloadSpeeds.length];

        int i = 0, j = 0;
        int ratio;
        DownloadEntry entry;
        String time_left;
        long downloadedBytes;
        float downloadSpeed;
        while(i < downloadInfo.length) {
            entry = this.entries.get(i);
            downloadedBytes = entry.getNumberOfDownloadedBytes();

            j = 0;
            downloadSpeed = 0;
            while(j < this.downloadSpeeds[i].length){
                downloadSpeed += this.downloadSpeeds[i][j];
                ++j;
            }
            time_left = this.calcTimeLeft(
                    downloadSpeed,
                    downloadedBytes,
                    entry.getFileSize(),
                    entry.getState()
            );
            ratio = this.calcPercent(downloadedBytes, entry.getFileSize());

            downloadInfo[i] = String.format(
                    "%3d %20s %10.2f kB/s  %12s  %11s  %4d%%  %11s",
                    i, entry.getFileName(), downloadSpeed / 1024, time_left,
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
        ArrayList<Pair<Long[], Long>> start = null;
        ArrayList<Pair<Long[], Long>> end = null;

        end = this.getBytesDownloadedAndTimeStamp();
        while(true){
            try {
                start = end;
                end = this.getBytesDownloadedAndTimeStamp();
                this.downloadSpeeds = this.calcDownloadSpeeds(start, end);
                _downloadInfo = this.getDownloadInfo();
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

    String getDetailDownload(int index){
        if(index >= this.downloadInfo.length || index < 0)
            return "  ";
        DownloadEntry entry = this.entries.get(index);

        long file_size = entry.getFileSize();
        long bytes_downloaded = entry.getNumberOfDownloadedBytes();

        int i = 0;
        float speed = 0;
        int ratio;
        StringBuilder threadInfo = new StringBuilder();
        while(i < this.downloadSpeeds[index].length){
            speed += this.downloadSpeeds[index][i];
            ratio = this.calcPercent(entry.getNumberOfDownloadedDownloadedBytesOfThread(i),
                    entry.getEndByteOfThread(i) - entry.getStartByteOfThread(i) +1);
            threadInfo.append(String.format(
                    "%3d %10.2f kB/s  %11s  %4d%%\n",
                    i,
                    this.downloadSpeeds[index][i] / 1024,
                    progress_bar(10, ratio),
                    ratio
            ));
            ++i;
        }

        ratio = this.calcPercent(bytes_downloaded, entry.getFileSize());
        String time_left = this.calcTimeLeft(speed, bytes_downloaded, file_size, entry.getState());

        return  "File name: " + entry.getFileName() + "\n" +
                "URL: " + entry.getDownloadLink() + "\n\n" +
                this.progress_bar(30, ratio) + ((ratio >= 0) ? ratio : "") + " %\n"+
                "\n" +
                "File size :" + entry.getFileSize() / 1024 + " kB\n" +
                "\n" +
                "Number of thread:" + entry.getThreadNum() + "\n" +
                String.format("%3s %10s kB/s  %12s  %5s\n",
                        "No.", "Speed","Progress", "%") +
                threadInfo.toString() +
                "\n" +
                "Downloaded : " + bytes_downloaded / 1024 + " kB\n" +
                "Download Speed : " + speed / 1024 + " kB/s\n" +
                "Time left : " + time_left + "\n";
    }
}
