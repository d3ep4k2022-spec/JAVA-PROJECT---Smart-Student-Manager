package com.studentmanager;

import com.studentmanager.exception.*;
import com.studentmanager.model.*;
import com.studentmanager.service.*;
import com.studentmanager.util.InputUtil;
import java.util.*;
import java.util.concurrent.*;

public class StudentManagerApp {
    private final InputUtil in=new InputUtil();
    private final StudentService students=new StudentService();
    private final TaskService tasks=new TaskService();
    private final AttendanceService attendance=new AttendanceService();
    private final AnalyticsService analytics=new AnalyticsService(students,tasks,attendance);

    public void run(){
        System.out.println("=================================");
        System.out.println("     SMART STUDENT MANAGER");
        System.out.println("=================================");
        while(true){
            try{
                System.out.println("\\n1. Students  2. Tasks  3. Attendance  4. Analytics  5. Exit");
                switch(in.integer("Choose: ")){
                    case 1->studentMenu();
                    case 2->taskMenu();
                    case 3->attendanceMenu();
                    case 4->showAnalyticsAsync();
                    case 5->{System.out.println("Goodbye!");return;}
                    default->System.out.println("Choose 1-5.");
                }
            }catch(ValidationException|DataNotFoundException e){System.out.println("Error: "+e.getMessage());}
             catch(Exception e){System.out.println("Unexpected error: "+e.getMessage());}
        }
    }

    private void studentMenu(){
        System.out.println("\\n--- STUDENT MANAGEMENT ---");
        System.out.println("1.Add 2.View 3.Update 4.Delete");
        int c=in.integer("Choose: ");
        if(c==1){
            students.add(new Student(in.text("ID: "),in.text("Name: "),in.text("Email: "),in.text("Course: ")));
            System.out.println("Student added.");
        } else if(c==2) students.all().forEach(System.out::println);
        else if(c==3) students.update(in.text("ID: "),in.text("New name: "),in.text("New email: "),in.text("New course: "));
        else if(c==4) students.delete(in.text("ID: "));
        else System.out.println("Invalid option.");
    }

    private void taskMenu(){
        System.out.println("\\n--- TASK MANAGEMENT ---");
        System.out.println("1.Add 2.View 3.Complete 4.Delete");
        int c=in.integer("Choose: ");
        if(c==1){
            tasks.add(new Task(in.text("Task ID: "),in.text("Student ID: "),in.text("Title: "),in.text("Due date: "),false));
            System.out.println("Task added.");
        } else if(c==2) tasks.all().forEach(System.out::println);
        else if(c==3){tasks.complete(in.text("Task ID: "));System.out.println("Task completed.");}
        else if(c==4) tasks.delete(in.text("Task ID: "));
        else System.out.println("Invalid option.");
    }

    private void attendanceMenu(){
        System.out.println("\\n--- ATTENDANCE ---");
        System.out.println("1.Record 2.View");
        int c=in.integer("Choose: ");
        if(c==1){
            attendance.add(new AttendanceRecord(in.text("Student ID: "),in.text("Subject: "),
                in.integer("Total classes: "),in.integer("Attended classes: ")));
            System.out.println("Attendance recorded.");
        } else if(c==2){
            attendance.all().forEach(a->System.out.printf("%s | %s | %.2f%%%n",
                a.getStudentId(),a.getSubject(),a.percentage()));
        } else System.out.println("Invalid option.");
    }

    private void showAnalyticsAsync(){
        ExecutorService pool=Executors.newSingleThreadExecutor();
        Future<String> f=pool.submit(analytics::report);
        try{System.out.println(f.get());}catch(Exception e){System.out.println("Analytics error: "+e.getMessage());}
        finally{pool.shutdown();}
    }
}
