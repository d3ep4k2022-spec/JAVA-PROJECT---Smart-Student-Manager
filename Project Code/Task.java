package com.studentmanager.model;

import com.studentmanager.util.TextUtil;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * An academic task (assignment, lab, revision block) owned by one student.
 *
 * <p>CSV layout: {@code id,studentId,title,dueDate,priority,status}</p>
 *
 * <p>Implements {@link Comparable} so that {@code Collections.sort} produces the
 * natural "most urgent first" ordering used throughout the task listing.</p>
 */
public class Task implements Identifiable, CsvSerializable, Comparable<Task> {

    private static final int CSV_FIELDS = 6;

    private final String id;
    private final String studentId;
    private String title;
    private LocalDate dueDate;
    private Priority priority;
    private TaskStatus status;

    public Task(String id, String studentId, String title, LocalDate dueDate,
                Priority priority, TaskStatus status) {
        this.id = id == null ? "" : id.trim();
        this.studentId = studentId == null ? "" : studentId.trim();
        this.title = title == null ? "" : title.trim();
        this.dueDate = dueDate;
        this.priority = priority == null ? Priority.MEDIUM : priority;
        this.status = status == null ? TaskStatus.PENDING : status;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title.trim();
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /** @return {@code true} when the deadline has passed and the task is still open. */
    public boolean isOverdue() {
        return dueDate != null && !status.isClosed() && dueDate.isBefore(LocalDate.now());
    }

    /**
     * @return whole days from today until the deadline; negative when overdue,
     *         {@link Long#MAX_VALUE} when no deadline is set
     */
    public long daysRemaining() {
        if (dueDate == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Natural order: earliest deadline first; ties broken by higher priority,
     * then by task id so the sort is stable and reproducible.
     */
    @Override
    public int compareTo(Task other) {
        if (this.dueDate != null && other.dueDate != null) {
            int byDate = this.dueDate.compareTo(other.dueDate);
            if (byDate != 0) {
                return byDate;
            }
        } else if (this.dueDate == null && other.dueDate != null) {
            return 1;
        } else if (this.dueDate != null) {
            return -1;
        }
        int byPriority = Integer.compare(other.priority.getWeight(), this.priority.getWeight());
        return byPriority != 0 ? byPriority : this.id.compareToIgnoreCase(other.id);
    }

    @Override
    public String toCsv() {
        return String.join(",",
                CsvSerializable.sanitise(id),
                CsvSerializable.sanitise(studentId),
                CsvSerializable.sanitise(title),
                dueDate == null ? "" : dueDate.toString(),
                priority.name(),
                status.name());
    }

    public static Task fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != CSV_FIELDS) {
            throw new IllegalArgumentException(
                    "Expected " + CSV_FIELDS + " columns in tasks.csv but found " + parts.length);
        }
        LocalDate due = null;
        if (!parts[3].isBlank()) {
            try {
                due = LocalDate.parse(parts[3].trim());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Bad ISO date in row: " + line, e);
            }
        }
        return new Task(parts[0], parts[1], parts[2], due,
                Priority.parse(parts[4]), TaskStatus.parse(parts[5]));
    }

    @Override
    public String toString() {
        String flag = isOverdue() ? "OVERDUE" : status.getLabel();
        return String.format("%s %s %s %s %s %s",
                TextUtil.fit(id, 8), TextUtil.fit(studentId, 8), TextUtil.fit(title, 30),
                TextUtil.fit(dueDate == null ? "-" : dueDate.toString(), 12),
                TextUtil.fit(priority.getLabel(), 8), flag);
    }
}
