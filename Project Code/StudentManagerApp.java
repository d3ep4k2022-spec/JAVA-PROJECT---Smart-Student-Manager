package com.studentmanager;

import com.studentmanager.exception.StudentManagerException;
import com.studentmanager.model.AttendanceRecord;
import com.studentmanager.model.Priority;
import com.studentmanager.model.Student;
import com.studentmanager.model.Task;
import com.studentmanager.model.TaskStatus;
import com.studentmanager.service.AnalyticsService;
import com.studentmanager.service.AttendanceService;
import com.studentmanager.service.BackupService;
import com.studentmanager.service.StudentService;
import com.studentmanager.service.TaskService;
import com.studentmanager.util.ConsoleUI;
import com.studentmanager.util.DemoDataSeeder;
import com.studentmanager.util.InputUtil;
import com.studentmanager.util.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Presentation layer: renders menus, reads input and delegates to the services.
 *
 * <p>This class contains no business rules and no file access. Every decision about what
 * is valid or what a report contains lives in the service layer, which is what lets the
 * services be reused unchanged behind a GUI or a REST controller later.</p>
 *
 * <p>Error handling follows one principle: the run loop is the single place where an
 * exception becomes a message on screen. Services throw; the loop catches, prints and
 * carries on. Nothing the user can type is able to terminate the program.</p>
 */
public class StudentManagerApp {

    /** How often the background snapshot runs, in seconds. */
    private static final long BACKUP_INTERVAL_SECONDS = 120;

    private final InputUtil input = new InputUtil();
    private final StudentService students = new StudentService();
    private final TaskService tasks = new TaskService(students);
    private final AttendanceService attendance = new AttendanceService(students);
    private final AnalyticsService analytics = new AnalyticsService(students, tasks, attendance);
    private final BackupService backups = new BackupService(BACKUP_INTERVAL_SECONDS);

    /** Replaces the CSV files with the bundled demo records. Used by {@code --seed}. */
    public void seedDemoData() {
        DemoDataSeeder.seed(students, tasks, attendance);
        ConsoleUI.success("Demo data loaded: " + students.count() + " students, "
                + tasks.count() + " tasks, " + attendance.count() + " attendance records.");
    }

    /** Main application loop. Returns only when the user chooses Exit. */
    public void run() {
        ConsoleUI.banner();
        backups.start();
        // Belt and braces: even a kill signal leaves the worker shut down cleanly.
        Runtime.getRuntime().addShutdownHook(new Thread(backups::stop, "shutdown-hook"));

        boolean running = true;
        while (running) {
            try {
                ConsoleUI.menu("Main Menu", List.of(
                        "Student Management",
                        "Task Management",
                        "Attendance Management",
                        "Analytics & Reports",
                        "Backup Status",
                        "Exit"));
                switch (input.integer("\n  Select an option [1-6]: ", 1, 6)) {
                    case 1 -> studentMenu();
                    case 2 -> taskMenu();
                    case 3 -> attendanceMenu();
                    case 4 -> analyticsMenu();
                    case 5 -> backupMenu();
                    case 6 -> running = confirmExit();
                    default -> ConsoleUI.info("Please choose 1-6.");
                }
            } catch (StudentManagerException e) {
                // Expected, business-level failure: report it and keep going.
                ConsoleUI.failure(e.category(), e.getMessage());
            } catch (RuntimeException e) {
                // Anything unforeseen: still never crash the session.
                ConsoleUI.failure("UNEXPECTED", e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }

        shutdown();
    }

    /** @return {@code false} to leave the loop, {@code true} to stay. */
    private boolean confirmExit() {
        return !input.confirm("\n  Exit Smart Student Manager?");
    }

    private void shutdown() {
        ConsoleUI.header("Shutting Down");
        backups.stop();
        try {
            ConsoleUI.info("Final snapshot written to: " + backups.backupNow());
        } catch (Exception e) {
            ConsoleUI.failure("STORAGE", "Final snapshot failed: " + e.getMessage());
        }
        ConsoleUI.info("All records are saved in the data/ folder. Goodbye.");
        input.close();
    }

    // ------------------------------------------------------------------
    // Student module
    // ------------------------------------------------------------------

    private void studentMenu() {
        ConsoleUI.menu("Student Management", List.of(
                "Add student",
                "View all students",
                "Search students",
                "Update student",
                "Delete student",
                "Rank by CGPA",
                "Back to main menu"));
        switch (input.integer("\n  Select an option [1-7]: ", 1, 7)) {
            case 1 -> addStudent();
            case 2 -> listStudents(students.findAll());
            case 3 -> listStudents(students.search(input.text("  Search keyword: ")));
            case 4 -> updateStudent();
            case 5 -> deleteStudent();
            case 6 -> listStudents(students.rankedByCgpa());
            default -> { /* back */ }
        }
    }

    private void addStudent() {
        ConsoleUI.header("Add Student");
        String suggested = students.nextId();
        String id = input.text("  Student ID [" + suggested + "]: ");
        Student student = new Student(
                id.isBlank() ? suggested : id,
                input.requiredText("  Full name: "),
                input.requiredText("  Email: "),
                input.requiredText("  Course: "),
                input.integer("  Semester (1-12): "),
                input.decimal("  CGPA (0-10): "));
        students.add(student);
        ConsoleUI.success("Student " + student.getId() + " added.");
    }

    private void updateStudent() {
        ConsoleUI.header("Update Student");
        Student existing = students.require(input.requiredText("  Student ID to update: "));
        ConsoleUI.info("Leave a field blank to keep the current value.");

        String name = input.text("  Name [" + existing.getName() + "]: ");
        String email = input.text("  Email [" + existing.getEmail() + "]: ");
        String course = input.text("  Course [" + existing.getCourse() + "]: ");
        String semester = input.text("  Semester [" + existing.getSemester() + "]: ");
        String cgpa = input.text("  CGPA [" + String.format("%.2f", existing.getCgpa()) + "]: ");

        if (!name.isBlank()) {
            existing.setName(name);
        }
        if (!email.isBlank()) {
            existing.setEmail(email);
        }
        if (!course.isBlank()) {
            existing.setCourse(course);
        }
        if (!semester.isBlank()) {
            existing.setSemester(parseIntOrFail(semester, "Semester"));
        }
        if (!cgpa.isBlank()) {
            existing.setCgpa(parseDoubleOrFail(cgpa, "CGPA"));
        }
        students.update(existing);
        ConsoleUI.success("Student " + existing.getId() + " updated.");
    }

    private void deleteStudent() {
        ConsoleUI.header("Delete Student");
        Student existing = students.require(input.requiredText("  Student ID to delete: "));
        int taskCount = tasks.findByStudent(existing.getId()).size();
        int attendanceCount = attendance.findByStudent(existing.getId()).size();
        ConsoleUI.info("This will also remove " + taskCount + " task(s) and "
                + attendanceCount + " attendance record(s).");
        if (!input.confirm("  Delete " + existing.getName() + "?")) {
            ConsoleUI.info("Cancelled. Nothing was removed.");
            return;
        }
        // Cascade first, so a failure part-way cannot orphan child records.
        tasks.deleteByStudent(existing.getId());
        attendance.deleteByStudent(existing.getId());
        students.delete(existing.getId());
        ConsoleUI.success("Student " + existing.getId() + " and all related records deleted.");
    }

    private void listStudents(List<Student> list) {
        ConsoleUI.header("Students (" + list.size() + ")");
        if (list.isEmpty()) {
            ConsoleUI.empty("students");
            return;
        }
        ConsoleUI.tableHeader(String.format("%-8s %-22s %-26s %-14s %-4s %s",
                "ID", "NAME", "EMAIL", "COURSE", "SEM", "CGPA"));
        list.forEach(student -> System.out.println("  " + student));
        ConsoleUI.rule();
        ConsoleUI.info(String.format("Showing %d of %d student(s) on roll. Roll average CGPA: %.2f",
                list.size(), students.count(), students.averageCgpa()));
    }

    // ------------------------------------------------------------------
    // Task module
    // ------------------------------------------------------------------

    private void taskMenu() {
        ConsoleUI.menu("Task Management", List.of(
                "Add task",
                "View all tasks",
                "View tasks for one student",
                "View overdue tasks",
                "Change task status",
                "Delete task",
                "Back to main menu"));
        switch (input.integer("\n  Select an option [1-7]: ", 1, 7)) {
            case 1 -> addTask();
            case 2 -> listTasks(tasks.findAllSorted(), "All Tasks");
            case 3 -> listTasks(tasks.findByStudent(input.requiredText("  Student ID: ")), "Student Tasks");
            case 4 -> listTasks(tasks.findOverdue(), "Overdue Tasks");
            case 5 -> changeTaskStatus();
            case 6 -> {
                tasks.delete(input.requiredText("  Task ID to delete: "));
                ConsoleUI.success("Task deleted.");
            }
            default -> { /* back */ }
        }
    }

    private void addTask() {
        ConsoleUI.header("Add Task");
        String suggested = tasks.nextId();
        String id = input.text("  Task ID [" + suggested + "]: ");
        String studentId = input.requiredText("  Student ID: ");
        String title = input.requiredText("  Title: ");
        LocalDate due = Validator.parseDate(
                input.text("  Due date (yyyy-MM-dd, blank for none): "));
        Priority priority = parsePriority(
                input.text("  Priority [1=Low 2=Medium 3=High, blank=Medium]: "));

        Task task = new Task(id.isBlank() ? suggested : id, studentId, title, due,
                priority, TaskStatus.PENDING);
        tasks.add(task);
        ConsoleUI.success("Task " + task.getId() + " assigned to " + studentId + ".");
    }

    private void changeTaskStatus() {
        ConsoleUI.header("Change Task Status");
        Task task = tasks.require(input.requiredText("  Task ID: "));
        ConsoleUI.info("Current status: " + task.getStatus().getLabel());
        ConsoleUI.info("1 = Pending, 2 = In Progress, 3 = Completed");
        TaskStatus status = parseStatus(input.requiredText("  New status: "));
        tasks.changeStatus(task.getId(), status);
        ConsoleUI.success("Task " + task.getId() + " is now " + status.getLabel() + ".");
    }

    private void listTasks(List<Task> list, String title) {
        ConsoleUI.header(title + " (" + list.size() + ")");
        if (list.isEmpty()) {
            ConsoleUI.empty("tasks");
            return;
        }
        ConsoleUI.tableHeader(String.format("%-8s %-8s %-30s %-12s %-8s %s",
                "TASK", "STUDENT", "TITLE", "DUE", "PRIORITY", "STATUS"));
        list.forEach(task -> System.out.println("  " + task));
        ConsoleUI.rule();
        ConsoleUI.info(String.format("Completion rate: %.1f%%", tasks.completionRate()));
    }

    // ------------------------------------------------------------------
    // Attendance module
    // ------------------------------------------------------------------

    private void attendanceMenu() {
        ConsoleUI.menu("Attendance Management", List.of(
                "Record a new subject",
                "Log classes against an existing record",
                "View all attendance",
                "View attendance for one student",
                "View at-risk subjects",
                "Delete an attendance record",
                "Back to main menu"));
        switch (input.integer("\n  Select an option [1-7]: ", 1, 7)) {
            case 1 -> addAttendance();
            case 2 -> logSession();
            case 3 -> listAttendance(attendance.findAll(), "All Attendance");
            case 4 -> listAttendance(
                    attendance.findByStudent(input.requiredText("  Student ID: ")), "Student Attendance");
            case 5 -> listAttendance(attendance.findAtRisk(), "Subjects Below 75%");
            case 6 -> {
                attendance.delete(input.requiredText("  Attendance ID to delete: "));
                ConsoleUI.success("Attendance record deleted.");
            }
            default -> { /* back */ }
        }
    }

    private void addAttendance() {
        ConsoleUI.header("Record Attendance");
        String suggested = attendance.nextId();
        String id = input.text("  Attendance ID [" + suggested + "]: ");
        AttendanceRecord record = new AttendanceRecord(
                id.isBlank() ? suggested : id,
                input.requiredText("  Student ID: "),
                input.requiredText("  Subject: "),
                input.integer("  Classes held: "),
                input.integer("  Classes attended: "));
        attendance.add(record);
        ConsoleUI.success(String.format("Recorded %s at %.2f%%.",
                record.getSubject(), record.percentage()));
        if (record.isBelowMinimum()) {
            ConsoleUI.info("Warning: below 75%. Attend the next "
                    + record.classesNeededToRecover() + " class(es) to recover.");
        }
    }

    private void logSession() {
        ConsoleUI.header("Log Classes");
        AttendanceRecord record = attendance.require(input.requiredText("  Attendance ID: "));
        ConsoleUI.info(String.format("%s currently at %.2f%% (%d/%d).", record.getSubject(),
                record.percentage(), record.getAttendedClasses(), record.getTotalClasses()));
        int held = input.integer("  Classes newly held: ");
        int attended = input.integer("  Of those, classes attended: ");
        attendance.addSession(record.getId(), held, attended);
        AttendanceRecord updated = attendance.require(record.getId());
        ConsoleUI.success(String.format("%s now at %.2f%%.",
                updated.getSubject(), updated.percentage()));
    }

    private void listAttendance(List<AttendanceRecord> list, String title) {
        ConsoleUI.header(title + " (" + list.size() + ")");
        if (list.isEmpty()) {
            ConsoleUI.empty("attendance records");
            return;
        }
        ConsoleUI.tableHeader(String.format("%-8s %-8s %-24s %5s %9s %8s  %s",
                "ID", "STUDENT", "SUBJECT", "HELD", "ATTENDED", "PERCENT", "FLAG"));
        list.forEach(record -> System.out.println("  " + record));
        ConsoleUI.rule();
        ConsoleUI.info(String.format("Overall attendance: %.2f%%", attendance.overallPercentage()));
    }

    // ------------------------------------------------------------------
    // Analytics module
    // ------------------------------------------------------------------

    private void analyticsMenu() {
        ConsoleUI.menu("Analytics & Reports", List.of(
                "Institution summary",
                "Report card for one student",
                "Back to main menu"));
        switch (input.integer("\n  Select an option [1-3]: ", 1, 3)) {
            case 1 -> printAsync(analytics::institutionReport, "institution summary");
            case 2 -> {
                String id = input.requiredText("  Student ID: ");
                printAsync(() -> analytics.studentReport(id), "report card");
            }
            default -> { /* back */ }
        }
    }

    /**
     * Runs a report on a worker thread and prints the result.
     *
     * <p>Reports scan every record in all three files, so on a large data set this keeps
     * the work off the thread that owns the console. {@link Future#get()} re-throws any
     * exception from the worker wrapped in an {@link ExecutionException}; unwrapping the
     * cause is what lets a {@code DataNotFoundException} raised inside the worker still
     * surface as a normal, readable message.</p>
     */
    private void printAsync(java.util.concurrent.Callable<String> job, String label) {
        ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "analytics-worker");
            thread.setDaemon(true);
            return thread;
        });
        try {
            Future<String> future = worker.submit(job);
            ConsoleUI.info("Generating " + label + " on a background thread...");
            System.out.println();
            System.out.println(future.get());
            input.pause();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof StudentManagerException known) {
                ConsoleUI.failure(known.category(), known.getMessage());
            } else {
                ConsoleUI.failure("ANALYTICS", String.valueOf(cause));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ConsoleUI.failure("ANALYTICS", "Report generation was interrupted.");
        } finally {
            worker.shutdown();
        }
    }

    // ------------------------------------------------------------------
    // Backup module
    // ------------------------------------------------------------------

    private void backupMenu() {
        ConsoleUI.header("Backup Status");
        ConsoleUI.info("Automatic snapshot interval : " + BACKUP_INTERVAL_SECONDS + " seconds");
        ConsoleUI.info("Snapshots this session      : " + backups.getCompletedBackups());
        ConsoleUI.info("Most recent snapshot        : " + backups.getLastBackupPath());
        if (input.confirm("\n  Take a snapshot now?")) {
            try {
                ConsoleUI.success("Snapshot written to " + backups.backupNow());
            } catch (Exception e) {
                ConsoleUI.failure("STORAGE", e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // Small parsing helpers that convert library exceptions into ours
    // ------------------------------------------------------------------

    private int parseIntOrFail(String raw, String field) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new com.studentmanager.exception.ValidationException(
                    field + " must be a whole number, received '" + raw + "'.");
        }
    }

    private double parseDoubleOrFail(String raw, String field) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new com.studentmanager.exception.ValidationException(
                    field + " must be a number, received '" + raw + "'.");
        }
    }

    private Priority parsePriority(String raw) {
        try {
            return Priority.parse(raw);
        } catch (IllegalArgumentException e) {
            throw new com.studentmanager.exception.ValidationException(e.getMessage());
        }
    }

    private TaskStatus parseStatus(String raw) {
        try {
            return TaskStatus.parse(raw);
        } catch (IllegalArgumentException e) {
            throw new com.studentmanager.exception.ValidationException(e.getMessage());
        }
    }
}
