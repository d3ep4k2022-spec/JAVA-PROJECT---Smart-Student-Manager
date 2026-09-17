package com.studentmanager.util;

import java.util.List;

/**
 * Presentation helpers that keep the console output consistent.
 *
 * <p>All layout lives here so the menu classes contain only workflow logic. Output is
 * plain 7-bit ASCII on purpose: box-drawing characters render as mojibake in the default
 * Windows console code page (CP437/CP1252), which is where most submissions are demonstrated.</p>
 */
public final class ConsoleUI {

    private static final int WIDTH = 78;

    private ConsoleUI() {
        throw new AssertionError("ConsoleUI is a static utility and must not be instantiated.");
    }

    /** Prints the application splash banner. */
    public static void banner() {
        System.out.println();
        System.out.println("+" + "=".repeat(WIDTH) + "+");
        System.out.println("|" + centre("SMART STUDENT MANAGER") + "|");
        System.out.println("|" + centre("Core Java Console Application") + "|");
        System.out.println("+" + "=".repeat(WIDTH) + "+");
    }

    /** Prints a section heading such as {@code --- STUDENT MANAGEMENT ---}. */
    public static void header(String title) {
        System.out.println();
        System.out.println("+" + "-".repeat(WIDTH) + "+");
        System.out.println("|" + centre(title.toUpperCase()) + "|");
        System.out.println("+" + "-".repeat(WIDTH) + "+");
    }

    /** Prints a numbered menu and its options. */
    public static void menu(String title, List<String> options) {
        header(title);
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, options.get(i));
        }
    }

    /** Prints a column heading row followed by a rule. */
    public static void tableHeader(String columns) {
        System.out.println();
        System.out.println("  " + columns);
        System.out.println("  " + "-".repeat(Math.min(columns.length() + 4, WIDTH)));
    }

    public static void success(String message) {
        System.out.println("  [OK] " + message);
    }

    public static void failure(String category, String message) {
        System.out.println("  [" + category + "] " + message);
    }

    public static void info(String message) {
        System.out.println("  " + message);
    }

    public static void empty(String what) {
        System.out.println("  (no " + what + " recorded yet)");
    }

    public static void rule() {
        System.out.println("  " + "-".repeat(WIDTH - 2));
    }

    private static String centre(String text) {
        if (text.length() >= WIDTH) {
            return text.substring(0, WIDTH);
        }
        int left = (WIDTH - text.length()) / 2;
        int right = WIDTH - text.length() - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }
}
