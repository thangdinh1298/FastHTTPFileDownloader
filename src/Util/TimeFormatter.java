package Util;

public class TimeFormatter {
    public static String secondToHMS(long seconds){
        StringBuilder builder = new StringBuilder("");
        int hour = (int)seconds/3600;
        seconds %= 3600;
        int minute = (int)seconds/60;
        seconds %= 60;
        if( hour < 10){
            builder.append("0").append(hour);
        }
        else
            builder.append(hour);

        builder.append(":");

        if( minute < 10){
            builder.append('0').append(minute);
        }
        else
            builder.append(minute);

        builder.append(":");
        if( seconds < 10){
            builder.append("0").append(seconds);
        }
        else
            builder.append(seconds);
        
        return builder.toString();
    }
}
