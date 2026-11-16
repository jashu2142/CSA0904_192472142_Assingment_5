# CSA0904 – Java Assignment 5 (CO5)
## Data Persistence Engineering – Files and Databases

### Student Details
- **Name:** PURUSHOTHAMAN.V
- **Register Number:** 192511064
- **Department:** Computer Science and Engineering

### Development Environment
- **IDE:** Visual Studio Code (VS Code)
- **Programming Language:** Java
- **JDK:** Java SE Development Kit 8 (JDK 1.8)
- **Operating System:** Windows
- **Terminal:** VS Code Integrated Terminal (PowerShell)

### Java Version
```bash
java -version
javac -version
```
**Java Version:** Java SE Development Kit 8 (JDK 1.8)

### Compilation and Execution
All Java programs were compiled and executed from the root repository folder:

```bash
# Task B1 - Instrumented Bus-Pass File Store & Buffered vs Unbuffered Benchmark
javac TaskB1/PassStore.java TaskB1/FileStoreBenchmark.java
java -cp . TaskB1.PassStore
java -cp . TaskB1.FileStoreBenchmark

# Task B2 - Adversarial Inputs (SQL Injection & Corrupted Bus-Pass File)
javac TaskB2/AdversarialDemo.java
java -cp . TaskB2.AdversarialDemo

# Task B3 - Statement vs PreparedStatement vs Batch & Transaction Rollback
javac TaskB3/PassLoader.java TaskB3/PassBenchmark.java
java -cp . TaskB3.PassBenchmark

# Task B4 - Multi-Campus Migration Tool & DatabaseMetaData Inspection
javac TaskB4/CampusMigrationTool.java
java -cp . TaskB4.CampusMigrationTool
```

### Assignment Results

| Task | Description | Status | Key Outcome / Finding |
|---|---|---|---|
| Task B1 | Instrumented File Store & Buffering Benchmark | Completed | Buffering achieved a **1026.5x speedup** on 50 MB (`210.2 s` → `0.20 s`); `PassStore.find()` scaled linearly ($O(n)$) up to 100,000 records[cite: 21, 29] |
| Task B2 | Adversarial Input Testing (SQLi & Damaged CSV) | Completed | `PreparedStatement` blocked `' OR '1'='1` injection; `PassStore` skipped malformed/empty lines cleanly[cite: 23] |
| Task B3 | `Statement` vs `PreparedStatement` vs Batch | Completed | Batched `PreparedStatement` reduced 100,000 inserts from `141.2 s` to `1.68 s`; `con.rollback()` guaranteed 0 stranded rows on failure[cite: 23, 24] |
| Task B4 | Multi-Campus Migration & `DatabaseMetaData` | Completed | Migrated valid records atomically while reporting rejected lines; verified target schema via `DatabaseMetaData`[cite: 24, 25] |

### Screenshots, Input Files & SQL Script
- Output screenshots demonstrating every experimental investigation are stored inside `TaskB1` through `TaskB4`[cite: 17, 25].
- MySQL database schema for Tasks B2–B4 is located at `TaskB3/schema.sql`[cite: 23, 25].
- Sample migration input file for Task B4 is located at `TaskB4/passes_input.csv`[cite: 24, 25].

---
**Submitted by:** PURUSHOTHAMAN.V  
**Register Number:** 192511064
