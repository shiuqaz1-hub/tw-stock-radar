package com.shiuqaz1hub.twstockradar;

import java.util.Calendar;
import java.util.TimeZone;

public class MarketStatusResolver {
    public static boolean isOpen() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
        int day = now.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) return false;
        int minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        return minutes >= 9 * 60 && minutes <= 13 * 60 + 30;
    }

    public static String getStatus() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
        int day = now.get(Calendar.DAY_OF_WEEK);
        int minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) return "休市（週末）";
        if (minutes < 9 * 60) return "尚未開盤";
        if (minutes <= 13 * 60 + 30) return "開盤中";
        return "已收盤";
    }
}
