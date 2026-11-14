package com.poornima.lab.model;

import java.sql.Timestamp;

public class Booking {
    private int bookingId;
    private int equipmentId;
    private String equipmentName;
    private String assetTag;
    private int userId;
    private String userName;
    private String userRole;
    private Timestamp startTime;
    private Timestamp endTime;
    private String purpose;
    private String status; // PENDING, APPROVED, ACTIVE, COMPLETED, CANCELLED, REJECTED
    private String remarks;
    private Timestamp bookedAt;

    public Booking() {}

    public Booking(int bookingId, int equipmentId, int userId, Timestamp startTime, 
                   Timestamp endTime, String purpose, String status, String remarks) {
        this.bookingId = bookingId;
        this.equipmentId = equipmentId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = status;
        this.remarks = remarks;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getBookedAt() { return bookedAt; }
    public void setBookedAt(Timestamp bookedAt) { this.bookedAt = bookedAt; }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " [" + assetTag + "] by " + userName + " (" + status + ")";
    }
}
