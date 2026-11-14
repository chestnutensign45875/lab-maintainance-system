package com.poornima.lab.config;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());

    private static String dbType = "UNKNOWN"; // "MYSQL" or "SQLITE"
    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;
    private static boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) return;

        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "db.properties not found, using default configuration");
        }

        String preferredType = props.getProperty("db.type", "AUTO").trim().toUpperCase();
        String mysqlHost = props.getProperty("mysql.host", "localhost");
        String mysqlPort = props.getProperty("mysql.port", "3306");
        String mysqlDb = props.getProperty("mysql.database", "lab_db");
        String mysqlUser = props.getProperty("mysql.user", "root");
        String mysqlPass = props.getProperty("mysql.password", "root");
        String sqlitePath = props.getProperty("sqlite.path", "lab_management.db");

        // Try MySQL first if preferred or AUTO
        if ("MYSQL".equals(preferredType) || "AUTO".equals(preferredType)) {
            String testUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + 
                             "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(testUrl, mysqlUser, mysqlPass)) {
                    jdbcUrl = testUrl;
                    dbUser = mysqlUser;
                    dbPassword = mysqlPass;
                    dbType = "MYSQL";
                    LOGGER.info("Connected successfully to MySQL database at " + mysqlHost + ":" + mysqlPort);
                }
            } catch (Exception e) {
                LOGGER.info("MySQL connection unavailable (" + e.getMessage() + "). Falling back to embedded SQLite.");
            }
        }

        // Fallback to SQLite
        if (!"MYSQL".equals(dbType)) {
            try {
                Class.forName("org.sqlite.JDBC");
                jdbcUrl = "jdbc:sqlite:" + sqlitePath;
                dbUser = "";
                dbPassword = "";
                dbType = "SQLITE";
                LOGGER.info("Using SQLite database at: " + sqlitePath);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found!", e);
                throw new RuntimeException("No suitable JDBC driver found", e);
            }
        }

        initialized = true;
        // Create tables and seed data if needed
        setupSchemaAndSeed();
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        if ("SQLITE".equals(dbType)) {
            Connection conn = DriverManager.getConnection(jdbcUrl);
            try (Statement s = conn.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } else {
            return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        }
    }

    public static String getDbType() {
        if (!initialized) initialize();
        return dbType;
    }

    public static String getJdbcUrl() {
        return jdbcUrl;
    }

    private static void setupSchemaAndSeed() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            boolean isSQLite = "SQLITE".equals(dbType);

            // 1. Users table
            String createUsers = isSQLite ?
                "CREATE TABLE IF NOT EXISTS users (" +
                "  user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT NOT NULL UNIQUE," +
                "  password_hash TEXT NOT NULL," +
                "  full_name TEXT NOT NULL," +
                "  email TEXT NOT NULL UNIQUE," +
                "  role TEXT NOT NULL DEFAULT 'STUDENT'," +
                "  department TEXT NOT NULL," +
                "  phone TEXT," +
                "  status TEXT NOT NULL DEFAULT 'ACTIVE'," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");" :
                "CREATE TABLE IF NOT EXISTS users (" +
                "  user_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  username VARCHAR(50) NOT NULL UNIQUE," +
                "  password_hash VARCHAR(255) NOT NULL," +
                "  full_name VARCHAR(100) NOT NULL," +
                "  email VARCHAR(100) NOT NULL UNIQUE," +
                "  role VARCHAR(30) NOT NULL DEFAULT 'STUDENT'," +
                "  department VARCHAR(100) NOT NULL," +
                "  phone VARCHAR(20)," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
            stmt.execute(createUsers);

            // 2. Categories table
            String createCategories = isSQLite ?
                "CREATE TABLE IF NOT EXISTS categories (" +
                "  category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL UNIQUE," +
                "  description TEXT" +
                ");" :
                "CREATE TABLE IF NOT EXISTS categories (" +
                "  category_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  name VARCHAR(100) NOT NULL UNIQUE," +
                "  description TEXT" +
                ");";
            stmt.execute(createCategories);

            // 3. Equipment table
            String createEquipment = isSQLite ?
                "CREATE TABLE IF NOT EXISTS equipment (" +
                "  equipment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  asset_tag TEXT NOT NULL UNIQUE," +
                "  name TEXT NOT NULL," +
                "  category_id INTEGER NOT NULL," +
                "  model TEXT," +
                "  serial_number TEXT," +
                "  lab_room TEXT NOT NULL," +
                "  status TEXT NOT NULL DEFAULT 'AVAILABLE'," +
                "  purchase_date DATE," +
                "  condition_notes TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE" +
                ");" :
                "CREATE TABLE IF NOT EXISTS equipment (" +
                "  equipment_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  asset_tag VARCHAR(50) NOT NULL UNIQUE," +
                "  name VARCHAR(150) NOT NULL," +
                "  category_id INT NOT NULL," +
                "  model VARCHAR(100)," +
                "  serial_number VARCHAR(100)," +
                "  lab_room VARCHAR(50) NOT NULL," +
                "  status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE'," +
                "  purchase_date DATE," +
                "  condition_notes TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE" +
                ");";
            stmt.execute(createEquipment);

            // 4. Bookings table
            String createBookings = isSQLite ?
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "  booking_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  equipment_id INTEGER NOT NULL," +
                "  user_id INTEGER NOT NULL," +
                "  start_time TIMESTAMP NOT NULL," +
                "  end_time TIMESTAMP NOT NULL," +
                "  purpose TEXT NOT NULL," +
                "  status TEXT NOT NULL DEFAULT 'PENDING'," +
                "  remarks TEXT," +
                "  booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE," +
                "  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ");" :
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "  booking_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  equipment_id INT NOT NULL," +
                "  user_id INT NOT NULL," +
                "  start_time DATETIME NOT NULL," +
                "  end_time DATETIME NOT NULL," +
                "  purpose TEXT NOT NULL," +
                "  status VARCHAR(30) NOT NULL DEFAULT 'PENDING'," +
                "  remarks TEXT," +
                "  booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE," +
                "  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ");";
            stmt.execute(createBookings);

            // 5. Maintenance Records table
            String createMaintenance = isSQLite ?
                "CREATE TABLE IF NOT EXISTS maintenance_records (" +
                "  record_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  equipment_id INTEGER NOT NULL," +
                "  maintenance_type TEXT NOT NULL," +
                "  start_date DATE NOT NULL," +
                "  completion_date DATE," +
                "  technician_name TEXT NOT NULL," +
                "  cost REAL DEFAULT 0.00," +
                "  status TEXT NOT NULL DEFAULT 'IN_PROGRESS'," +
                "  notes TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE" +
                ");" :
                "CREATE TABLE IF NOT EXISTS maintenance_records (" +
                "  record_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  equipment_id INT NOT NULL," +
                "  maintenance_type VARCHAR(50) NOT NULL," +
                "  start_date DATE NOT NULL," +
                "  completion_date DATE," +
                "  technician_name VARCHAR(100) NOT NULL," +
                "  cost DECIMAL(10, 2) DEFAULT 0.00," +
                "  status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS'," +
                "  notes TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE" +
                ");";
            stmt.execute(createMaintenance);

            // 6. Audit Logs table
            String createAudit = isSQLite ?
                "CREATE TABLE IF NOT EXISTS audit_logs (" +
                "  log_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER," +
                "  action TEXT NOT NULL," +
                "  entity TEXT NOT NULL," +
                "  entity_id INTEGER," +
                "  details TEXT," +
                "  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL" +
                ");" :
                "CREATE TABLE IF NOT EXISTS audit_logs (" +
                "  log_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT," +
                "  action VARCHAR(50) NOT NULL," +
                "  entity VARCHAR(50) NOT NULL," +
                "  entity_id INT," +
                "  details TEXT," +
                "  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL" +
                ");";
            stmt.execute(createAudit);

            // Check if categories table is populated
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            if (rs.next() && rs.getInt(1) == 0) {
                seedInitialData(conn, isSQLite);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database tables", e);
        }
    }

    private static void seedInitialData(Connection conn, boolean isSQLite) throws SQLException {
        try (Statement s = conn.createStatement()) {
            // Seed Categories
            s.execute("INSERT INTO categories (category_id, name, description) VALUES " +
                    "(1, 'HPC & AI Workstations', 'High-performance GPU computing clusters and deep learning stations')," +
                    "(2, 'Test & Measurement', 'Oscilloscopes, digital multimeters, spectrum and logic analyzers')," +
                    "(3, 'Networking & Infrastructure', 'Managed switches, routers, firewalls, and rack servers')," +
                    "(4, 'Embedded & IoT Kits', 'Raspberry Pi, NVIDIA Jetson, STM32, ESP32, and Arduino platforms')," +
                    "(5, 'Robotics & Automation', 'Robotic arms, sensor integration testbeds, and motor controllers')," +
                    "(6, 'Rapid Prototyping', 'Precision 3D printers, PCB milling machines, and soldering stations');");

            // Seed Users
            s.execute("INSERT INTO users (user_id, username, password_hash, full_name, email, role, department, phone, status) VALUES " +
                    "(1, 'admin', 'admin123', 'Prof. Anil Gupta', 'admin@poornima.edu.in', 'ADMIN', 'Computer Engineering', '+91 9829012345', 'ACTIVE')," +
                    "(2, 'prof_sharma', 'faculty123', 'Dr. Ramesh Sharma', 'rsharma@poornima.edu.in', 'FACULTY', 'AI & Data Science', '+91 9829054321', 'ACTIVE')," +
                    "(3, 'dr_verma', 'faculty123', 'Dr. Sunita Verma', 'sverma@poornima.edu.in', 'FACULTY', 'Electronics & Comm.', '+91 9829098765', 'ACTIVE')," +
                    "(4, 'tech_rajesh', 'tech123', 'Mr. Rajesh Kumar', 'rkumar@poornima.edu.in', 'TECHNICIAN', 'Central Lab Facilities', '+91 9414011223', 'ACTIVE')," +
                    "(5, 'ayush_kumar', 'student123', 'Ayush Kumar', 'ayush.kumar@poornima.edu.in', 'STUDENT', 'Computer Engineering', '+91 9123456780', 'ACTIVE')," +
                    "(6, 'rohit_meena', 'student123', 'Rohit Meena', 'rohit.meena@poornima.edu.in', 'STUDENT', 'Information Technology', '+91 9876543210', 'ACTIVE');");

            // Seed Equipment
            s.execute("INSERT INTO equipment (equipment_id, asset_tag, name, category_id, model, serial_number, lab_room, status, purchase_date, condition_notes) VALUES " +
                    "(1, 'EQ-HPC-01', 'NVIDIA RTX 4090 Deep Learning Rig', 1, 'Dual RTX 4090 24GB / i9-14900K / 64GB DDR5', 'SN-NV4090-8812', 'Lab 301 - AI & Vision Lab', 'AVAILABLE', '2025-08-10', 'Excellent condition, liquid cooling verified')," +
                    "(2, 'EQ-TM-02', 'Rigol DS1054Z 50MHz Digital Oscilloscope', 2, 'DS1054Z 4-Channel 1GSa/s', 'SN-RGL-554109', 'Lab 204 - Embedded Systems', 'AVAILABLE', '2024-03-15', 'Calibrated until Dec 2026')," +
                    "(3, 'EQ-NET-03', 'Cisco Catalyst 2960-X Gigabit Switch', 3, 'WS-C2960X-24TS-L 24 Ports PoE+', 'SN-CSCO-992144', 'Lab 102 - Advanced Networks', 'BOOKED', '2023-11-20', 'All gigabit ports operational')," +
                    "(4, 'EQ-TM-04', 'Keysight 34461A 6.5 Digit Truevolt DMM', 2, '34461A Truevolt Digital Multimeter', 'SN-KEYS-33211', 'Lab 204 - Embedded Systems', 'AVAILABLE', '2024-06-18', 'Certified precision tolerance')," +
                    "(5, 'EQ-IOT-05', 'NVIDIA Jetson AGX Orin 64GB Dev Kit', 4, 'AGX Orin 275 TOPS AI', 'SN-JT-774412', 'Lab 301 - AI & Vision Lab', 'AVAILABLE', '2025-01-12', 'JetPack 6.0 installed with TensorRT')," +
                    "(6, 'EQ-IOT-06', 'Raspberry Pi 5 Edge AI Compute Cluster', 4, 'Quad RPi 5 8GB Nodes with PCIe NVMe Hat', 'SN-RPI-NODE-04', 'Lab 302 - IoT Innovations', 'BOOKED', '2025-02-01', 'Docker Swarm cluster active')," +
                    "(7, 'EQ-TM-07', 'Rohde & Schwarz Spectrum Analyzer 3GHz', 2, 'FPC1500 5kHz - 3GHz Tracking Gen', 'SN-RS-100293', 'Lab 206 - RF & Microwave', 'AVAILABLE', '2024-09-05', 'RF probe kit complete in flight case')," +
                    "(8, 'EQ-ROB-08', 'DOBOT Magician Educational Robotic Arm', 5, 'Magician v2 4-DOF with Pneumatic Gripper', 'SN-DOB-44321', 'Lab 108 - Robotics Lab', 'AVAILABLE', '2024-10-14', 'Vacuum pump and gripper calibrated')," +
                    "(9, 'EQ-RP-09', 'Creality Ender-3 V3 KE High-Speed 3D Printer', 6, 'Ender-3 V3 KE 500mm/s CoreXZ', 'SN-CR-908122', 'Lab 108 - Rapid Prototyping', 'AVAILABLE', '2025-03-01', 'PEI build plate, direct drive extruder')," +
                    "(10, 'EQ-TM-10', 'Saleae Logic Pro 16 Logic Analyzer', 2, 'Logic Pro 16 USB 3.0 500MS/s', 'SN-SAL-88201', 'Lab 205 - Signal Processing', 'AVAILABLE', '2024-05-11', 'Includes 16 micro-grabber clips')," +
                    "(11, 'EQ-TM-11', 'FLIR E4 Infrared Thermal Imaging Camera', 2, 'E4 WiFi MSX Enhancement 80x60', 'SN-FLIR-09124', 'Lab 204 - Embedded Systems', 'UNDER_MAINTENANCE', '2023-12-01', 'Battery pack replacement scheduled')," +
                    "(12, 'EQ-DSP-12', 'Texas Instruments TMS320C6748 DSP Kit', 4, 'LCDK-C6748 Floating Point DSP Starter', 'SN-TI-6748-09', 'Lab 205 - Signal Processing', 'UNDER_MAINTENANCE', '2023-09-10', 'Firmware JTAG flash in progress');");

            // Seed Bookings
            s.execute("INSERT INTO bookings (booking_id, equipment_id, user_id, start_time, end_time, purpose, status, remarks) VALUES " +
                    "(1, 3, 5, '2026-11-14 09:00:00', '2026-11-14 13:00:00', 'VLAN Configuration and Network Security Lab Practical', 'ACTIVE', 'Issued to Ayush Kumar with patch cables')," +
                    "(2, 6, 2, '2026-11-14 14:00:00', '2026-11-14 18:00:00', 'Distributed Edge AI Benchmarking for M.Tech Thesis', 'ACTIVE', 'Allocated 4 nodes cluster for model evaluation')," +
                    "(3, 1, 5, '2026-11-15 10:00:00', '2026-11-15 16:00:00', 'Deep Neural Network Training for Final Year Capstone', 'APPROVED', 'Scheduled for GPU cluster room 301')," +
                    "(4, 2, 6, '2026-11-13 11:00:00', '2026-11-13 14:00:00', 'Pulse Width Modulation Signal Analysis for Power Inverter', 'COMPLETED', 'Returned in perfect condition and verified')," +
                    "(5, 5, 2, '2026-11-12 10:00:00', '2026-11-12 15:00:00', 'YOLOv10 Real-Time Object Detection on Edge Hardware', 'COMPLETED', 'Returned on time without issues');");

            // Seed Maintenance Records
            s.execute("INSERT INTO maintenance_records (record_id, equipment_id, maintenance_type, start_date, completion_date, technician_name, cost, status, notes) VALUES " +
                    "(1, 11, 'REPAIR', '2026-11-10', NULL, 'Mr. Rajesh Kumar', 1450.00, 'IN_PROGRESS', 'Ordered original OEM lithium battery pack from FLIR authorized distributor')," +
                    "(2, 12, 'CALIBRATION', '2026-11-12', NULL, 'M/s Precision Tech Instruments', 850.00, 'SCHEDULED', 'Scheduled annual calibration and frequency drift audit')," +
                    "(3, 1, 'PREVENTIVE', '2026-10-15', '2026-10-15', 'Mr. Rajesh Kumar', 320.00, 'COMPLETED', 'Cleaned dust filters, checked AIO liquid cooler flow rates, updated CUDA drivers to 12.6')," +
                    "(4, 2, 'CALIBRATION', '2026-08-20', '2026-08-21', 'National Metrology Services', 1200.00, 'COMPLETED', '4-channel frequency response calibration certificate issued');");

            // Seed Audit Logs
            s.execute("INSERT INTO audit_logs (log_id, user_id, action, entity, entity_id, details) VALUES " +
                    "(1, 1, 'SYSTEM_STARTUP', 'SYSTEM', 0, 'Lab Equipment Booking & Maintenance System initialized')," +
                    "(2, 1, 'EQUIPMENT_REGISTERED', 'EQUIPMENT', 1, 'Added NVIDIA RTX 4090 Deep Learning Rig (EQ-HPC-01)')," +
                    "(3, 5, 'BOOKING_CREATED', 'BOOKING', 1, 'Booking requested for Cisco Catalyst 2960-X Switch')," +
                    "(4, 1, 'BOOKING_APPROVED', 'BOOKING', 1, 'Booking approved for student Ayush Kumar')," +
                    "(5, 4, 'MAINTENANCE_LOGGED', 'MAINTENANCE', 1, 'Maintenance opened for FLIR E4 Thermal Camera');");
        }
    }
}
