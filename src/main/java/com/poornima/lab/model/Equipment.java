package com.poornima.lab.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Equipment {
    private int equipmentId;
    private String assetTag;
    private String name;
    private int categoryId;
    private String categoryName; // Helper for display
    private String model;
    private String serialNumber;
    private String labRoom;
    private String status; // AVAILABLE, BOOKED, UNDER_MAINTENANCE, DECOMMISSIONED
    private Date purchaseDate;
    private String conditionNotes;
    private Timestamp createdAt;

    public Equipment() {}

    public Equipment(int equipmentId, String assetTag, String name, int categoryId, 
                     String model, String serialNumber, String labRoom, String status, 
                     Date purchaseDate, String conditionNotes) {
        this.equipmentId = equipmentId;
        this.assetTag = assetTag;
        this.name = name;
        this.categoryId = categoryId;
        this.model = model;
        this.serialNumber = serialNumber;
        this.labRoom = labRoom;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.conditionNotes = conditionNotes;
    }

    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getLabRoom() { return labRoom; }
    public void setLabRoom(String labRoom) { this.labRoom = labRoom; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getConditionNotes() { return conditionNotes; }
    public void setConditionNotes(String conditionNotes) { this.conditionNotes = conditionNotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return assetTag + " - " + name + " (" + status + ")";
    }
}
