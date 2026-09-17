package com.studentmanager.service;

import com.studentmanager.exception.ValidationException;
import com.studentmanager.model.AttendanceRecord;
import com.studentmanager.repository.CsvRepository;
import com.studentmanager.repository.Repository;
import com.studentmanager.util.Validator;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business operations on {@link AttendanceRecord}s.
 *
 * <p>Enforces one record per student per subject. Allowing two rows for the same pair
 * would make the average attendance figure double-count that subject.</p>
 */
public class AttendanceService extends AbstractCrudService<AttendanceRecord> {

    public static final String HEADER = "id,studentId,subject,totalClasses,attendedClasses";

    private final StudentService studentService;

    public AttendanceService(StudentService studentService) {
        this(new CsvRepository<>("attendance.csv", HEADER, AttendanceRecord::fromCsv), studentService);
    }

    public AttendanceService(Repository<AttendanceRecord> repository, StudentService studentService) {
        super(repository);
        this.studentService = studentService;
    }

    @Override
    protected void validate(AttendanceRecord record) {
        Validator.requireId(record.getId(), "Attendance ID");
        Validator.requireId(record.getStudentId(), "Student ID");
        Validator.requireText(record.getSubject(), "Subject");
        Validator.requireValidAttendance(record.getTotalClasses(), record.getAttendedClasses());
        if (studentService != null && !studentService.findById(record.getStudentId()).isPresent()) {
            throw new ValidationException("No student exists with id '" + record.getStudentId() + "'.");
        }
        boolean duplicatePair = findAll().stream()
                .anyMatch(existing -> !existing.getId().equalsIgnoreCase(record.getId())
                        && existing.getStudentId().equalsIgnoreCase(record.getStudentId())
                        && existing.getSubject().equalsIgnoreCase(record.getSubject()));
        if (duplicatePair) {
            throw new ValidationException("Attendance for '" + record.getSubject()
                    + "' is already recorded for student " + record.getStudentId()
                    + ". Update the existing record instead.");
        }
    }

    @Override
    protected String entityName() {
        return "Attendance record";
    }

    @Override
    protected String idPrefix() {
        return "A";
    }

    /** Adds newly held and newly attended classes to an existing record. */
    public void addSession(String recordId, int held, int attended) {
        AttendanceRecord record = require(recordId);
        if (held <= 0 || attended < 0 || attended > held) {
            throw new ValidationException("Session values invalid: attended " + attended
                    + " of " + held + " classes held.");
        }
        record.setTotalClasses(record.getTotalClasses() + held);
        record.setAttendedClasses(record.getAttendedClasses() + attended);
        repository.update(record);
    }

    /** @return every record for one student, lowest attendance first. */
    public List<AttendanceRecord> findByStudent(String studentId) {
        return findAll().stream()
                .filter(r -> r.getStudentId().equalsIgnoreCase(studentId))
                .sorted(Comparator.comparingDouble(AttendanceRecord::percentage))
                .collect(Collectors.toList());
    }

    /**
     * Aggregate attendance for one student across all subjects.
     *
     * <p>Computed as total attended / total held, NOT as the mean of the per-subject
     * percentages. Those two figures differ whenever subjects have different class
     * counts, and only the former matches how a college actually computes attendance.</p>
     */
    public double overallPercentageFor(String studentId) {
        List<AttendanceRecord> records = findByStudent(studentId);
        int held = records.stream().mapToInt(AttendanceRecord::getTotalClasses).sum();
        int attended = records.stream().mapToInt(AttendanceRecord::getAttendedClasses).sum();
        return held == 0 ? 0.0 : attended * 100.0 / held;
    }

    /** @return institute-wide attendance percentage across every recorded subject. */
    public double overallPercentage() {
        int held = findAll().stream().mapToInt(AttendanceRecord::getTotalClasses).sum();
        int attended = findAll().stream().mapToInt(AttendanceRecord::getAttendedClasses).sum();
        return held == 0 ? 0.0 : attended * 100.0 / held;
    }

    /** @return records below the institutional minimum, worst first. */
    public List<AttendanceRecord> findAtRisk() {
        return findAll().stream()
                .filter(AttendanceRecord::isBelowMinimum)
                .sorted(Comparator.comparingDouble(AttendanceRecord::percentage))
                .collect(Collectors.toList());
    }

    /** Removes every attendance record for a student. Called when that student is deleted. */
    public int deleteByStudent(String studentId) {
        List<AttendanceRecord> owned = findByStudent(studentId);
        owned.forEach(r -> repository.deleteById(r.getId()));
        return owned.size();
    }
}
