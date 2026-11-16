package TaskB1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PassStore {
    private final Path file;

    public PassStore(Path file) {
        this.file = file;
    }

    public void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", passId, regNo, name,
                    String.valueOf(route), String.valueOf(amount)));
            w.newLine();
        }
    }

    /**
     * Returns the five fields of the pass with the given ID, or null if not found.
     */
    public String[] find(String passId) throws IOException {
        // TODO 1: Read the file line by line with a BufferedReader inside
        // try-with-resources;
        // skip damaged lines (not exactly 5 fields) and return the fields of the
        // matching pass
        if (!Files.exists(file)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    continue; // Skip damaged line cleanly without aborting search
                }
                if (fields[0].trim().equals(passId)) {
                    return fields;
                }
            }
        }
        return null;
    }

    /** Lists all valid passes for a given route number (Task A1 operation iii). */
    public List<String[]> listByRoute(int route) throws IOException {
        List<String[]> matches = new ArrayList<>();
        if (!Files.exists(file)) {
            return matches;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    continue;
                }
                try {
                    if (Integer.parseInt(fields[3].trim()) == route) {
                        matches.add(fields);
                    }
                } catch (NumberFormatException ignored) {
                    // Skip corrupted numeric route field
                }
            }
        }
        return matches;
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes", ".csv");
        PassStore store = new PassStore(f);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.write(f, "P1002, DAMAGED LINE\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);

        System.out.println("=== Complete the Code Verification ===");
        System.out.println("Find P1003 (valid)   : " + Arrays.toString(store.find("P1003")));
        System.out.println("Find P1002 (damaged) : " + Arrays.toString(store.find("P1002")));
        System.out.println("Find P9999 (missing) : " + Arrays.toString(store.find("P9999")));
        System.out.println("List Route 12 count  : " + store.listByRoute(12).size());
        Files.delete(f);
    }
}