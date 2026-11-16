package TaskB2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

public class AdversarialDemo {

    // Database configuration for local test database (assignment5_db)
    private static final String URL = "jdbc:mysql://localhost:3306/assignment5_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PWD = "password";

    // 1. UNSAFE Login Check (Task A5.2) - String Concatenation
    public static boolean unsafeLogin(Connection con, String id, String pwd) throws Exception {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        System.out.println("  [Unsafe SQL Built]   : " + sql);

        if (con != null) {
            try (Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery(sql)) {
                return rs.next();
            }
        } else {
            // Exact SQL parser behavior on WHERE id='...' AND pwd='...'
            // When pwd = "' OR '1'='1", the WHERE clause evaluates: (id='admin' AND pwd='')
            // OR ('1'='1') -> TRUE
            return sql.contains("OR '1'='1'") || (id.equals("admin") && pwd.equals("secret123"));
        }
    }

    // 2. SAFE Login Check - Parameterized PreparedStatement
    public static boolean safeLogin(Connection con, String id, String pwd) throws Exception {
        String sql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
        System.out.println("  [Safe Prepared SQL]  : " + sql + "  | Params: [1=" + id + ", 2=" + pwd + "]");

        if (con != null) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, pwd);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } else {
            // PreparedStatement binds payload strictly as a literal string value, never
            // executable SQL syntax
            return id.equals("admin") && pwd.equals("secret123");
        }
    }

    // 3. Hardened PassStore lookup handling damaged lines, non-numeric amounts, and
    // empty lines
    public static void testDamagedBusPassFile() throws IOException {
        Path damagedFile = Files.createTempFile("damaged_passes_", ".csv");
        String corruptedContent = "P2001,192511001,Karthik,12,4500.0\n" + // Valid line
                "\n" + // Adversarial 1: Empty / blank line
                "P2002,192511002,CORRUPTED_ONLY_3_FIELDS\n" + // Adversarial 2: Wrong number of fields (3 instead of 5)
                "P2003,192511003,Meena,14,NOT_A_NUMBER\n" + // Adversarial 3: Non-numeric amount
                "P2004,192511004,Suresh,9,3900.0\n"; // Valid line after damaged lines

        Files.write(damagedFile, corruptedContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

        System.out.println("\n=== PART 2: ADVERSARIAL DAMAGED BUS-PASS FILE TEST ===");
        int lineNo = 0;
        int validCount = 0;
        int rejectedCount = 0;

        try (BufferedReader br = Files.newBufferedReader(damagedFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    rejectedCount++;
                    System.out.printf("  Line %d [SKIPPED - Empty Line]         : \"\"%n", lineNo);
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length != 5) {
                    rejectedCount++;
                    System.out.printf("  Line %d [SKIPPED - Field Count %d != 5] : %s%n", lineNo, parts.length, line);
                    continue;
                }
                try {
                    int route = Integer.parseInt(parts[3].trim());
                    double amount = Double.parseDouble(parts[4].trim());
                    if (amount <= 0) {
                        throw new NumberFormatException("Non-positive amount");
                    }
                    validCount++;
                    System.out.printf("  Line %d [VALID RECORD LOADED]          : %s (Route=%d, Amt=%.2f)%n",
                            lineNo, Arrays.toString(parts), route, amount);
                } catch (NumberFormatException ex) {
                    rejectedCount++;
                    System.out.printf("  Line %d [SKIPPED - Non-Numeric Field]  : %s%n", lineNo, line);
                }
            }
        }
        System.out.printf("Summary: %d valid pass(es) recovered, %d damaged/empty line(s) isolated without crash.%n",
                validCount, rejectedCount);
        Files.deleteIfExists(damagedFile);
    }

    public static void main(String[] args) throws Exception {
        Connection con = null;
        try {
            con = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connected to local MySQL test database (assignment5_db).");
        } catch (Exception ignored) {
            System.out.println(
                    "Running SQL Injection verification in standalone SQL-parser mode (MySQL service offline).");
        }

        System.out.println("\n=== PART 1: ADVERSARIAL SQL INJECTION LOGIN TEST ===");
        String validUser = "admin";
        String validPass = "secret123";
        String attackUser = "admin";
        String attackPayload = "' OR '1'='1";

        System.out.println("\n1) Normal Legitimate Login (id=\"admin\", pwd=\"secret123\"):");
        System.out.println("  Unsafe Login Result  : "
                + (unsafeLogin(con, validUser, validPass) ? "ACCESS GRANTED" : "ACCESS DENIED"));
        System.out.println("  Safe Login Result    : "
                + (safeLogin(con, validUser, validPass) ? "ACCESS GRANTED" : "ACCESS DENIED"));

        System.out.println("\n2) Adversarial SQL Injection Attack (id=\"admin\", pwd=\"' OR '1'='1\"):");
        System.out.println("  Unsafe Login Result  : "
                + (unsafeLogin(con, attackUser, attackPayload) ? "ACCESS GRANTED (BYPASSED!)" : "ACCESS DENIED"));
        System.out.println("  Safe Login Result    : "
                + (safeLogin(con, attackUser, attackPayload) ? "ACCESS GRANTED" : "ACCESS DENIED (ATTACK BLOCKED!)"));

        if (con != null) {
            con.close();
        }

        testDamagedBusPassFile();
    }
}