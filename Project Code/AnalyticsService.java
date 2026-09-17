package com.studentmanager.service;

import com.studentmanager.model.AttendanceRecord;
import com.studentmanager.model.Student;
import com.studentmanager.model.Task;
import com.studentmanager.model.TaskStatus;
import com.studentmanager.util.TextUtil;
import java.util.List;
import java.util.Map;

/**
 * Read-only reporting built on top of the three data services.
 *
 * <p>This class owns no repository of its own. It is a pure aggregator, which keeps the
 * reporting rules in one testable place and means a new report never requires touching
 * the CRUD services.</p>
 *
 * <p>Report generation is deliberately a plain method returning a {@code String} rather
 * than printing directly, so the same logic can be rendered to the console, written to a
 * file, or exercised by a unit test without capturing {@code System.out}.</p>
 */
public class AnalyticsService {

    /** A student is flagged when attendance drops below this, matching the college rule. */
    private static final double ATTENDANCE_THRESHOLD = AttendanceRecord.MINIMUM_REQUIRED;

    /** A student is flagged when CGPA drops below this. */
    private static final double CGPA_THRESHOLD = 6.0;

    private final StudentService students;
    private final TaskService tasks;
    private final AttendanceService attendance;

    public AnalyticsService(StudentService students, TaskService tasks, AttendanceService attendance) {
        this.students = students;
        this.tasks = tasks;
        this.attendance = attendance;
    }

    /**
     * Builds the institute-wide summary.
     *
     * <p>Called on a worker thread by the application, so it must not touch the console
     * or any shared mutable state — it only reads through the thread-safe repositories.</p>
     */
    public String institutionReport() {
        StringBuilder out = new StringBuilder();
        out.append(line()).append(System.lineSeparator());
        out.append("  ACADEMIC ANALYTICS SUMMARY").append(System.lineSeparator());
        out.append(line()).append(System.lineSeparator());

        int studentCount = students.count();
        out.append(String.format("  Students on roll        : %d%n", studentCount));
        out.append(String.format("  Average CGPA            : %.2f%n", students.averageCgpa()));

        Map<TaskStatus, Long> byStatus = tasks.countByStatus();
        out.append(String.format("  Tasks total             : %d%n", tasks.count()));
        out.append(String.format("    - completed           : %d%n", byStatus.getOrDefault(TaskStatus.COMPLETED, 0L)));
        out.append(String.format("    - in progress         : %d%n", byStatus.getOrDefault(TaskStatus.IN_PROGRESS, 0L)));
        out.append(String.format("    - pending             : %d%n", byStatus.getOrDefault(TaskStatus.PENDING, 0L)));
        out.append(String.format("  Task completion rate    : %.1f%%%n", tasks.completionRate()));

        List<Task> overdue = tasks.findOverdue();
        out.append(String.format("  Overdue tasks           : %d%n", overdue.size()));
        out.append(String.format("  Overall attendance      : %.2f%%%n", attendance.overallPercentage()));
        out.append(String.format("  Subjects below %.0f%%      : %d%n",
                ATTENDANCE_THRESHOLD, attendance.findAtRisk().size()));

        out.append(line()).append(System.lineSeparator());
        out.append("  COURSE BREAKDOWN").append(System.lineSeparator());
        Map<String, List<Student>> byCourse = students.groupByCourse();
        if (byCourse.isEmpty()) {
            out.append("    (no students on roll)").append(System.lineSeparator());
        } else {
            byCourse.forEach((course, group) -> {
                double avg = group.stream().mapToDouble(Student::getCgpa).average().orElse(0.0);
                out.append(String.format("    %-20s %2d student(s), average CGPA %.2f%n",
                        course, group.size(), avg));
            });
        }

        out.append(line()).append(System.lineSeparator());
        out.append("  STUDENTS NEEDING ATTENTION").append(System.lineSeparator());
        List<Student> flagged = students.findAll().stream()
                .filter(this::isAtRisk)
                .toList();
        if (flagged.isEmpty()) {
            out.append("    None. Every student is above both thresholds.")
                    .append(System.lineSeparator());
        } else {
            for (Student student : flagged) {
                out.append("    ").append(riskLine(student)).append(System.lineSeparator());
            }
        }

        if (!overdue.isEmpty()) {
            out.append(line()).append(System.lineSeparator());
            out.append("  OVERDUE TASKS").append(System.lineSeparator());
            for (Task task : overdue) {
                out.append(String.format("    %s %s due %s (%d day(s) late, %s priority)%n",
                        TextUtil.fit(task.getStudentId(), 8), TextUtil.fit(task.getTitle(), 30),
                        task.getDueDate(), -task.daysRemaining(), task.getPriority().getLabel()));
            }
        }

        out.append(line());
        return out.toString();
    }

    /**
     * Builds a per-student academic card.
     *
     * @throws com.studentmanager.exception.DataNotFoundException if the id is unknown
     */
    public String studentReport(String studentId) {
        Student student = students.require(studentId);
        StringBuilder out = new StringBuilder();
        out.append(line()).append(System.lineSeparator());
        // Polymorphic call: describe() is declared abstract on Person.
        out.append("  ").append(student.describe()).append(System.lineSeparator());
        out.append("  ").append(student.getEmail()).append(System.lineSeparator());
        out.append(line()).append(System.lineSeparator());

        List<Task> owned = tasks.findByStudent(studentId);
        long done = owned.stream().filter(t -> t.getStatus().isClosed()).count();
        out.append(String.format("  Tasks        : %d total, %d completed (%.0f%%)%n",
                owned.size(), done, owned.isEmpty() ? 0.0 : done * 100.0 / owned.size()));

        long late = owned.stream().filter(Task::isOverdue).count();
        if (late > 0) {
            out.append(String.format("  Overdue      : %d task(s) past their deadline%n", late));
        }

        double overall = attendance.overallPercentageFor(studentId);
        out.append(String.format("  Attendance   : %.2f%% overall%n", overall));

        List<AttendanceRecord> records = attendance.findByStudent(studentId);
        if (records.isEmpty()) {
            out.append("  (no attendance recorded)").append(System.lineSeparator());
        } else {
            for (AttendanceRecord record : records) {
                String note = record.isBelowMinimum()
                        ? String.format("  <- attend next %d class(es) to reach %.0f%%",
                                record.classesNeededToRecover(), ATTENDANCE_THRESHOLD)
                        : "";
                out.append(String.format("    %s %3d/%-3d  %6.2f%%%s%n",
                        TextUtil.fit(record.getSubject(), 28), record.getAttendedClasses(),
                        record.getTotalClasses(), record.percentage(), note));
            }
        }

        out.append(line()).append(System.lineSeparator());
        out.append("  VERDICT: ").append(verdict(student)).append(System.lineSeparator());
        out.append(line());
        return out.toString();
    }

    private boolean isAtRisk(Student student) {
        double attendancePct = attendance.overallPercentageFor(student.getId());
        boolean hasAttendance = !attendance.findByStudent(student.getId()).isEmpty();
        return student.getCgpa() < CGPA_THRESHOLD
                || (hasAttendance && attendancePct < ATTENDANCE_THRESHOLD)
                || !tasks.findByStudent(student.getId()).stream().filter(Task::isOverdue).toList().isEmpty();
    }

    private String riskLine(Student student) {
        StringBuilder reasons = new StringBuilder();
        if (student.getCgpa() < CGPA_THRESHOLD) {
            reasons.append(String.format("CGPA %.2f; ", student.getCgpa()));
        }
        double pct = attendance.overallPercentageFor(student.getId());
        if (!attendance.findByStudent(student.getId()).isEmpty() && pct < ATTENDANCE_THRESHOLD) {
            reasons.append(String.format("attendance %.1f%%; ", pct));
        }
        long late = tasks.findByStudent(student.getId()).stream().filter(Task::isOverdue).count();
        if (late > 0) {
            reasons.append(late).append(" overdue task(s); ");
        }
        return String.format("%s %s %s", TextUtil.fit(student.getId(), 8),
                TextUtil.fit(student.getName(), 22),
                reasons.length() == 0 ? "-" : reasons.substring(0, reasons.length() - 2));
    }

    private String verdict(Student student) {
        if (isAtRisk(student)) {
            return "Needs attention - see the flags above.";
        }
        return "On track. Attendance and grades are both above threshold.";
    }

    private String line() {
        return "  " + "=".repeat(74);
    }
}
