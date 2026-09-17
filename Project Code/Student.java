package com.studentmanager.model;

import com.studentmanager.util.TextUtil;

/**
 * A student enrolled in a course. Concrete subclass of {@link Person}.
 *
 * <p>CSV layout: {@code id,name,email,course,semester,cgpa}</p>
 */
public class Student extends Person implements CsvSerializable {

    private static final int CSV_FIELDS = 6;

    private String course;
    private int semester;
    private double cgpa;

    public Student(String id, String name, String email, String course, int semester, double cgpa) {
        super(id, name, email);
        this.course = course == null ? "" : course.trim();
        this.semester = semester;
        this.cgpa = cgpa;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course == null ? "" : course.trim();
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    @Override
    public String describe() {
        return String.format("%s (%s) - %s, semester %d, CGPA %.2f",
                getName(), getId(), course, semester, cgpa);
    }

    @Override
    public String toCsv() {
        return String.join(",",
                CsvSerializable.sanitise(getId()),
                CsvSerializable.sanitise(getName()),
                CsvSerializable.sanitise(getEmail()),
                CsvSerializable.sanitise(course),
                String.valueOf(semester),
                String.format("%.2f", cgpa));
    }

    /**
     * Rebuilds a {@code Student} from one CSV line.
     *
     * @throws IllegalArgumentException if the line does not hold the expected column count
     *         or if the numeric columns cannot be parsed
     */
    public static Student fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != CSV_FIELDS) {
            throw new IllegalArgumentException(
                    "Expected " + CSV_FIELDS + " columns in students.csv but found " + parts.length);
        }
        try {
            return new Student(parts[0], parts[1], parts[2], parts[3],
                    Integer.parseInt(parts[4].trim()),
                    Double.parseDouble(parts[5].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Non-numeric semester or CGPA in row: " + line, e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %-4d %.2f",
                TextUtil.fit(getId(), 8), TextUtil.fit(getName(), 22),
                TextUtil.fit(getEmail(), 26), TextUtil.fit(course, 14), semester, cgpa);
    }
}
