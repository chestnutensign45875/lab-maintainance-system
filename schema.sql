-- =====================================================================
-- LAB EQUIPMENT BOOKING AND MAINTENANCE SYSTEM
-- Database Schema for MySQL
-- Minor Project (Non-Syllabus Project)
-- Department of Computer Engineering
-- Poornima Institute of Engineering & Technology, Jaipur
-- =====================================================================

CREATE DATABASE IF NOT EXISTS lab_db;
USE lab_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL DEFAULT 'STUDENT', -- ADMIN, FACULTY, STUDENT, TECHNICIAN
    department VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, BLOCKED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Equipment Categories Table
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- 3. Equipment Table
CREATE TABLE IF NOT EXISTS equipment (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    asset_tag VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category_id INT NOT NULL,
    model VARCHAR(100),
    serial_number VARCHAR(100),
    lab_room VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, BOOKED, UNDER_MAINTENANCE, DECOMMISSIONED
    purchase_date DATE,
    condition_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

-- 4. Equipment Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id INT NOT NULL,
    user_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    purpose TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, ACTIVE, COMPLETED, CANCELLED, REJECTED
    remarks TEXT,
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Maintenance Records Table
CREATE TABLE IF NOT EXISTS maintenance_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id INT NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL, -- PREVENTIVE, CALIBRATION, REPAIR, INSPECTION
    start_date DATE NOT NULL,
    completion_date DATE,
    technician_name VARCHAR(100) NOT NULL,
    cost DECIMAL(10, 2) DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE
);

-- 6. System Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(50) NOT NULL,
    entity VARCHAR(50) NOT NULL,
    entity_id INT,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Indexes for Fast Query Performance
CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_equipment_category ON equipment(category_id);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_time ON bookings(start_time, end_time);
CREATE INDEX idx_maintenance_equipment ON maintenance_records(equipment_id);

-- ---------------------------------------------------------------------
-- Seed Initial Data
-- ---------------------------------------------------------------------

-- Categories
INSERT INTO categories (category_id, name, description) VALUES
(1, 'HPC & AI Workstations', 'High-performance GPU computing clusters and deep learning stations'),
(2, 'Test & Measurement', 'Oscilloscopes, digital multimeters, spectrum and logic analyzers'),
(3, 'Networking & Infrastructure', 'Managed switches, routers, firewalls, and rack servers'),
(4, 'Embedded & IoT Kits', 'Raspberry Pi, NVIDIA Jetson, STM32, ESP32, and Arduino platforms'),
(5, 'Robotics & Automation', 'Robotic arms, sensor integration testbeds, and motor controllers'),
(6, 'Rapid Prototyping', 'Precision 3D printers, PCB milling machines, and soldering stations')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Users (Passwords are salted SHA-256 hashes or plain hashes supported by SecurityUtil)
INSERT INTO users (user_id, username, password_hash, full_name, email, role, department, phone, status) VALUES
(1, 'admin', 'admin123', 'Prof. Anil Gupta', 'admin@poornima.edu.in', 'ADMIN', 'Computer Engineering', '+91 9829012345', 'ACTIVE'),
(2, 'prof_sharma', 'faculty123', 'Dr. Ramesh Sharma', 'rsharma@poornima.edu.in', 'FACULTY', 'AI & Data Science', '+91 9829054321', 'ACTIVE'),
(3, 'dr_verma', 'faculty123', 'Dr. Sunita Verma', 'sverma@poornima.edu.in', 'FACULTY', 'Electronics & Comm.', '+91 9829098765', 'ACTIVE'),
(4, 'tech_rajesh', 'tech123', 'Mr. Rajesh Kumar', 'rkumar@poornima.edu.in', 'TECHNICIAN', 'Central Lab Facilities', '+91 9414011223', 'ACTIVE'),
(5, 'ayush_kumar', 'student123', 'Ayush Kumar', 'ayush.kumar@poornima.edu.in', 'STUDENT', 'Computer Engineering', '+91 9123456780', 'ACTIVE'),
(6, 'rohit_meena', 'student123', 'Rohit Meena', 'rohit.meena@poornima.edu.in', 'STUDENT', 'Information Technology', '+91 9876543210', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Equipment
INSERT INTO equipment (equipment_id, asset_tag, name, category_id, model, serial_number, lab_room, status, purchase_date, condition_notes) VALUES
(1, 'EQ-HPC-01', 'NVIDIA RTX 4090 Deep Learning Rig', 1, 'Dual RTX 4090 24GB / i9-14900K / 64GB DDR5', 'SN-NV4090-8812', 'Lab 301 - AI & Vision Lab', 'AVAILABLE', '2025-08-10', 'Excellent condition, liquid cooling verified'),
(2, 'EQ-TM-02', 'Rigol DS1054Z 50MHz Digital Oscilloscope', 2, 'DS1054Z 4-Channel 1GSa/s', 'SN-RGL-554109', 'Lab 204 - Embedded Systems', 'AVAILABLE', '2024-03-15', 'Calibrated until Dec 2026'),
(3, 'EQ-NET-03', 'Cisco Catalyst 2960-X Gigabit Switch', 3, 'WS-C2960X-24TS-L 24 Ports PoE+', 'SN-CSCO-992144', 'Lab 102 - Advanced Networks', 'BOOKED', '2023-11-20', 'All gigabit ports operational'),
(4, 'EQ-TM-04', 'Keysight 34461A 6.5 Digit Truevolt DMM', 2, '34461A Truevolt Digital Multimeter', 'SN-KEYS-33211', 'Lab 204 - Embedded Systems', 'AVAILABLE', '2024-06-18', 'Certified precision tolerance'),
(5, 'EQ-IOT-05', 'NVIDIA Jetson AGX Orin 64GB Dev Kit', 4, 'AGX Orin 275 TOPS AI', 'SN-JT-774412', 'Lab 301 - AI & Vision Lab', 'AVAILABLE', '2025-01-12', 'JetPack 6.0 installed with TensorRT'),
(6, 'EQ-IOT-06', 'Raspberry Pi 5 Edge AI Compute Cluster', 4, 'Quad RPi 5 8GB Nodes with PCIe NVMe Hat', 'SN-RPI-NODE-04', 'Lab 302 - IoT Innovations', 'BOOKED', '2025-02-01', 'Docker Swarm cluster active'),
(7, 'EQ-TM-07', 'Rohde & Schwarz Spectrum Analyzer 3GHz', 2, 'FPC1500 5kHz - 3GHz Tracking Gen', 'SN-RS-100293', 'Lab 206 - RF & Microwave', 'AVAILABLE', '2024-09-05', 'RF probe kit complete in flight case'),
(8, 'EQ-ROB-08', 'DOBOT Magician Educational Robotic Arm', 5, 'Magician v2 4-DOF with Pneumatic Gripper', 'SN-DOB-44321', 'Lab 108 - Robotics Lab', 'AVAILABLE', '2024-10-14', 'Vacuum pump and gripper calibrated'),
(9, 'EQ-RP-09', 'Creality Ender-3 V3 KE High-Speed 3D Printer', 6, 'Ender-3 V3 KE 500mm/s CoreXZ', 'SN-CR-908122', 'Lab 108 - Rapid Prototyping', 'AVAILABLE', '2025-03-01', 'PEI build plate, direct drive extruder'),
(10, 'EQ-TM-10', 'Saleae Logic Pro 16 Logic Analyzer', 2, 'Logic Pro 16 USB 3.0 500MS/s', 'SN-SAL-88201', 'Lab 205 - Signal Processing', 'AVAILABLE', '2024-05-11', 'Includes 16 micro-grabber clips'),
(11, 'EQ-TM-11', 'FLIR E4 Infrared Thermal Imaging Camera', 2, 'E4 WiFi MSX Enhancement 80x60', 'SN-FLIR-09124', 'Lab 204 - Embedded Systems', 'UNDER_MAINTENANCE', '2023-12-01', 'Battery pack replacement scheduled'),
(12, 'EQ-DSP-12', 'Texas Instruments TMS320C6748 DSP Kit', 4, 'LCDK-C6748 Floating Point DSP Starter', 'SN-TI-6748-09', 'Lab 205 - Signal Processing', 'UNDER_MAINTENANCE', '2023-09-10', 'Firmware JTAG flash in progress')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Bookings
INSERT INTO bookings (booking_id, equipment_id, user_id, start_time, end_time, purpose, status, remarks) VALUES
(1, 3, 5, '2026-11-14 09:00:00', '2026-11-14 13:00:00', 'VLAN Configuration and Network Security Lab Practical', 'ACTIVE', 'Issued to Ayush Kumar with patch cables'),
(2, 6, 2, '2026-11-14 14:00:00', '2026-11-14 18:00:00', 'Distributed Edge AI Benchmarking for M.Tech Thesis', 'ACTIVE', 'Allocated 4 nodes cluster for model evaluation'),
(3, 1, 5, '2026-11-15 10:00:00', '2026-11-15 16:00:00', 'Deep Neural Network Training for Final Year Capstone', 'APPROVED', 'Scheduled for GPU cluster room 301'),
(4, 2, 6, '2026-11-13 11:00:00', '2026-11-13 14:00:00', 'Pulse Width Modulation Signal Analysis for Power Inverter', 'COMPLETED', 'Returned in perfect condition and verified'),
(5, 5, 2, '2026-11-12 10:00:00', '2026-11-12 15:00:00', 'YOLOv10 Real-Time Object Detection on Edge Hardware', 'COMPLETED', 'Returned on time without issues')
ON DUPLICATE KEY UPDATE purpose=VALUES(purpose);

-- Maintenance Records
INSERT INTO maintenance_records (record_id, equipment_id, maintenance_type, start_date, completion_date, technician_name, cost, status, notes) VALUES
(1, 11, 'REPAIR', '2026-11-10', NULL, 'Mr. Rajesh Kumar', 1450.00, 'IN_PROGRESS', 'Ordered original OEM lithium battery pack from FLIR authorized distributor'),
(2, 12, 'CALIBRATION', '2026-11-12', NULL, 'M/s Precision Tech Instruments', 850.00, 'SCHEDULED', 'Scheduled annual calibration and frequency drift audit'),
(3, 1, 'PREVENTIVE', '2026-10-15', '2026-10-15', 'Mr. Rajesh Kumar', 320.00, 'COMPLETED', 'Cleaned dust filters, checked AIO liquid cooler flow rates, updated CUDA drivers to 12.6'),
(4, 2, 'CALIBRATION', '2026-08-20', '2026-08-21', 'National Metrology Services', 1200.00, 'COMPLETED', '4-channel frequency response calibration certificate issued')
ON DUPLICATE KEY UPDATE notes=VALUES(notes);

-- Audit Logs
INSERT INTO audit_logs (log_id, user_id, action, entity, entity_id, details) VALUES
(1, 1, 'SYSTEM_STARTUP', 'SYSTEM', 0, 'Lab Equipment Booking & Maintenance System initialized'),
(2, 1, 'EQUIPMENT_REGISTERED', 'EQUIPMENT', 1, 'Added NVIDIA RTX 4090 Deep Learning Rig (EQ-HPC-01)'),
(3, 5, 'BOOKING_CREATED', 'BOOKING', 1, 'Booking requested for Cisco Catalyst 2960-X Switch'),
(4, 1, 'BOOKING_APPROVED', 'BOOKING', 1, 'Booking approved for student Ayush Kumar'),
(5, 4, 'MAINTENANCE_LOGGED', 'MAINTENANCE', 1, 'Maintenance opened for FLIR E4 Thermal Camera');
