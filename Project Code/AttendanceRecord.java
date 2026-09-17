package com.studentmanager.model;

import com.studentmanager.util.TextUtil;

/**
 * Attendance held by one student for one subject.
 *
 * <p>CSV layout: {@code id,studentId,subject,totalClasses,attendedClasses}</p>
 *
 * <p>Storing the two raw counters rather than a pre-computed percentage keeps the
 * record a single source of truth: the percentage is derived on demand, so it can
 * never drift out of step with the counters.</p>
 */
public class AttendanceRecord implements Identifiable, CsvSerializable {

    private static final int CSV_FIELDS = 5;

    /** Institutional minimum attendance, used by the analytics risk flag. */
    public static final double MINIMUM_REQUIRED = 75.0;

    private final String id;
    private final String studentId;
    private String subject;
    private int totalClasses;
    private int attendedClasses;

    public AttendanceRecord(String id, String studentId, String subject,
                            int totalClasses, int attendedClasses) {
        this.id = id == null ? "" : id.trim();
        this.studentId = studentId == null ? "" : studentId.trim();
        this.subject = subject == null ? "" : subject.trim();
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject == null ? "" : subject.trim();
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getAttendedClasses() {
        return attendedClasses;
    }

    public void setAttendedClasses(int attendedClasses) {
        this.attendedClasses = attendedClasses;
    }

    /** @return attendance percentage, or {@code 0.0} when no class has been held yet. */
    public double percentage() {
        return totalClasses == 0 ? 0.0 : (attendedClasses * 100.0) / totalClasses;
    }

    /** @return {@code true} when this subject sits below the institutional minimum. */
    public boolean isBelowMinimum() {
        return percentage() < MINIMUM_REQUIRED;
    }

    /**
     * @return how many further consecutive classes must be attended to reach
     *         {@link #MINIMUM_REQUIRED}; {@code 0} when the student is already clear
     */
    public int classesNeededToRecover() {
        if (!isBelowMinimum()) {
            return 0;
        }
        int extra = 0;
        while ((attendedClasses + extra) * 100.0 / (totalClasses + extra) < MINIMUM_REQUIRED) {
            extra++;
            if (extra > 10_000) {
                break;
            }
        }
        return extra;
    }

    @Override
    public String toCsv() {
        return String.join(",",
                CsvSerializable.sanitise(id),
                CsvSerializable.sanitise(studentId),
                CsvSerializable.sanitise(subject),
                String.valueOf(totalClasses),
                String.valueOf(attendedClasses));
    }

    public static AttendanceRecord fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != CSV_FIELDS) {
            throw new IllegalArgumentException(
                    "Expected " + CSV_FIELDS + " columns in attendance.csv but found " + parts.length);
        }
        try {
            return new AttendanceRecord(parts[0], parts[1], parts[2],
                    Integer.parseInt(parts[3].trim()),
                    Integer.parseInt(parts[4].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Non-numeric class counts in row: " + line, e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %5d %9d %8.2f%%  %s",
                TextUtil.fit(id, 8), TextUtil.fit(studentId, 8), TextUtil.fit(subject, 27),
                totalClasses, attendedClasses, percentage(),
                isBelowMinimum() ? "AT RISK" : "OK");
    }
}
