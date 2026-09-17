package com.studentmanager.service;

import com.studentmanager.exception.*;
import com.studentmanager.model.Student;
import com.studentmanager.repository.FileRepository;
import java.util.*;

public class StudentService {
    private final FileRepository<Student> repo =
        new FileRepository<>("students.csv", Student::fromCsv, Student::toCsv);

    public List<Student> all(){return repo.findAll();}

    public void add(Student s){
        validate(s);
        if(all().stream().anyMatch(x->x.getId().equalsIgnoreCase(s.getId())))
            throw new ValidationException("Student ID already exists.");
        List<Student> list=all(); list.add(s); repo.saveAll(list);
    }

    public void update(String id,String name,String email,String course){
        List<Student> list=all();
        Student s=list.stream().filter(x->x.getId().equalsIgnoreCase(id)).findFirst()
                .orElseThrow(()->new DataNotFoundException("Student not found."));
        s.setName(name); s.setEmail(email); s.setCourse(course); validate(s); repo.saveAll(list);
    }

    public void delete(String id){
        List<Student> list=all();
        if(!list.removeIf(x->x.getId().equalsIgnoreCase(id)))
            throw new DataNotFoundException("Student not found.");
        repo.saveAll(list);
    }

    private void validate(Student s){
        if(s.getId().isBlank()||s.getName().isBlank()||s.getCourse().isBlank())
            throw new ValidationException("ID, name and course are required.");
        if(!s.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new ValidationException("Invalid email format.");
    }
}
