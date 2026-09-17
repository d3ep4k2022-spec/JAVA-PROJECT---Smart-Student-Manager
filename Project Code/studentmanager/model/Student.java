package com.studentmanager.model;

public class Student {
    private String id;
    private String name;
    private String email;
    private String course;

    public Student(String id, String name, String email, String course) {
        this.id = id; this.name = name; this.email = email; this.course = course;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getCourse() { return course; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setCourse(String course) { this.course = course; }

    public String toCsv() {
        return String.join(",", clean(id), clean(name), clean(email), clean(course));
    }
    private String clean(String s) { return s.replace(",", " "); }

    public static Student fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Student(p[0], p[1], p[2], p[3]);
    }

    @Override public String toString() {
        return id + " | " + name + " | " + email + " | " + course;
    }
}
