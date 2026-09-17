# 🎓 Smart Student Manager

A console-based academic management system built with **Core Java (17+)** and **zero external dependencies**. It tracks student records, assignments, and attendance thresholds while generating real-time analytics reports. All data is persisted to plain CSV files with automatic background backups.

---

### 📌 Quick Links

* 📄 [Full Project Report](https://www.google.com/search?q=docs/report.pdf&utm_source=gemini)
* 💡 [Viva Preparation Guide](https://www.google.com/search?q=docs/viva-questions.md&utm_source=gemini)
* 📜 [Sample Session Transcript](https://www.google.com/search?q=demo/sample-session-transcript.txt&utm_source=gemini)

---

## ⚡ Key Modules

| Module | Purpose |
| --- | --- |
| 🧑‍🎓 **Student Management** | CRUD operations, record search, and dynamic CGPA-based ranking |
| 📋 **Task Management** | Priority & deadline scheduling, status tracking, and overdue alerts |
| 📅 **Attendance Tracker** | Per-subject session logging with automatic flags for attendance `< 75%` |
| 📊 **Analytics & Reports** | Threaded computation of institution summaries and per-student report cards |
| 💾 **Backup Engine** | Automatic background snapshots every 2 minutes with manual trigger support |

---

## 🖼️ Preview

> 📂 More interface previews available in [`docs/screenshots/`](https://www.google.com/search?q=docs/screenshots/&utm_source=gemini).

---

## 🏗️ System Architecture

```text
Presentation Layer    (Main, StudentManagerApp, ConsoleUI, InputUtil)
         │
Service Layer         (AbstractCrudService<T>, StudentService, TaskService,
         │             AttendanceService, AnalyticsService, BackupService)
         │
Repository Layer      (Repository<T> interface, CsvRepository<T> implementation)
         │
Storage Layer         (data/students.csv, data/tasks.csv, data/attendance.csv)

```

**System Diagrams:**

📐 [Architecture Overview](https://www.google.com/search?q=docs/diagrams/architecture.png&utm_source=gemini) · 🧩 [Class Diagram](https://www.google.com/search?q=docs/diagrams/class-diagram.png&utm_source=gemini) · 🔄 [Add-Student Sequence](https://www.google.com/search?q=docs/diagrams/sequence-add-student.png&utm_source=gemini) · 👤 [Use Case Diagram](https://www.google.com/search?q=docs/diagrams/use-case.png&utm_source=gemini)

---

## 💡 Engineering Highlights

* 🧬 **Intentional OOP:** `Person` serves as an abstract base (`getRole()`, `describe()`), extended by `Student`. Common validation and retrieval patterns live inside generic base classes (`AbstractCrudService<T>`) to eliminate boilerplate.
* 📦 **Generic Persistence:** A unified `CsvRepository<T & CsvSerializable Identifiable extends>` handles all disk I/O using method references (e.g., `Student::fromCsv`) instead of entity-specific readers.
* 🧵 **Non-Blocking Concurrency:**
* `BackupService` leverages a daemon-based `ScheduledExecutorService` for 2-minute recurring snapshots.
* Heavy analytics tasks execute via `ExecutorService` and `Future` workers to keep the CLI responsive.


* 🛡️ **Robust Error Handling:** A centralized 5-class hierarchy rooted in `StudentManagerException` catches input edge cases at the console loop boundary, preventing session crashes.
* 🔗 **Referential Integrity:** Enforces cascade-deletes and prevents orphaned foreign keys when assigning tasks or attendance to missing student IDs.

---

## ⚙️ Prerequisites

* ☕ **JDK 17+** (Built and verified on JDK 21)
* 🧰 **Dependencies:** None (Pure standard library)

---

## 🚀 Quickstart

### 1️⃣ Clone the Repository

```bash
git clone <this-repo-url>
cd SmartStudentManager

```

### 2️⃣ Build & Run

**Unix / macOS:**

```bash
./scripts/build.sh
./scripts/run.sh --seed

```

**Windows:**

```cmd
scripts\build.bat
scripts\run.bat --seed

```

**Direct Compilation (Manual):**

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.studentmanager.Main --seed

```

> 💡 **Tip:** The `--seed` flag preloads sample records for demonstration. Omit it to start with empty datasets.

---

## 📁 Repository Structure

```text
SmartStudentManager/
├── src/main/java/com/studentmanager/
│   ├── Main.java, StudentManagerApp.java
│   ├── model/        # Domain entities (Student, Task, AttendanceRecord, Enums)
│   ├── service/      # Business logic, schedulers, and analytics
│   ├── repository/   # Generic repository contracts and CSV engines
│   ├── exception/    # Custom exception hierarchy
│   └── util/         # CLI rendering, validators, and demo seeder
├── data/             # Persistent CSV flat files
├── docs/             # Diagrams, screenshots, viva prep, and report PDF
├── demo/             # Complete terminal execution transcripts
└── scripts/          # Shell and batch automation helpers

```

---

## 🧪 Demo Data Profile

Booting with `--seed` provisions realistic test fixtures across all modules:

* 👥 **6 Students** distributed across 3 degree tracks
* 📝 **10 Tasks** spanning all lifecycle stages (*Pending*, *In-Progress*, *Completed*, *Overdue*)
* 📉 **9 Attendance Records**, including 3 edge cases below the **75% minimum threshold**

---

## ⚠️ Known Constraints

* 🔒 **Single-Process Only:** Flat-file storage lacks multi-process record locking.
* 👤 **Zero Authentication:** Open console interface without role-based access control (RBAC).
* 🔢 **Readable IDs:** Uses simple formatted keys (`S001`, `T001`) instead of UUIDs to facilitate manual CSV grading inspections.

---

## 📜 License

Created as academic coursework. Open for inspection, reference, and educational adaptation.
