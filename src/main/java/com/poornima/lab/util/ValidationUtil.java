package com.poornima.lab.util;

import java.sql.Timestamp;
import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[0-9\\s-]{10,15}$");

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return true; // Phone is optional in some contexts
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) return false;
        String trimmed = username.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 30 && trimmed.matches("^[a-zA-Z0-9_.-]+$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidBookingRange(Timestamp start, Timestamp end) {
        if (start == null || end == null) return false;
        return end.after(start);
    }
}
