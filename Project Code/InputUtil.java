package com.studentmanager.util;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Thin wrapper around a single {@link Scanner} on {@code System.in}.
 *
 * <p>Two rules are enforced here that matter in a console application:</p>
 * <ul>
 *   <li><b>One Scanner for the whole program.</b> Creating a second {@code Scanner} on
 *       {@code System.in} and closing either one closes the underlying stream for both,
 *       which is a classic source of {@code NoSuchElementException} in student projects.</li>
 *   <li><b>Only {@code nextLine()} is used.</b> Mixing {@code nextInt()} with
 *       {@code nextLine()} leaves the trailing newline in the buffer and silently
 *       swallows the next prompt. Reading whole lines and parsing them avoids that entirely.</li>
 * </ul>
 */
public class InputUtil {

    private final Scanner scanner = new Scanner(System.in);

    /** Reads a trimmed line. Returns an empty string if the stream ends (piped input). */
    public String text(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    /** Reads a non-blank line, re-prompting until the user supplies one. */
    public String requiredText(String prompt) {
        while (true) {
            String value = text(prompt);
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("  ! This field is required.");
        }
    }

    /** Reads an integer, re-prompting on anything unparseable. */
    public int integer(String prompt) {
        while (true) {
            String raw = text(prompt);
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("  ! '" + raw + "' is not a whole number. Try again.");
            }
        }
    }

    /** Reads an integer constrained to a range, re-prompting until it fits. */
    public int integer(String prompt, int min, int max) {
        while (true) {
            int value = integer(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("  ! Enter a number between " + min + " and " + max + ".");
        }
    }

    /** Reads a decimal, re-prompting on anything unparseable. */
    public double decimal(String prompt) {
        while (true) {
            String raw = text(prompt);
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                System.out.println("  ! '" + raw + "' is not a number. Try again.");
            }
        }
    }

    /** @return {@code true} only when the user answers y or yes. */
    public boolean confirm(String prompt) {
        String answer = text(prompt + " (y/n): ").toLowerCase();
        return answer.equals("y") || answer.equals("yes");
    }

    /** Blocks until the user presses Enter, so a report is not scrolled away instantly. */
    public void pause() {
        text("\nPress Enter to continue...");
    }

    /** Closes the shared scanner. Called once, from the application shutdown path. */
    public void close() {
        scanner.close();
    }
}
