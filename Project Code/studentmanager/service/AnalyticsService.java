package com.studentmanager.service;

import com.studentmanager.model.*;
import java.util.*;

public class AnalyticsService {
    private final StudentService students;
    private final TaskService tasks;
    private final AttendanceService attendance;

    public AnalyticsService(StudentService students,TaskService tasks,AttendanceService attendance){
        this.students=students;this.tasks=tasks;this.attendance=attendance;
    }

    public String report(){
        long completed=tasks.all().stream().filter(Task::isCompleted).count();
        long total=tasks.all().size();
        double avg=attendance.all().stream().mapToDouble(AttendanceRecord::percentage).average().orElse(0);
        String risk=avg<75?"Attendance below 75% — review required":"Attendance at or above 75%";
        return "\\n=== ACADEMIC ANALYTICS ===\\nStudents: "+students.all().size()
            +"\\nTasks completed: "+completed+"/"+total
            +"\\nAverage attendance: "+String.format("%.2f",avg)+"%"
            +"\\nStatus: "+risk+"\\n";
    }
}
