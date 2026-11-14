package com.poornima.lab.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class MaintenanceRecord {
    private int recordId;
    private int equipmentId;
    private String equipmentName;
    private String assetTag;
    private String maintenanceType; // PREVENTIVE, CALIBRATION, REPAIR, INSPECTION
    private Date startDate;
    private Date completionDate;
    private String technicianName;
    private BigDecimal cost;
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    private String notes;
    private Timestamp createdAt;

    public MaintenanceRecord() {}

    public MaintenanceRecord(int recordId, int equipmentId, String maintenanceType, 
                             Date startDate, Date completionDate, String technicianName, 
                             BigDecimal cost, String status, String notes) {
        this.recordId = recordId;
        this.equipmentId = equipmentId;
        this.maintenanceType = maintenanceType;
        this.startDate = startDate;
        this.completionDate = completionDate;
        this.technicianName = technicianName;
        this.cost = cost;
        this.status = status;
        this.notes = notes;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getEquipmentId() { return equipmentId; }
    public void setEquipmentId(int equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }

    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Maintenance #" + recordId + " [" + assetTag + "] (" + maintenanceType + " - " + status + ")";
    }
}
