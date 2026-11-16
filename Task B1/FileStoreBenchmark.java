package TaskB1;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileStoreBenchmark {

    // Task A2.1: Unbuffered byte-by-byte read (1 OS syscall per byte)
    public static long countLinesUnbuffered(Path path) throws IOException {
        long lines = 0;
        try (InputStream in = new FileInputStream(path.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                if (b == '\n') {
                    lines++;
                }
            }
        }
        return lines;
    }

    // Task A2.2: Buffered stream read (8 KB / 64 KB chunks)
    public static long countLinesBuffered(Path path) throws IOException {
        long lines = 0;
        try (InputStream in = new BufferedInputStream(new FileInputStream(path.toFile()), 65536)) {
            int b;
            while ((b = in.read()) != -1) {
                if (b == '\n') {
                    lines++;
                }
            }
        }
        return lines;
    }

    private static Path generateLogFile(int targetMB) throws IOException {
        Path temp = Files.createTempFile("attendance_" + targetMB + "MB_", ".log");
        byte[] lineBytes = "2026-11-15 08:30:12 | REG=192511064 | GATE=MAIN | STATUS=IN\n"
                .getBytes(StandardCharsets.UTF_8); // 64 bytes per line
        long targetBytes = (long) targetMB * 1024 * 1024;
        long written = 0;
        try (BufferedWriter bw = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            String lineStr = new String(lineBytes, StandardCharsets.UTF_8);
            while (written < targetBytes) {
                bw.write(lineStr);
                written += lineBytes.length;
            }
        }
        return temp;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== TASK B1.3: UNBUFFERED vs BUFFERED LINE COUNT BENCHMARK ===");
        System.out.printf("%-10s | %-12s | %-18s | %-18s | %-10s%n",
                "File Size", "Lines", "Unbuffered (ms)", "Buffered (ms)", "Speedup");
        System.out.println("----------------------------------------------------------------------------");

        int[] sizesMB = { 1, 10, 50 };
        for (int mb : sizesMB) {
            Path logFile = generateLogFile(mb);
            long t1 = System.nanoTime();
            long c1 = countLinesUnbuffered(logFile);
            double unbufMs = (System.nanoTime() - t1) / 1_000_000.0;

            long t2 = System.nanoTime();
            long c2 = countLinesBuffered(logFile);
            double bufMs = (System.nanoTime() - t2) / 1_000_000.0;

            System.out.printf("%-10s | %-12d | %-18.2f | %-18.2f | %-9.1fx%n",
                    mb + " MB", c2, unbufMs, bufMs, unbufMs / Math.max(bufMs, 0.01));
            Files.deleteIfExists(logFile);
        }

        System.out.println("\n=== TASK B1.4: PASS LOOKUP SCALING (1,000 to 100,000 RECORDS) ===");
        System.out.printf("%-14s | %-22s | %-22s%n", "Record Count", "Worst-Case Find (ms)", "Avg of 50 Searches (ms)");
        System.out.println("----------------------------------------------------------------");

        int[] recordCounts = { 1000, 10000, 100000 };
        for (int n : recordCounts) {
            Path passFile = Files.createTempFile("passes_scale_" + n + "_", ".csv");
            try (BufferedWriter bw = Files.newBufferedWriter(passFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (int i = 1; i <= n; i++) {
                    bw.write("P" + i + ",192511" + (i % 1000) + ",Student_" + i + "," + (i % 25 + 1) + ",4500.0");
                    bw.newLine();
                }
            }

            PassStore store = new PassStore(passFile);
            String targetId = "P" + n; // Last record (worst-case linear scan)

            long s1 = System.nanoTime();
            store.find(targetId);
            double worstMs = (System.nanoTime() - s1) / 1_000_000.0;

            long s2 = System.nanoTime();
            for (int q = 0; q < 50; q++) {
                store.find("P" + ((q * (n / 50)) + 1));
            }
            double avgMs = ((System.nanoTime() - s2) / 1_000_000.0) / 50.0;

            System.out.printf("%-14d | %-22.3f | %-22.3f%n", n, worstMs, avgMs);
            Files.deleteIfExists(passFile);
        }
    }
}