package ru.sejapoe.tinkab.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to convert size strings to bytes.
 */
@UtilityClass
public class SizeConverter {

    /**
     * Converts a size string to bytes.
     *
     * @param sizeString Size string to convert (e.g., "32G", "10M")
     * @return Size in bytes
     * @throws IllegalArgumentException if the size string is invalid
     */
    public static long convertToBytes(String sizeString) {
        if (sizeString == null || sizeString.isEmpty()) {
            throw new IllegalArgumentException("Size string cannot be null or empty.");
        }

        Pattern pattern = Pattern.compile("(\\d+)([KkMmGgTt]?)");
        Matcher matcher = pattern.matcher(sizeString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid size string format.");
        }

        long size = Long.parseLong(matcher.group(1));
        String suffix = matcher.group(2).toUpperCase();

        return switch (suffix) {
            case "K" -> size * 1024;
            case "M" -> size * 1024 * 1024;
            case "G" -> size * 1024 * 1024 * 1024;
            case "T" -> size * 1024L * 1024 * 1024 * 1024;
            default -> size;
        };
    }
}
