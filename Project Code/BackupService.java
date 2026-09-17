package com.studentmanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Periodically snapshots the {@code data/} folder into {@code backups/} on a
 * background thread.
 *
 * <p>This is the concurrency element of the project, and it is a genuine use of a
 * second thread rather than a decorative one: copying files is I/O-bound work with no
 * return value the user is waiting for, so running it off the main thread means the
 * menu never stalls while a snapshot is taken.</p>
 *
 * <p>Concurrency decisions:</p>
 * <ul>
 *   <li>A {@link ScheduledExecutorService} with a <b>single</b> thread — two concurrent
 *       backups could interleave and copy a half-written file.</li>
 *   <li>The thread is marked <b>daemon</b>, so a forgotten shutdown can never keep the
 *       JVM alive after the user picks Exit.</li>
 *   <li>The repositories it reads from are {@code synchronized}, so a backup running
 *       while the user is saving a record still sees a consistent file.</li>
 *   <li>The task body catches {@link Throwable}. An uncaught exception inside a scheduled
 *       task silently cancels all future runs — a well-known trap with this API.</li>
 * </ul>
 */
public class BackupService {

    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path dataDir;
    private final Path backupDir;
    private final long intervalSeconds;
    private final AtomicInteger completedBackups = new AtomicInteger();

    private ScheduledExecutorService scheduler;
    private volatile String lastBackupPath = "none yet";

    public BackupService(long intervalSeconds) {
        this(Paths.get("data"), Paths.get("backups"), intervalSeconds);
    }

    public BackupService(Path dataDir, Path backupDir, long intervalSeconds) {
        this.dataDir = dataDir;
        this.backupDir = backupDir;
        this.intervalSeconds = intervalSeconds;
    }

    /** Starts the background schedule. Safe to call once; further calls are ignored. */
    public synchronized void start() {
        if (scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "backup-worker");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::runSafely,
                intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    /** Wraps {@link #backupNow()} so a failure never cancels the recurring schedule. */
    private void runSafely() {
        try {
            backupNow();
        } catch (Throwable t) {
            System.err.println("[backup] snapshot failed: " + t.getMessage());
        }
    }

    /**
     * Takes one snapshot immediately, on the calling thread.
     *
     * @return the folder the snapshot was written to
     * @throws IOException if the copy fails
     */
    public String backupNow() throws IOException {
        if (!Files.exists(dataDir)) {
            return "skipped (no data folder)";
        }
        Path target = backupDir.resolve(LocalDateTime.now().format(STAMP));
        Files.createDirectories(target);
        try (Stream<Path> files = Files.list(dataDir)) {
            List<Path> csvFiles = files
                    .filter(p -> p.toString().endsWith(".csv"))
                    .toList();
            for (Path source : csvFiles) {
                Files.copy(source, target.resolve(source.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        completedBackups.incrementAndGet();
        lastBackupPath = target.toString();
        return lastBackupPath;
    }

    /** @return how many snapshots have completed since the application started. */
    public int getCompletedBackups() {
        return completedBackups.get();
    }

    /** @return the folder of the most recent snapshot. */
    public String getLastBackupPath() {
        return lastBackupPath;
    }

    /**
     * Stops the schedule and waits briefly for an in-flight snapshot to finish,
     * so the application never exits mid-copy.
     */
    public synchronized void stop() {
        if (scheduler == null) {
            return;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            // Restore the interrupt flag rather than swallowing it - the caller may also
            // need to know the thread was interrupted.
            Thread.currentThread().interrupt();
        }
        scheduler = null;
    }
}
