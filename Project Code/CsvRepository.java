package com.studentmanager.repository;

import com.studentmanager.exception.StorageException;
import com.studentmanager.model.CsvSerializable;
import com.studentmanager.model.Identifiable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Flat-file {@link Repository} backed by one CSV file per entity type.
 *
 * <p>Design notes worth defending in a viva:</p>
 * <ul>
 *   <li><b>One generic class, three entity types.</b> The parser is injected as a
 *       {@code Function<String, T>} method reference, so {@code Student::fromCsv},
 *       {@code Task::fromCsv} and {@code AttendanceRecord::fromCsv} all reuse this code.</li>
 *   <li><b>Cached in memory.</b> The file is read once at construction; every later read
 *       is served from an {@link ArrayList}. The original version re-read the file on every
 *       single query, which made one "add student" operation perform three full file reads.</li>
 *   <li><b>Atomic writes.</b> Data is written to a temporary file and then moved over the
 *       real one, so a crash mid-write cannot leave a half-truncated CSV behind.</li>
 *   <li><b>Thread safe.</b> All mutating methods are {@code synchronized} on the repository
 *       instance, because the background backup thread reads the same list concurrently.</li>
 *   <li><b>Corrupt rows are skipped, not fatal.</b> A single malformed line produces a warning
 *       on {@code System.err} and the remaining records still load.</li>
 * </ul>
 *
 * @param <T> entity type; must expose an id and know how to serialise itself
 */
public class CsvRepository<T extends Identifiable & CsvSerializable> implements Repository<T> {

    /** Root folder for all CSV files, created on first use. */
    public static final String DATA_DIR = "data";

    private final Path file;
    private final String header;
    private final Function<String, T> parser;
    private final List<T> cache = new ArrayList<>();

    /**
     * @param fileName simple file name such as {@code "students.csv"}
     * @param header   comma-separated column names written as line 1 and skipped on read
     * @param parser   factory turning one CSV line into an entity
     */
    public CsvRepository(String fileName, String header, Function<String, T> parser) {
        this.file = Paths.get(DATA_DIR, fileName);
        this.header = header;
        this.parser = parser;
        initialiseFile();
        reload();
    }

    private void initialiseFile() {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(file)) {
                Files.write(file, List.of(header), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialise " + file + ": " + e.getMessage(), e);
        }
    }

    @Override
    public final synchronized void reload() {
        cache.clear();
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank() || (i == 0 && line.equalsIgnoreCase(header))) {
                    continue;
                }
                try {
                    cache.add(parser.apply(line));
                } catch (RuntimeException e) {
                    System.err.println("[warn] Skipping corrupt row " + (i + 1)
                            + " in " + file.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new StorageException("Could not read " + file + ": " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(cache); // defensive copy: callers cannot mutate the cache
    }

    @Override
    public synchronized Optional<T> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return cache.stream()
                .filter(entity -> entity.getId().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    @Override
    public synchronized boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public synchronized void save(T entity) {
        cache.add(entity);
        flush();
    }

    @Override
    public synchronized void update(T entity) {
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId().equalsIgnoreCase(entity.getId())) {
                cache.set(i, entity);
                flush();
                return;
            }
        }
        throw new StorageException("Cannot update absent record: " + entity.getId(), null);
    }

    @Override
    public synchronized boolean deleteById(String id) {
        boolean removed = cache.removeIf(entity -> entity.getId().equalsIgnoreCase(id.trim()));
        if (removed) {
            flush();
        }
        return removed;
    }

    @Override
    public synchronized int count() {
        return cache.size();
    }

    /** Writes the whole cache back to disk atomically (temp file then move). */
    private void flush() {
        List<String> lines = new ArrayList<>();
        lines.add(header);
        for (T entity : cache) {
            lines.add(entity.toCsv());
        }
        try {
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Could not write " + file + ": " + e.getMessage(), e);
        }
    }

    /** @return the path of the backing file, used by the backup service. */
    public Path getFile() {
        return file;
    }
}
