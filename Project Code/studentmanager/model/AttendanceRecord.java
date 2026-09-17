package com.studentmanager.model;

public class AttendanceRecord {
    private String studentId;
    private String subject;
    private int totalClasses;
    private int attendedClasses;

    public AttendanceRecord(String studentId, String subject, int totalClasses, int attendedClasses) {
        this.studentId=studentId; this.subject=subject; this.totalClasses=totalClasses; this.attendedClasses=attendedClasses;
    }
    public String getStudentId(){return studentId;}
    public String getSubject(){return subject;}
    public int getTotalClasses(){return totalClasses;}
    public int getAttendedClasses(){return attendedClasses;}
    public double percentage(){return totalClasses==0?0:(attendedClasses*100.0/totalClasses);}

    public String toCsv(){return String.join(",",studentId,subject.replace(","," "),String.valueOf(totalClasses),String.valueOf(attendedClasses));}
    public static AttendanceRecord fromCsv(String line){
        String[] p=line.split(",",-1);
        return new AttendanceRecord(p[0],p[1],Integer.parseInt(p[2]),Integer.parseInt(p[3]));
    }
}
