package com.poornima.lab.dao;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public void log(Integer userId, String action, String entity, Integer entityId, String details) {
        String sql = "INSERT INTO audit_logs (user_id, action, entity, entity_id, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, action);
            stmt.setString(3, entity);
            if (entityId != null) {
                stmt.setInt(4, entityId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Log silent or print
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }

    public List<AuditLog> getRecentLogs(int limit) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT a.*, u.username FROM audit_logs a " +
                     "LEFT JOIN users u ON a.user_id = u.user_id " +
                     "ORDER BY a.log_id DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setLogId(rs.getInt("log_id"));
                    int uid = rs.getInt("user_id");
                    log.setUserId(rs.wasNull() ? null : uid);
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setEntity(rs.getString("entity"));
                    int eid = rs.getInt("entity_id");
                    log.setEntityId(rs.wasNull() ? null : eid);
                    log.setDetails(rs.getString("details"));
                    log.setTimestamp(com.poornima.lab.util.DateUtil.getSafeTimestamp(rs, "timestamp"));
                    list.add(log);
                }
            }
        }
        return list;
    }
}
