# Project Statement

## Problem Statement
College students often manage academic tasks, attendance, and student information using
separate notes or applications. This makes it difficult to maintain consistent records,
cross-reference them against each other, and quickly understand a student's overall
academic status.

## Scope
Smart Student Manager is a console-based Core Java system that stores student records,
academic tasks, and attendance records in one place, enforces consistency between them
(a task or attendance entry can't exist for a student who isn't on roll), and generates
analytics that reason across all three record types at once.

## Target Users
- College students tracking their own coursework and attendance
- Student coordinators managing a small cohort
- Small academic groups without access to a full ERP system

## High-Level Features
- Student CRUD, search, and CGPA ranking
- Task CRUD with priority, deadlines, and status tracking; overdue detection
- Attendance recording, session logging, and 75% threshold risk flagging
- Cross-entity academic analytics (institution-wide summary and per-student report cards)
- CSV-based persistence with atomic writes and in-memory caching
- Centralized validation and a custom exception hierarchy so invalid input never crashes
  the session
- Automatic background backups and multithreaded report generation

## Out of Scope
- Multi-user access control / authentication
- Concurrent access from more than one OS process to the same data files
- A graphical or web-based interface (console only)
