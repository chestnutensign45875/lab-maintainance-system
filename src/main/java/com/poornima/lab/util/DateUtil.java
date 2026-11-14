package com.poornima.lab.util;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtil {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DISPLAY_DATE_TIME = "dd MMM yyyy, hh:mm a";
    public static final String DISPLAY_DATE = "dd MMM yyyy";

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private static final SimpleDateFormat displayDateTimeFormat = new SimpleDateFormat(DISPLAY_DATE_TIME);
    private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat(DISPLAY_DATE);

    public static synchronized String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "-";
        return displayDateTimeFormat.format(timestamp);
    }

    public static synchronized String formatDate(Date date) {
        if (date == null) return "-";
        return displayDateFormat.format(date);
    }

    public static synchronized String formatStandardDate(Date date) {
        if (date == null) return "";
        return dateFormat.format(date);
    }

    public static synchronized String formatStandardDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return dateTimeFormat.format(timestamp);
    }

    public static synchronized Timestamp parseDateTime(String text) throws ParseException {
        if (text == null || text.trim().isEmpty()) return null;
        java.util.Date parsed = dateTimeFormat.parse(text.trim());
        return new Timestamp(parsed.getTime());
    }

    public static synchronized Date parseDate(String text) throws ParseException {
        if (text == null || text.trim().isEmpty()) return null;
        java.util.Date parsed = dateFormat.parse(text.trim());
        return new Date(parsed.getTime());
    }

    public static Date getSafeDate(ResultSet rs, String col) {
        try {
            String val = rs.getString(col);
            if (val == null || val.trim().isEmpty()) return null;
            val = val.trim();
            if (val.length() >= 10) {
                return Date.valueOf(val.substring(0, 10));
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static Timestamp getSafeTimestamp(ResultSet rs, String col) {
        try {
            String val = rs.getString(col);
            if (val == null || val.trim().isEmpty()) return null;
            val = val.trim();
            if (val.length() == 10) {
                val = val + " 00:00:00";
            }
            if (val.length() == 16) {
                val = val + ":00";
            }
            return Timestamp.valueOf(val);
        } catch (Exception ignored) {}
        return null;
    }
}
