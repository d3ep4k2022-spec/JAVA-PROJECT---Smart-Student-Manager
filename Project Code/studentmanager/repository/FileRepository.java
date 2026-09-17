package com.studentmanager.repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileRepository<T> {
    private final Path file;
    private final java.util.function.Function<String,T> parser;
    private final java.util.function.Function<T,String> serializer;

    public FileRepository(String filename, java.util.function.Function<String,T> parser,
                          java.util.function.Function<T,String> serializer) {
        this.file=Paths.get("data",filename);
        this.parser=parser; this.serializer=serializer;
        try { Files.createDirectories(file.getParent()); if(!Files.exists(file)) Files.createFile(file); }
        catch(IOException e){throw new RuntimeException("Cannot initialize storage: "+e.getMessage());}
    }

    public List<T> findAll(){
        try {
            List<T> result=new ArrayList<>();
            for(String line:Files.readAllLines(file))
                if(!line.isBlank()) result.add(parser.apply(line));
            return result;
        } catch(IOException e){throw new RuntimeException("Cannot read storage: "+e.getMessage());}
    }

    public void saveAll(List<T> items){
        try {
            List<String> lines=items.stream().map(serializer).toList();
            Files.write(file,lines);
        } catch(IOException e){throw new RuntimeException("Cannot write storage: "+e.getMessage());}
    }
}
