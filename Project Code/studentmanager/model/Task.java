package com.studentmanager.model;

public class Task {
    private String id;
    private String studentId;
    private String title;
    private String dueDate;
    private boolean completed;

    public Task(String id, String studentId, String title, String dueDate, boolean completed) {
        this.id=id; this.studentId=studentId; this.title=title; this.dueDate=dueDate; this.completed=completed;
    }
    public String getId(){return id;}
    public String getStudentId(){return studentId;}
    public String getTitle(){return title;}
    public String getDueDate(){return dueDate;}
    public boolean isCompleted(){return completed;}
    public void setCompleted(boolean completed){this.completed=completed;}

    public String toCsv(){
        return String.join(",", id, studentId, title.replace(","," "), dueDate, String.valueOf(completed));
    }
    public static Task fromCsv(String line){
        String[] p=line.split(",",-1);
        return new Task(p[0],p[1],p[2],p[3],Boolean.parseBoolean(p[4]));
    }
    @Override public String toString(){
        return id+" | "+studentId+" | "+title+" | Due: "+dueDate+" | "+(completed?"DONE":"PENDING");
    }
}
