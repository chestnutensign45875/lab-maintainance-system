package com.poornima.lab.dao;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.model.Equipment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {

    public boolean create(Equipment eq) throws SQLException {
        String sql = "INSERT INTO equipment (asset_tag, name, category_id, model, serial_number, lab_room, status, purchase_date, condition_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, eq.getAssetTag());
            stmt.setString(2, eq.getName());
            stmt.setInt(3, eq.getCategoryId());
            stmt.setString(4, eq.getModel());
            stmt.setString(5, eq.getSerialNumber());
            stmt.setString(6, eq.getLabRoom());
            stmt.setString(7, eq.getStatus() != null ? eq.getStatus() : "AVAILABLE");
            stmt.setDate(8, eq.getPurchaseDate());
            stmt.setString(9, eq.getConditionNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        eq.setEquipmentId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Equipment eq) throws SQLException {
        String sql = "UPDATE equipment SET asset_tag = ?, name = ?, category_id = ?, model = ?, serial_number = ?, " +
                     "lab_room = ?, status = ?, purchase_date = ?, condition_notes = ? WHERE equipment_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, eq.getAssetTag());
            stmt.setString(2, eq.getName());
            stmt.setInt(3, eq.getCategoryId());
            stmt.setString(4, eq.getModel());
            stmt.setString(5, eq.getSerialNumber());
            stmt.setString(6, eq.getLabRoom());
            stmt.setString(7, eq.getStatus());
            stmt.setDate(8, eq.getPurchaseDate());
            stmt.setString(9, eq.getConditionNotes());
            stmt.setInt(10, eq.getEquipmentId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int equipmentId) throws SQLException {
        String sql = "DELETE FROM equipment WHERE equipment_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Equipment findById(int equipmentId) throws SQLException {
        String sql = "SELECT e.*, c.name AS category_name FROM equipment e " +
                     "LEFT JOIN categories c ON e.category_id = c.category_id " +
                     "WHERE e.equipment_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEquipment(rs);
                }
            }
        }
        return null;
    }

    public Equipment findByAssetTag(String assetTag) throws SQLException {
        String sql = "SELECT e.*, c.name AS category_name FROM equipment e " +
                     "LEFT JOIN categories c ON e.category_id = c.category_id " +
                     "WHERE LOWER(e.asset_tag) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, assetTag);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEquipment(rs);
                }
            }
        }
        return null;
    }

    public List<Equipment> getAllEquipment() throws SQLException {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT e.*, c.name AS category_name FROM equipment e " +
                     "LEFT JOIN categories c ON e.category_id = c.category_id " +
                     "ORDER BY e.equipment_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToEquipment(rs));
            }
        }
        return list;
    }

    public List<Equipment> searchEquipment(String keyword, Integer categoryId, String status, String labRoom) throws SQLException {
        List<Equipment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT e.*, c.name AS category_name FROM equipment e " +
            "LEFT JOIN categories c ON e.category_id = c.category_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(e.asset_tag) LIKE ? OR LOWER(e.name) LIKE ? OR LOWER(e.model) LIKE ? OR LOWER(e.serial_number) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND e.category_id = ? ");
            params.add(categoryId);
        }
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND UPPER(e.status) = ? ");
            params.add(status.trim().toUpperCase());
        }
        if (labRoom != null && !labRoom.trim().isEmpty() && !"ALL".equalsIgnoreCase(labRoom)) {
            sql.append("AND e.lab_room = ? ");
            params.add(labRoom.trim());
        }

        sql.append("ORDER BY e.equipment_id ASC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToEquipment(rs));
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int equipmentId, String status) throws SQLException {
        String sql = "UPDATE equipment SET status = ? WHERE equipment_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, equipmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getTotalCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM equipment");
    }

    public int getAvailableCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM equipment WHERE status = 'AVAILABLE'");
    }

    public int getBookedCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM equipment WHERE status = 'BOOKED'");
    }

    public int getUnderMaintenanceCount() throws SQLException {
        return getCountBySql("SELECT COUNT(*) FROM equipment WHERE status = 'UNDER_MAINTENANCE'");
    }

    public List<String> getAllLabRooms() throws SQLException {
        List<String> rooms = new ArrayList<>();
        String sql = "SELECT DISTINCT lab_room FROM equipment ORDER BY lab_room ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(rs.getString("lab_room"));
            }
        }
        return rooms;
    }

    private int getCountBySql(String sql) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Equipment mapRowToEquipment(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipmentId(rs.getInt("equipment_id"));
        e.setAssetTag(rs.getString("asset_tag"));
        e.setName(rs.getString("name"));
        e.setCategoryId(rs.getInt("category_id"));
        e.setCategoryName(rs.getString("category_name"));
        e.setModel(rs.getString("model"));
        e.setSerialNumber(rs.getString("serial_number"));
        e.setLabRoom(rs.getString("lab_room"));
        e.setStatus(rs.getString("status"));
        e.setPurchaseDate(com.poornima.lab.util.DateUtil.getSafeDate(rs, "purchase_date"));
        e.setConditionNotes(rs.getString("condition_notes"));
        e.setCreatedAt(com.poornima.lab.util.DateUtil.getSafeTimestamp(rs, "created_at"));
        return e;
    }
}
