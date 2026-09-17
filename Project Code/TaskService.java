package com.studentmanager.service;

import com.studentmanager.exception.ValidationException;
import com.studentmanager.model.Task;
import com.studentmanager.model.TaskStatus;
import com.studentmanager.repository.CsvRepository;
import com.studentmanager.repository.Repository;
import com.studentmanager.util.Validator;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business operations on {@link Task} records.
 *
 * <p>Holds a reference to {@link StudentService} so it can enforce referential
 * integrity: a task may not be created for a student id that does not exist. The
 * original design allowed orphan tasks, which then broke the per-student analytics.</p>
 */
public class TaskService extends AbstractCrudService<Task> {

    public static final String HEADER = "id,studentId,title,dueDate,priority,status";

    private final StudentService studentService;

    public TaskService(StudentService studentService) {
        this(new CsvRepository<>("tasks.csv", HEADER, Task::fromCsv), studentService);
    }

    public TaskService(Repository<Task> repository, StudentService studentService) {
        super(repository);
        this.studentService = studentService;
    }

    @Override
    protected void validate(Task task) {
        Validator.requireId(task.getId(), "Task ID");
        Validator.requireText(task.getTitle(), "Title");
        Validator.requireId(task.getStudentId(), "Student ID");
        if (studentService != null && !studentService.findById(task.getStudentId()).isPresent()) {
            throw new ValidationException("No student exists with id '" + task.getStudentId()
                    + "'. Add the student before assigning tasks.");
        }
    }

    @Override
    protected String entityName() {
        return "Task";
    }

    @Override
    protected String idPrefix() {
        return "T";
    }

    /** Moves a task to a new lifecycle state. */
    public void changeStatus(String taskId, TaskStatus status) {
        Task task = require(taskId);
        task.setStatus(status);
        repository.update(task);
    }

    /** Convenience wrapper used by the menu's "mark complete" option. */
    public void complete(String taskId) {
        changeStatus(taskId, TaskStatus.COMPLETED);
    }

    /** @return every task in natural order: earliest deadline first, then highest priority. */
    public List<Task> findAllSorted() {
        return findAll().stream().sorted().collect(Collectors.toList());
    }

    /** @return the tasks belonging to one student, in natural order. */
    public List<Task> findByStudent(String studentId) {
        return findAll().stream()
                .filter(t -> t.getStudentId().equalsIgnoreCase(studentId))
                .sorted()
                .collect(Collectors.toList());
    }

    /** @return open tasks whose deadline has already passed, most overdue first. */
    public List<Task> findOverdue() {
        return findAll().stream()
                .filter(Task::isOverdue)
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
    }

    /** @return tasks in a given lifecycle state. */
    public List<Task> findByStatus(TaskStatus status) {
        return findAll().stream()
                .filter(t -> t.getStatus() == status)
                .sorted()
                .collect(Collectors.toList());
    }

    /** @return completion rate as a percentage of all tasks, or {@code 0.0} when none exist. */
    public double completionRate() {
        List<Task> all = findAll();
        if (all.isEmpty()) {
            return 0.0;
        }
        long done = all.stream().filter(t -> t.getStatus().isClosed()).count();
        return done * 100.0 / all.size();
    }

    /** @return count of tasks per status, for the analytics summary. */
    public java.util.Map<TaskStatus, Long> countByStatus() {
        return findAll().stream()
                .collect(Collectors.groupingBy(Task::getStatus,
                        () -> new java.util.EnumMap<>(TaskStatus.class),
                        Collectors.counting()));
    }

    /** Removes every task owned by a student. Called when that student is deleted. */
    public int deleteByStudent(String studentId) {
        List<Task> owned = findByStudent(studentId);
        owned.forEach(t -> repository.deleteById(t.getId()));
        return owned.size();
    }
}
