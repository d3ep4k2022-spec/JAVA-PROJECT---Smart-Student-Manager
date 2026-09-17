package com.studentmanager.util;

import com.studentmanager.exception.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Central home for every business validation rule.
 *
 * <p>Keeping the rules here rather than inside each service means an error message is
 * worded once and reused everywhere, and a rule change happens in exactly one file.
 * The class is {@code final} with a private constructor because it holds only static
 * helpers and must never be instantiated or subclassed.</p>
 */
public final class Validator {

    /**
     * Pragmatic e-mail pattern: one or more non-space, non-@ characters, an @,
     * a domain, a dot and a TLD of at least two letters. Deliberately not RFC 5322 —
     * a fully compliant regex is unreadable and rejects almost nothing extra in practice.
     */
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** IDs are alphanumeric with optional dashes, 2-12 characters, e.g. {@code S001}. */
    private static final Pattern ID = Pattern.compile("^[A-Za-z0-9-]{2,12}$");

    private Validator() {
        throw new AssertionError("Validator is a static utility and must not be instantiated.");
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public static String requireId(String value, String fieldName) {
        String trimmed = requireText(value, fieldName);
        if (!ID.matcher(trimmed).matches()) {
            throw new ValidationException(
                    fieldName + " must be 2-12 letters, digits or dashes (example: S001).");
        }
        return trimmed;
    }

    public static String requireEmail(String value) {
        String trimmed = requireText(value, "Email");
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new ValidationException("Invalid email format: '" + trimmed
                    + "'. Expected something like name@college.edu.");
        }
        return trimmed;
    }

    public static int requireRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(
                    fieldName + " must be between " + min + " and " + max + " (received " + value + ").");
        }
        return value;
    }

    public static double requireRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(String.format(
                    "%s must be between %.2f and %.2f (received %.2f).", fieldName, min, max, value));
        }
        return value;
    }

    /**
     * Parses an ISO-8601 date ({@code yyyy-MM-dd}).
     *
     * @param value raw text; blank is allowed and yields {@code null} (task with no deadline)
     * @throws ValidationException if the text is present but unparseable
     */
    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    "Invalid date '" + value + "'. Use the format yyyy-MM-dd, for example 2026-10-05.");
        }
    }

    /** Guards the attendance counters, which are mutually dependent. */
    public static void requireValidAttendance(int total, int attended) {
        if (total <= 0) {
            throw new ValidationException("Total classes must be greater than zero.");
        }
        if (attended < 0) {
            throw new ValidationException("Attended classes cannot be negative.");
        }
        if (attended > total) {
            throw new ValidationException(
                    "Attended classes (" + attended + ") cannot exceed total classes (" + total + ").");
        }
    }
}
