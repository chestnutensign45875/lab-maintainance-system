package com.poornima.lab.dao;

import com.poornima.lab.config.DatabaseConfig;
import com.poornima.lab.model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public boolean create(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (equipment_id, user_id, start_time, end_time, purpose, status, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getEquipmentId());
            stmt.setInt(2, booking.getUserId());
            stmt.setTimestamp(3, booking.getStartTime());
            stmt.setTimestamp(4, booking.getEndTime());
            stmt.setString(5, booking.getPurpose());
            stmt.setString(6, booking.getStatus() != null ? booking.getStatus() : "PENDING");
            stmt.setString(7, booking.getRemarks());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        booking.setBookingId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateStatus(int bookingId, String status, String remarks) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, remarks = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, remarks);
            stmt.setInt(3, bookingId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT b.*, e.name AS equipment_name, e.asset_tag, u.full_name AS user_name, u.role AS user_role " +
                     "FROM bookings b " +
                     "JOIN equipment e ON b.equipment_id = e.equipment_id " +
                     "JOIN users u ON b.user_id = u.user_id " +
                     "WHERE b.booking_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBooking(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, e.name AS equipment_name, e.asset_tag, u.full_name AS user_name, u.role AS user_role " +
                     "FROM bookings b " +
                     "JOIN equipment e ON b.equipment_id = e.equipment_id " +
                     "JOIN users u ON b.user_id = u.user_id " +
                     "ORDER BY b.booking_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToBooking(rs));
            }
        }
        return list;
    }

    public List<Booking> getBookingsByUser(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, e.name AS equipment_name, e.asset_tag, u.full_name AS user_name, u.role AS user_role " +
                     "FROM bookings b " +
                     "JOIN equipment e ON b.equipment_id = e.equipment_id " +
                     "JOIN users u ON b.user_id = u.user_id " +
                     "WHERE b.user_id = ? " +
                     "ORDER BY b.booking_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBooking(rs));
                }
            }
        }
        return list;
    }

    public boolean checkConflict(int equipmentId, Timestamp start, Timestamp end, Integer excludeBookingId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM bookings WHERE equipment_id = ? " +
            "AND status IN ('PENDING', 'APPROVED', 'ACTIVE') " +
            "AND NOT (end_time <= ? OR start_time >= ?) "
        );
        if (excludeBookingId != null && excludeBookingId > 0) {
            sql.append("AND booking_id <> ? ");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setInt(1, equipmentId);
            stmt.setTimestamp(2, start);
            stmt.setTimestamp(3, end);
            if (excludeBookingId != null && excludeBookingId > 0) {
                stmt.setInt(4, excludeBookingId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int getActiveBookingCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE status IN ('APPROVED', 'ACTIVE')";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Booking> searchBookings(String keyword, String status) throws SQLException {
        List<Booking> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT b.*, e.name AS equipment_name, e.asset_tag, u.full_name AS user_name, u.role AS user_role " +
            "FROM bookings b " +
            "JOIN equipment e ON b.equipment_id = e.equipment_id " +
            "JOIN users u ON b.user_id = u.user_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(e.asset_tag) LIKE ? OR LOWER(e.name) LIKE ? OR LOWER(u.full_name) LIKE ? OR LOWER(b.purpose) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND UPPER(b.status) = ? ");
            params.add(status.trim().toUpperCase());
        }

        sql.append("ORDER BY b.booking_id DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBooking(rs));
                }
            }
        }
        return list;
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setEquipmentId(rs.getInt("equipment_id"));
        b.setEquipmentName(rs.getString("equipment_name"));
        b.setAssetTag(rs.getString("asset_tag"));
        b.setUserId(rs.getInt("user_id"));
        b.setUserName(rs.getString("user_name"));
        b.setUserRole(rs.getString("user_role"));
        b.setStartTime(com.poornima.lab.util.DateUtil.getSafeTimestamp(rs, "start_time"));
        b.setEndTime(com.poornima.lab.util.DateUtil.getSafeTimestamp(rs, "end_time"));
        b.setPurpose(rs.getString("purpose"));
        b.setStatus(rs.getString("status"));
        b.setRemarks(rs.getString("remarks"));
        b.setBookedAt(com.poornima.lab.util.DateUtil.getSafeTimestamp(rs, "booked_at"));
        return b;
    }
}
