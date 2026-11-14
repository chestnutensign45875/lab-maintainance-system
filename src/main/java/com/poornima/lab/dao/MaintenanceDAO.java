package com.poornima.lab.dao;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.model.MaintenanceRecord;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    public boolean create(MaintenanceRecord record) throws SQLException {
        String sql = "INSERT INTO maintenance_records (equipment_id, maintenance_type, start_date, completion_date, technician_name, cost, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, record.getEquipmentId());
            stmt.setString(2, record.getMaintenanceType());
            stmt.setDate(3, record.getStartDate());
            stmt.setDate(4, record.getCompletionDate());
            stmt.setString(5, record.getTechnicianName());
            stmt.setBigDecimal(6, record.getCost() != null ? record.getCost() : BigDecimal.ZERO);
            stmt.setString(7, record.getStatus() != null ? record.getStatus() : "IN_PROGRESS");
            stmt.setString(8, record.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        record.setRecordId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(MaintenanceRecord record) throws SQLException {
        String sql = "UPDATE maintenance_records SET equipment_id = ?, maintenance_type = ?, start_date = ?, " +
                     "completion_date = ?, technician_name = ?, cost = ?, status = ?, notes = ? WHERE record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getEquipmentId());
            stmt.setString(2, record.getMaintenanceType());
            stmt.setDate(3, record.getStartDate());
            stmt.setDate(4, record.getCompletionDate());
            stmt.setString(5, record.getTechnicianName());
            stmt.setBigDecimal(6, record.getCost());
            stmt.setString(7, record.getStatus());
            stmt.setString(8, record.getNotes());
            stmt.setInt(9, record.getRecordId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean completeMaintenance(int recordId, Date completionDate, String notes) throws SQLException {
        String sql = "UPDATE maintenance_records SET completion_date = ?, status = 'COMPLETED', notes = ? WHERE record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, completionDate != null ? completionDate : new Date(System.currentTimeMillis()));
            stmt.setString(2, notes);
            stmt.setInt(3, recordId);
            return stmt.executeUpdate() > 0;
        }
    }

    public MaintenanceRecord findById(int recordId) throws SQLException {
        String sql = "SELECT m.*, e.name AS equipment_name, e.asset_tag FROM maintenance_records m " +
                     "JOIN equipment e ON m.equipment_id = e.equipment_id WHERE m.record_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRecord(rs);
                }
            }
        }
        return null;
    }

    public List<MaintenanceRecord> getAllRecords() throws SQLException {
        List<MaintenanceRecord> list = new ArrayList<>();
        String sql = "SELECT m.*, e.name AS equipment_name, e.asset_tag FROM maintenance_records m " +
                     "JOIN equipment e ON m.equipment_id = e.equipment_id ORDER BY m.record_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToRecord(rs));
            }
        }
        return list;
    }

    public List<MaintenanceRecord> getRecordsByEquipment(int equipmentId) throws SQLException {
        List<MaintenanceRecord> list = new ArrayList<>();
        String sql = "SELECT m.*, e.name AS equipment_name, e.asset_tag FROM maintenance_records m " +
                     "JOIN equipment e ON m.equipment_id = e.equipment_id WHERE m.equipment_id = ? " +
                     "ORDER BY m.record_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
        }
        return list;
    }

    public int getActiveMaintenanceCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM maintenance_records WHERE status IN ('SCHEDULED', 'IN_PROGRESS')";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public BigDecimal getTotalCost() throws SQLException {
        String sql = "SELECT SUM(cost) FROM maintenance_records";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                BigDecimal sum = rs.getBigDecimal(1);
                return sum != null ? sum : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    public List<MaintenanceRecord> searchRecords(String keyword, String status) throws SQLException {
        List<MaintenanceRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT m.*, e.name AS equipment_name, e.asset_tag FROM maintenance_records m " +
            "JOIN equipment e ON m.equipment_id = e.equipment_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(e.asset_tag) LIKE ? OR LOWER(e.name) LIKE ? OR LOWER(m.technician_name) LIKE ? OR LOWER(m.maintenance_type) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND UPPER(m.status) = ? ");
            params.add(status.trim().toUpperCase());
        }

        sql.append("ORDER BY m.record_id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
        }
        return list;
    }

    private MaintenanceRecord mapRowToRecord(ResultSet rs) throws SQLException {
        MaintenanceRecord r = new MaintenanceRecord();
        r.setRecordId(rs.getInt("record_id"));
        r.setEquipmentId(rs.getInt("equipment_id"));
        r.setEquipmentName(rs.getString("equipment_name"));
        r.setAssetTag(rs.getString("asset_tag"));
        r.setMaintenanceType(rs.getString("maintenance_type"));
        r.setStartDate(com.poornima.lab.util.DateUtil.getSafeDate(rs, "start_date"));
        r.setCompletionDate(com.poornima.lab.util.DateUtil.getSafeDate(rs, "completion_date"));
        r.setTechnicianName(rs.getString("technician_name"));
        r.setCost(rs.getBigDecimal("cost"));
        r.setStatus(rs.getString("status"));
        r.setNotes(rs.getString("notes"));
        r.setCreatedAt(com.poornima.lab.util.DateUtil.getSafeTimestamp(rs, "created_at"));
        return r;
    }
}
