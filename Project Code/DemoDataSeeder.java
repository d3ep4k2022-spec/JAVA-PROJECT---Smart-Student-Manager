package com.studentmanager.util;

import com.studentmanager.model.AttendanceRecord;
import com.studentmanager.model.Priority;
import com.studentmanager.model.Student;
import com.studentmanager.model.Task;
import com.studentmanager.model.TaskStatus;
import com.studentmanager.service.AttendanceService;
import com.studentmanager.service.StudentService;
import com.studentmanager.service.TaskService;
import java.time.LocalDate;
import java.util.List;

/**
 * Loads a small, deliberately varied demo data set.
 *
 * <p>The data is chosen so that a demonstration exercises every branch of the analytics
 * module without the presenter having to type anything: it contains a high performer, a
 * student below the CGPA threshold, a student below the 75% attendance rule, overdue and
 * completed tasks, and two different courses.</p>
 *
 * <p>Deadlines are expressed relative to {@link LocalDate#now()} rather than hard-coded,
 * so the "overdue" column is still meaningful whenever the project is demonstrated.</p>
 */
public final class DemoDataSeeder {

    private DemoDataSeeder() {
        throw new AssertionError("DemoDataSeeder is a static utility and must not be instantiated.");
    }

    /** Clears all three files and repopulates them. Existing data is destroyed. */
    public static void seed(StudentService students, TaskService tasks, AttendanceService attendance) {
        clear(students, tasks, attendance);

        List<Student> roster = List.of(
                new Student("S001", "Aarav Sharma", "aarav.sharma@college.edu", "B.Tech CSE", 5, 8.74),
                new Student("S002", "Diya Nair", "diya.nair@college.edu", "B.Tech CSE", 5, 9.12),
                new Student("S003", "Rohan Verma", "rohan.verma@college.edu", "B.Tech CSE", 5, 5.68),
                new Student("S004", "Ishita Rao", "ishita.rao@college.edu", "B.Tech ECE", 3, 7.95),
                new Student("S005", "Kabir Singh", "kabir.singh@college.edu", "B.Tech ECE", 3, 6.42),
                new Student("S006", "Meera Iyer", "meera.iyer@college.edu", "B.Sc Maths", 1, 8.10));
        roster.forEach(students::add);

        List<Task> workload = List.of(
                task("T001", "S001", "OOP lab record submission", -6, Priority.HIGH, TaskStatus.COMPLETED),
                task("T002", "S001", "DBMS assignment 3", 4, Priority.MEDIUM, TaskStatus.IN_PROGRESS),
                task("T003", "S002", "Data structures viva prep", 2, Priority.HIGH, TaskStatus.IN_PROGRESS),
                task("T004", "S002", "Java mini project report", 9, Priority.MEDIUM, TaskStatus.PENDING),
                task("T005", "S003", "Operating systems quiz", -3, Priority.HIGH, TaskStatus.PENDING),
                task("T006", "S003", "Maths tutorial sheet 7", -11, Priority.LOW, TaskStatus.PENDING),
                task("T007", "S004", "Signals lab report", 1, Priority.HIGH, TaskStatus.PENDING),
                task("T008", "S004", "Microprocessors revision", 14, Priority.LOW, TaskStatus.COMPLETED),
                task("T009", "S005", "Digital circuits assignment", -1, Priority.MEDIUM, TaskStatus.PENDING),
                task("T010", "S006", "Linear algebra problem set", 6, Priority.MEDIUM, TaskStatus.COMPLETED));
        workload.forEach(tasks::add);

        List<AttendanceRecord> register = List.of(
                new AttendanceRecord("A001", "S001", "Object Oriented Programming", 42, 39),
                new AttendanceRecord("A002", "S001", "Database Systems", 38, 30),
                new AttendanceRecord("A003", "S002", "Object Oriented Programming", 42, 41),
                new AttendanceRecord("A004", "S002", "Database Systems", 38, 36),
                new AttendanceRecord("A005", "S003", "Object Oriented Programming", 42, 24),
                new AttendanceRecord("A006", "S003", "Operating Systems", 36, 21),
                new AttendanceRecord("A007", "S004", "Signals and Systems", 40, 35),
                new AttendanceRecord("A008", "S005", "Digital Circuits", 40, 27),
                new AttendanceRecord("A009", "S006", "Linear Algebra", 30, 28));
        register.forEach(attendance::add);
    }

    /** Builds a task whose deadline is {@code offsetDays} from today (negative = past). */
    private static Task task(String id, String studentId, String title,
                             int offsetDays, Priority priority, TaskStatus status) {
        return new Task(id, studentId, title, LocalDate.now().plusDays(offsetDays), priority, status);
    }

    /** Deletes children before parents so referential integrity holds at every step. */
    private static void clear(StudentService students, TaskService tasks, AttendanceService attendance) {
        tasks.findAll().forEach(t -> tasks.delete(t.getId()));
        attendance.findAll().forEach(a -> attendance.delete(a.getId()));
        students.findAll().forEach(s -> students.delete(s.getId()));
    }
}
