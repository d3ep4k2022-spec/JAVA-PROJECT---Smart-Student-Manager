package com.studentmanager.service;

import com.studentmanager.model.Student;
import com.studentmanager.repository.CsvRepository;
import com.studentmanager.repository.Repository;
import com.studentmanager.util.Validator;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business operations on {@link Student} records.
 *
 * <p>Inherits the whole CRUD algorithm from {@link AbstractCrudService} and adds only
 * the student-specific rules and queries.</p>
 */
public class StudentService extends AbstractCrudService<Student> {

    public static final String HEADER = "id,name,email,course,semester,cgpa";

    public StudentService() {
        this(new CsvRepository<>("students.csv", HEADER, Student::fromCsv));
    }

    /** Constructor injection, used by tests to substitute an in-memory repository. */
    public StudentService(Repository<Student> repository) {
        super(repository);
    }

    @Override
    protected void validate(Student student) {
        Validator.requireId(student.getId(), "Student ID");
        Validator.requireText(student.getName(), "Name");
        Validator.requireEmail(student.getEmail());
        Validator.requireText(student.getCourse(), "Course");
        Validator.requireRange(student.getSemester(), 1, 12, "Semester");
        Validator.requireRange(student.getCgpa(), 0.0, 10.0, "CGPA");
    }

    @Override
    protected String entityName() {
        return "Student";
    }

    @Override
    protected String idPrefix() {
        return "S";
    }

    /**
     * Case-insensitive substring search across name, id, email and course.
     *
     * @param keyword text to look for; blank returns every student
     */
    public List<Student> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String needle = keyword.trim().toLowerCase();
        return findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains(needle)
                        || s.getId().toLowerCase().contains(needle)
                        || s.getEmail().toLowerCase().contains(needle)
                        || s.getCourse().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

    /** @return all students ordered by CGPA, highest first. */
    public List<Student> rankedByCgpa() {
        return findAll().stream()
                .sorted(Comparator.comparingDouble(Student::getCgpa).reversed()
                        .thenComparing(Student::getName))
                .collect(Collectors.toList());
    }

    /** @return the mean CGPA across all students, or {@code 0.0} when there are none. */
    public double averageCgpa() {
        return findAll().stream()
                .mapToDouble(Student::getCgpa)
                .average()
                .orElse(0.0);
    }

    /** @return students grouped by course, for the analytics breakdown. */
    public java.util.Map<String, List<Student>> groupByCourse() {
        return findAll().stream()
                .collect(Collectors.groupingBy(Student::getCourse, java.util.TreeMap::new,
                        Collectors.toList()));
    }
}
