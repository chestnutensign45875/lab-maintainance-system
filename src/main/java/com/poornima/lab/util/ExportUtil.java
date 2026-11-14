package com.poornima.lab.util;

import com.poornima.lab.model.Booking;
import com.poornima.lab.model.Equipment;
import com.poornima.lab.model.MaintenanceRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExportUtil {

    public static void exportEquipmentToCSV(List<Equipment> list, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("Equipment ID,Asset Tag,Name,Category,Model,Serial Number,Lab Room,Status,Purchase Date,Condition Notes");
            for (Equipment e : list) {
                writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        e.getEquipmentId(),
                        escape(e.getAssetTag()),
                        escape(e.getName()),
                        escape(e.getCategoryName()),
                        escape(e.getModel()),
                        escape(e.getSerialNumber()),
                        escape(e.getLabRoom()),
                        escape(e.getStatus()),
                        e.getPurchaseDate() != null ? e.getPurchaseDate().toString() : "",
                        escape(e.getConditionNotes())
                );
            }
        }
    }

    public static void exportBookingsToCSV(List<Booking> list, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("Booking ID,Asset Tag,Equipment Name,User Name,Role,Start Time,End Time,Status,Purpose,Remarks");
            for (Booking b : list) {
                writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        b.getBookingId(),
                        escape(b.getAssetTag()),
                        escape(b.getEquipmentName()),
                        escape(b.getUserName()),
                        escape(b.getUserRole()),
                        DateUtil.formatDateTime(b.getStartTime()),
                        DateUtil.formatDateTime(b.getEndTime()),
                        escape(b.getStatus()),
                        escape(b.getPurpose()),
                        escape(b.getRemarks())
                );
            }
        }
    }

    public static void exportMaintenanceToCSV(List<MaintenanceRecord> list, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("Record ID,Asset Tag,Equipment Name,Type,Start Date,Completion Date,Technician,Cost (INR),Status,Notes");
            for (MaintenanceRecord r : list) {
                writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.2f\",\"%s\",\"%s\"%n",
                        r.getRecordId(),
                        escape(r.getAssetTag()),
                        escape(r.getEquipmentName()),
                        escape(r.getMaintenanceType()),
                        r.getStartDate() != null ? r.getStartDate().toString() : "",
                        r.getCompletionDate() != null ? r.getCompletionDate().toString() : "N/A",
                        escape(r.getTechnicianName()),
                        r.getCost() != null ? r.getCost().doubleValue() : 0.0,
                        escape(r.getStatus()),
                        escape(r.getNotes())
                );
            }
        }
    }

    public static String generateComprehensiveReport(List<Equipment> equipmentList, 
                                                      List<Booking> bookingList, 
                                                      List<MaintenanceRecord> maintenanceList) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sb.append("========================================================================================\n");
        sb.append("          LAB EQUIPMENT BOOKING AND MAINTENANCE SYSTEM - SUMMARY AUDIT REPORT          \n");
        sb.append("          Poornima Institute of Engineering and Technology, Jaipur                     \n");
        sb.append("          Generated At: ").append(sdf.format(new Date())).append("\n");
        sb.append("========================================================================================\n\n");

        sb.append("1. EXECUTIVE INVENTORY SUMMARY\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        long totalEq = equipmentList.size();
        long availEq = equipmentList.stream().filter(Equipment::isAvailable).count();
        long bookedEq = equipmentList.stream().filter(e -> "BOOKED".equalsIgnoreCase(e.getStatus())).count();
        long maintEq = equipmentList.stream().filter(e -> "UNDER_MAINTENANCE".equalsIgnoreCase(e.getStatus())).count();

        sb.append(String.format("   * Total Assets Cataloged      : %d units\n", totalEq));
        sb.append(String.format("   * Available for Reservation   : %d units (%.1f%%)\n", availEq, totalEq > 0 ? (availEq * 100.0 / totalEq) : 0));
        sb.append(String.format("   * Currently Checked Out/Booked: %d units\n", bookedEq));
        sb.append(String.format("   * Under Service/Maintenance   : %d units\n\n", maintEq));

        sb.append("2. EQUIPMENT CATALOG OVERVIEW\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-12s | %-32s | %-24s | %-16s | %-12s\n", "Asset Tag", "Equipment Name", "Lab Room", "Category", "Status"));
        sb.append("----------------------------------------------------------------------------------------\n");
        for (Equipment e : equipmentList) {
            String name = e.getName().length() > 30 ? e.getName().substring(0, 29) + "…" : e.getName();
            String cat = e.getCategoryName() != null && e.getCategoryName().length() > 15 ? e.getCategoryName().substring(0, 14) + "…" : (e.getCategoryName() != null ? e.getCategoryName() : "General");
            sb.append(String.format("%-12s | %-32s | %-24s | %-16s | %-12s\n",
                    e.getAssetTag(), name, e.getLabRoom(), cat, e.getStatus()));
        }
        sb.append("\n");

        sb.append("3. RECENT BOOKINGS\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-6s | %-12s | %-20s | %-18s | %-10s\n", "ID", "Asset Tag", "Reserved By", "Time Window", "Status"));
        sb.append("----------------------------------------------------------------------------------------\n");
        for (int i = 0; i < Math.min(10, bookingList.size()); i++) {
            Booking b = bookingList.get(i);
            String user = b.getUserName() != null ? b.getUserName() : "User " + b.getUserId();
            if (user.length() > 18) user = user.substring(0, 17) + "…";
            String win = DateUtil.formatDateTime(b.getStartTime());
            sb.append(String.format("%-6d | %-12s | %-20s | %-18s | %-10s\n",
                    b.getBookingId(), b.getAssetTag(), user, win, b.getStatus()));
        }
        sb.append("\n");

        sb.append("4. MAINTENANCE LOG AUDIT\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-6s | %-12s | %-14s | %-22s | %-12s | %-10s\n", "ID", "Asset Tag", "Type", "Technician", "Cost (INR)", "Status"));
        sb.append("----------------------------------------------------------------------------------------\n");
        for (MaintenanceRecord r : maintenanceList) {
            sb.append(String.format("%-6d | %-12s | %-14s | %-22s | ₹%-11.2f | %-10s\n",
                    r.getRecordId(), r.getAssetTag(), r.getMaintenanceType(), r.getTechnicianName(),
                    r.getCost() != null ? r.getCost().doubleValue() : 0.0, r.getStatus()));
        }
        sb.append("\n========================================================================================\n");
        sb.append("                            END OF LABORATORY STATUS REPORT                             \n");
        sb.append("========================================================================================\n");

        return sb.toString();
    }

    private static String escape(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
