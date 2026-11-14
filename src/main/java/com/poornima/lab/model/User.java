package com.poornima.lab.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role; // ADMIN, FACULTY, STUDENT, TECHNICIAN
    private String department;
    private String phone;
    private String status; // ACTIVE, INACTIVE, BLOCKED
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String username, String passwordHash, String fullName, 
                String email, String role, String department, String phone, String status) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.department = department;
        this.phone = phone;
        this.status = status;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isFaculty() {
        return "FACULTY".equalsIgnoreCase(role);
    }

    public boolean isTechnician() {
        return "TECHNICIAN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
