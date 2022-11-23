package com.squidspirit.chattingroom.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CurrentTime {

    public static String getTime() {
        return getTimeWithFormat("yyyy/MM/dd HH:mm:ss");
    }

    public static String getTimeWithFormat(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.TAIWAN);
        Date date = new Date();
        return formatter.format(date);
    }
}
