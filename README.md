# Lab Equipment Booking and Maintenance System

**Course:** Minor Project (Non-Syllabus Project - NSP) — 3rd Semester  
**Institution:** Poornima Institute of Engineering & Technology, Jaipur  
**Department:** Department of Computer Engineering  
**Academic Year:** 2026–27  
**Technology Stack:** Java 17 LTS, Java Swing, FlatLaf Modern Look & Feel, JDBC, MySQL 8.0, Embedded SQLite, Apache Maven  

---

## 📌 Project Overview

The **Lab Equipment Booking and Maintenance System** is an enterprise-grade desktop management software designed to automate and digitize the complete operational lifecycle of academic and research laboratory equipment. 

In institutional environments housing high-value computing and precision measurement assets (such as NVIDIA GPU deep learning rigs, digital storage oscilloscopes, managed network switches, RF spectrum analyzers, and 3D printers), legacy paper logbooks lead to reservation collisions, untracked calibration drift, and unbudgeted maintenance costs. This application resolves these challenges through automated conflict detection, structured preventive and corrective maintenance logging, role-based access control, and one-click regulatory audit exports.

---

## 🌟 Key Features

### 1. Modern, Responsive Java Swing Interface
- Built using **FlatLaf 3.4.1**, delivering modern styling with rounded components, card panels, elevated shadows, and high-DPI scaling.
- Custom table cell renderers with **dynamic color-coded status pills** (e.g., Emerald for *AVAILABLE*, Amber for *BOOKED*, Rose for *UNDER_MAINTENANCE*, Blue for *COMPLETED*).
- Live real-time clock and institutional branding.

### 2. Dual-Engine Database Architecture (Plug & Play)
- **Primary:** Full enterprise **MySQL 8.0** connectivity with connection pooling, transactional safety, and foreign key cascades.
- **Intelligent Fallback:** If a live MySQL server is not detected, the system **automatically initializes an embedded SQLite database** (`lab_management.db`) and seeds comprehensive sample data. This guarantees zero-configuration demonstration for examiners and evaluators without connection errors.

### 3. Equipment Master Catalog (CRUD)
- Full Create, Read, Update, and Delete operations for assets across 6 departmental categories.
- Tracks unique asset tags, model specifications, manufacturer serial numbers, assigned lab rooms, purchase dates, and condition remarks.
- Keystroke-responsive live search and multi-parameter filtering (by Category, Room, and Status).

### 4. Conflict-Free Reservation Engine
- Algorithmic time-slot overlap validation preventing double-booking of any asset.
- Tracks academic purpose, reserved time windows, and responsible user.
- Streamlined check-out issuance and physical return verification that automatically restores asset availability.

### 5. Maintenance & Service Lifecycle Management
- Schedules and logs Preventive Maintenance, Annual Calibration, Corrective Repair, and Firmware Upgrades.
- Automatically transitions equipment to `UNDER_MAINTENANCE` upon service ticket opening, preventing booking of faulty assets.
- Automatically restores equipment to `AVAILABLE` upon inspection sign-off.
- Records technician details, diagnostic remarks, and incurred service costs (in INR).

### 6. Role-Based Access Control (RBAC) & Security
- Cryptographic **SHA-256 password hashing** for credential security.
- Distinct privilege tiers for **Administrator / Lab In-Charge**, **Faculty Members**, **Students**, and **Maintenance Technicians**.
- Dedicated Administrator user management panel (role modification, account activation/blocking, password resets).

### 7. Analytical Reporting & CSV Data Export
- One-click export of complete datasets to standard **CSV format** (`lab_equipment.csv`, `lab_bookings.csv`, `lab_maintenance.csv`).
- Real-time generation of executive text audit summaries displaying inventory utilization metrics and active service tickets.
- Immutable system audit trail tracking all user transactions.

---

## 🗄️ Database Architecture & DDL

The database contains six 3NF-normalized tables:
1. `users` — Authentication credentials, academic role, department, contact info, status.
2. `categories` — Hardware domains (HPC & AI, Test & Measurement, Networking, Embedded & IoT, Robotics, Rapid Prototyping).
3. `equipment` — Hardware asset records with status, room location, and serial numbers.
4. `bookings` — Time-window reservation records with conflict validation.
5. `maintenance_records` — Service tickets, technician logs, calibration dates, and expenditure.
6. `audit_logs` — Immutable audit trail of administrative and reservation events.

A standalone SQL script is available at [`schema.sql`](schema.sql) for import into MySQL Workbench or phpMyAdmin.

---

## 🔑 Pre-Configured Demo Accounts

For immediate evaluation, the login screen provides convenient quick-fill buttons for all roles:

| Role | Username | Password | Full Name / Description |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Prof. Anil Gupta (Lab In-Charge) |
| **Faculty** | `prof_sharma` | `faculty123` | Dr. Ramesh Sharma (AI & Data Science) |
| **Faculty** | `dr_verma` | `faculty123` | Dr. Sunita Verma (Electronics & Comm.) |
| **Technician** | `tech_rajesh` | `tech123` | Mr. Rajesh Kumar (Central Facilities) |
| **Student** | `ayush_kumar` | `student123` | Ayush Kumar (Computer Engineering) |
| **Student** | `rohit_meena` | `student123` | Rohit Meena (Information Technology) |

*(Note: Passwords can also be changed at any time using the built-in password reset feature).*

---

## 🚀 How to Run the Project

### Prerequisites
- **Java Development Kit (JDK 17 or higher)**
- **Apache Maven 3.6+** (optional if running pre-built JAR)

### Option 1: Run via Executable Launcher (Recommended)

#### On Linux / macOS:
```bash
./run.sh
```

#### On Windows:
Double-click `run.bat` or run:
```cmd
run.bat
```

### Option 2: Run via Pre-Built Fat JAR
```bash
java -jar target/LabEquipmentSystem.jar
```

### Option 3: Compile and Run with Maven
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.poornima.lab.Main"
```

---

## 📋 14-Week NSP Curriculum Alignment Matrix

| Week | Planned NSP Milestone | Implementation Evidence |
| :---: | :--- | :--- |
| **Week 1-2** | Environment setup & Java fundamentals | Maven project structure, Java 17 configuration, adopted design patterns. |
| **Week 3** | Basic Swing forms (Login & Registration) | `LoginPanel`, `LoginFrame`, `RegisterDialog` with input validation. |
| **Week 4** | Advanced Swing components | `ModernTable`, `JComboBox`, `JCheckBox`, `JMenu`, `JScrollPane`, FlatLaf styling. |
| **Week 5** | Event Handling in Java Swing | `ActionListener`, `DocumentListener` (live search), `MouseListener` (row double-click). |
| **Week 6-7** | GUI Prototype & System Screens | Complete dashboard layout, KPI stat cards, modular `CardLayout` architecture. |
| **Week 8** | Database Design & Schema Creation | Normalized 6-table relational schema with keys, triggers, and [`schema.sql`](schema.sql). |
| **Week 9** | JDBC Connectivity & Architecture | `DatabaseConfig.java` supporting automated MySQL connection with SQLite fallback. |
| **Week 10** | JDBC CRUD Implementation | Dedicated DAOs (`UserDAO`, `EquipmentDAO`, `BookingDAO`, `MaintenanceDAO`, `AuditLogDAO`). |
| **Week 11** | GUI & Database Integration + Validation | `ValidationUtil.java`, `SecurityUtil.java` (SHA-256), date-range conflict detection. |
| **Week 12** | Data Display, Live Search & Reporting | Dynamic table sorting, status chip renderers, `ExportUtil.java` (CSV & TXT reports). |
| **Week 13** | Code Quality, Standards & Error Recovery | Exception handling, try-with-resources, logging, and polished UI aesthetics. |
| **Week 14** | Final Demonstration & Documentation | Executable Fat JAR and official project report matching college SRS guidelines. |

---

## 📑 Project Report & Documentation

The official project report has been prepared in `.docx` format strictly complying with the guidelines specified in `NSP Report Format 3rd Semester.docx`:
- **Document File:** [`Lab_Equipment_Booking_and_Maintenance_System_Report.docx`](Lab_Equipment_Booking_and_Maintenance_System_Report.docx)
- **Font & Spacing:** Times New Roman, 12 pt body, 1.5 line spacing, 16 pt bold chapter headings, 14 pt bold section headings.
- **Cover Page:** Formal title, institution emblem, student and faculty submission details.
- **Embedded Diagrams & Snapshots:** Includes Use Case Diagram, Architectural Workflow Flowchart, and 12 high-resolution screenshots of the working software.
