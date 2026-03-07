package rbac.SystemValidation;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuditLog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");

    List<AuditEntry> entries = new ArrayList<>();

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

        entries.add(new AuditEntry(LocalDateTime.now().format(DATE_FORMAT), action.toUpperCase(Locale.ROOT), performer, target.toLowerCase(), details));
    }

    public List<AuditEntry> getAll() {
        return  entries;
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return  entries.stream().filter(value -> value.performer().toLowerCase().equals(performer.toLowerCase())).toList();
    }

    public void printLog() {
        for (AuditEntry value : entries) {
            System.out.println( value.timestamp() + ": " + value.performer() + " do " + value.action() + " on " + value.target() + ". Comment: " + value.details());
        }
    }

    public void saveToFile(String filename) {
        filename = filename.trim();
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("{\n");
            writer.write("  \"logs\": [\n");

            for (int i = 0; i < entries.size(); i++) {
                AuditEntry value = entries.get(i);
                writer.write("    {\n");
                writer.write("      \"timestamp\": \"" + value.timestamp() + "\",\n");
                writer.write("      \"action\": \"" + value.action() + "\",\n");
                writer.write("      \"performer\": \"" + value.performer() + "\",\n");
                writer.write("      \"target\": \"" + value.target() + "\",\n");
                writer.write("      \"details\": \"" + value.details() + "\"\n");
                writer.write("    }");

                if (i < entries.size() - 1) {
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
