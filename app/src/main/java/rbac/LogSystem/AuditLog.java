package rbac.LogSystem;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class AuditLog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private List<AuditEntry> entries = new CopyOnWriteArrayList<>();
    private  Thread workThread;
    private volatile boolean running = true;

    public AuditLog() {
        Runnable worker = this::worker;
        workThread = new Thread(worker);
        workThread.start();
    }

    private void worker() {
        while (running) {
            try {
                AuditEntry entry = queue.take();
                entries.add(entry);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        List<AuditEntry> remainder = new ArrayList<>();
        queue.drainTo(remainder);
        entries.addAll(remainder);
    }

    public void shutdown() {
        running = false;
        workThread.interrupt();

        try {
            workThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void log(String action, String performer, String target, String details) {
        if (action == null || action.trim().isEmpty()){
            throw new IllegalArgumentException("Action not be empty or null");
        }
        if (performer == null || performer.trim().isEmpty()){
            throw new IllegalArgumentException("Performer not be empty or null");
        }
        if (target == null || target.trim().isEmpty()){
            throw new IllegalArgumentException("Target not be empty or null");
        }

        AuditEntry log = new AuditEntry(LocalDateTime.now().format(DATE_FORMAT), action.toUpperCase(Locale.ROOT), performer, target.toLowerCase(), details);
        queue.offer(log);
    }

    public List<AuditEntry> getAll() {
        return  entries;
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream().filter(value -> value.performer().toLowerCase().equals(performer.toLowerCase())).toList();
    }

    public void printLog() {
        for (AuditEntry value : entries) {
            System.out.println( value.timestamp() + ": " + value.performer() + " do " + value.action() + " on " + value.target() + ". Comment: " + value.details());
        }
    }

    public void saveToFile(String filename) {
        List<AuditEntry> helpList = new ArrayList<>(entries);

        filename = filename.trim();
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("{\n");
            writer.write("  \"logs\": [\n");

            for (int i = 0; i < helpList.size(); i++) {
                AuditEntry value = helpList.get(i);
                writer.write("    {\n");
                writer.write("      \"timestamp\": \"" + value.timestamp() + "\",\n");
                writer.write("      \"action\": \"" + value.action() + "\",\n");
                writer.write("      \"performer\": \"" + value.performer() + "\",\n");
                writer.write("      \"target\": \"" + value.target() + "\",\n");
                writer.write("      \"details\": \"" + value.details() + "\"\n");
                writer.write("    }");

                if (i < helpList.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }

            writer.write("  ]\n");
            writer.write("}\n");
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }

    }
}
