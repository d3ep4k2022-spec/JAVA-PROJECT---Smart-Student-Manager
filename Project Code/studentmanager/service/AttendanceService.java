package com.studentmanager.service;

import com.studentmanager.exception.ValidationException;
import com.studentmanager.model.AttendanceRecord;
import com.studentmanager.repository.FileRepository;
import java.util.*;

public class AttendanceService {
    private final FileRepository<AttendanceRecord> repo =
        new FileRepository<>("attendance.csv", AttendanceRecord::fromCsv, AttendanceRecord::toCsv);

    public List<AttendanceRecord> all(){return repo.findAll();}

    public void add(AttendanceRecord a){
        if(a.getStudentId().isBlank()||a.getSubject().isBlank())
            throw new ValidationException("Student ID and subject are required.");
        if(a.getTotalClasses()<=0||a.getAttendedClasses()<0||a.getAttendedClasses()>a.getTotalClasses())
            throw new ValidationException("Attendance values are invalid.");
        List<AttendanceRecord> l=all();l.add(a);repo.saveAll(l);
    }
}
