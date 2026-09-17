package com.studentmanager.util;

/**
 * Text helpers for fixed-width console tables.
 *
 * <p>{@code String.format("%-24s", value)} pads a short value but does nothing to a long
 * one, so a single 27-character subject name silently shifts every column to its right.
 * {@link #fit(String, int)} guarantees an exact width in both directions.</p>
 */
public final class TextUtil {

    private TextUtil() {
        throw new AssertionError("TextUtil is a static utility and must not be instantiated.");
    }

    /**
     * Forces a string to exactly {@code width} characters, padding with spaces or
     * truncating with a trailing period to signal that text was cut.
     *
     * @param value text to fit, {@code null} treated as empty
     * @param width target width; values below 1 are treated as 1
     */
    public static String fit(String value, int width) {
        int target = Math.max(1, width);
        String text = value == null ? "" : value;
        if (text.length() == text.codePointCount(0, text.length()) && text.length() > target) {
            return target <= 1 ? text.substring(0, target) : text.substring(0, target - 1) + ".";
        }
        if (text.length() > target) {
            return text.substring(0, target);
        }
        return text + " ".repeat(target - text.length());
    }
}
