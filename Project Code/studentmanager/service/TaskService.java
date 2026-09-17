package com.studentmanager.service;

import com.studentmanager.exception.*;
import com.studentmanager.model.Task;
import com.studentmanager.repository.FileRepository;
import java.util.*;

public class TaskService {
    private final FileRepository<Task> repo =
        new FileRepository<>("tasks.csv", Task::fromCsv, Task::toCsv);

    public List<Task> all(){return repo.findAll();}
    public void add(Task t){
        if(t.getId().isBlank()||t.getTitle().isBlank()||t.getStudentId().isBlank())
            throw new ValidationException("Task ID, student ID and title are required.");
        if(all().stream().anyMatch(x->x.getId().equalsIgnoreCase(t.getId())))
            throw new ValidationException("Task ID already exists.");
        List<Task> l=all();l.add(t);repo.saveAll(l);
    }
    public void complete(String id){
        List<Task> l=all();
        Task t=l.stream().filter(x->x.getId().equalsIgnoreCase(id)).findFirst()
            .orElseThrow(()->new DataNotFoundException("Task not found."));
        t.setCompleted(true);repo.saveAll(l);
    }
    public void delete(String id){
        List<Task> l=all();
        if(!l.removeIf(x->x.getId().equalsIgnoreCase(id)))
            throw new DataNotFoundException("Task not found.");
        repo.saveAll(l);
    }
}
