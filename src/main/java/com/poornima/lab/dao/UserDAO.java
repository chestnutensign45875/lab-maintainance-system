package com.poornima.lab.dao;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.model.User;
import com.poornima.lab.util.SecurityUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, role, department, phone, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, SecurityUtil.hashPassword(user.getPasswordHash()));
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole());
            stmt.setString(6, user.getDepartment());
            stmt.setString(7, user.getPhone());
            stmt.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, department = ?, phone = ?, status = ? " +
                     "WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getDepartment());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getStatus());
            stmt.setInt(7, user.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    public User authenticate(String username, String rawPassword) throws SQLException {
        User user = findByUsername(username);
        if (user != null && "ACTIVE".equalsIgnoreCase(user.getStatus())) {
            if (SecurityUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
                return user;
            }
        }
        return null;
    }

    public boolean updatePassword(int userId, String rawNewPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, SecurityUtil.hashPassword(rawNewPassword));
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        }
        return list;
    }

    public List<User> searchUsers(String keyword) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE LOWER(username) LIKE ? OR LOWER(full_name) LIKE ? " +
                     "OR LOWER(email) LIKE ? OR LOWER(department) LIKE ? ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
        }
        return list;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setDepartment(rs.getString("department"));
        u.setPhone(rs.getString("phone"));
        u.setStatus(rs.getString("status"));
        u.setCreatedAt(com.poornima.lab.util.DateUtil.getSafeTimestamp(rs, "created_at"));
        return u;
    }
}
