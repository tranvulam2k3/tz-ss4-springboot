package com.techzenacademy.management.util;

public class NormalizerUtil {
    private NormalizerUtil() {
    }

    public static String trimToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    public static String normalizeEmail(String s) {
        String v = trimToNull(s);
        return (v == null) ? null : v.toLowerCase();
    }

    public static String normalizePhone(String s) {
        String v = trimToNull(s);
        return (v == null)
                ? null
                // Loại bỏ khoảng trắng và dấu ngăn cách
                : v.replaceAll("[\\s.\\-]", "");
    }

    public static String normalizeCode(String s) {
        String v = trimToNull(s);
        return (v == null) ? null : v.toUpperCase();
    }
}
