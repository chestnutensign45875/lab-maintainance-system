package com.poornima.lab.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class DiagramGenerator {

    public static void main(String[] args) {
        File outDir = new File("screenshots");
        if (!outDir.exists()) outDir.mkdirs();

        generateUseCaseDiagram(new File(outDir, "figure_3_1_use_case_diagram.png"));
        generateFlowchart(new File(outDir, "figure_3_2_flowchart.png"));
        System.out.println("Diagrams generated successfully.");
    }

    public static void generateUseCaseDiagram(File outFile) {
        int width = 1100;
        int height = 750;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        // System Boundary Box
        int boxX = 220;
        int boxY = 60;
        int boxW = 660;
        int boxH = 640;

        g2.setColor(new Color(248, 250, 252));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        g2.setColor(new Color(79, 70, 229));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 16, 16);

        // Title
        g2.setFont(new Font("Times New Roman", Font.BOLD, 18));
        g2.drawString("Lab Equipment Booking & Maintenance System", boxX + 130, boxY + 35);

        // Use Case Ovals (x, y, w, h, label)
        Object[][] useCases = {
                {420, 120, 260, 42, "Authenticate & Manage Profile"},
                {420, 175, 260, 42, "Browse & Search Equipment"},
                {420, 230, 260, 42, "Reserve / Book Equipment"},
                {420, 285, 260, 42, "Validate Conflict & Slots <<include>>"},
                {420, 340, 260, 42, "Approve / Reject Bookings"},
                {420, 395, 260, 42, "Check-in & Return Equipment"},
                {420, 450, 260, 42, "Log Maintenance & Repairs"},
                {420, 505, 260, 42, "Update Equipment Status <<extend>>"},
                {420, 560, 260, 42, "Manage User Accounts & Roles"},
                {420, 615, 260, 42, "Export Reports & CSV Audit Logs"}
        };

        // Draw Use Cases
        g2.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        for (Object[] uc : useCases) {
            int ux = (int) uc[0];
            int uy = (int) uc[1];
            int uw = (int) uc[2];
            int uh = (int) uc[3];
            String label = (String) uc[4];

            g2.setColor(new Color(238, 242, 255));
            g2.fillOval(ux, uy, uw, uh);
            g2.setColor(new Color(99, 102, 241));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(ux, uy, uw, uh);

            g2.setColor(new Color(15, 23, 42));
            FontMetrics fm = g2.getFontMetrics();
            int tx = ux + (uw - fm.stringWidth(label)) / 2;
            int ty = uy + (uh - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(label, tx, ty);
        }

        // Draw Actors:
        // Left Actors: Student (x=90, y=200), Faculty (x=90, y=360)
        // Right Actors: Admin / Lab In-Charge (x=980, y=220), Lab Assistant / Technician (x=980, y=480)
        drawActor(g2, 90, 210, "Student");
        drawActor(g2, 90, 390, "Faculty");
        drawActor(g2, 980, 240, "Admin / In-Charge");
        drawActor(g2, 980, 500, "Technician");

        // Association lines
        g2.setColor(new Color(100, 116, 139));
        g2.setStroke(new BasicStroke(1.2f));

        // Student connections
        connect(g2, 120, 220, 420, 141);
        connect(g2, 120, 220, 420, 196);
        connect(g2, 120, 220, 420, 251);
        connect(g2, 120, 220, 420, 416);

        // Faculty connections
        connect(g2, 120, 400, 420, 141);
        connect(g2, 120, 400, 420, 196);
        connect(g2, 120, 400, 420, 251);
        connect(g2, 120, 400, 420, 361);
        connect(g2, 120, 400, 420, 416);

        // Admin connections
        connect(g2, 950, 250, 680, 141);
        connect(g2, 950, 250, 680, 196);
        connect(g2, 950, 250, 680, 361);
        connect(g2, 950, 250, 680, 471);
        connect(g2, 950, 250, 680, 581);
        connect(g2, 950, 250, 680, 636);

        // Technician connections
        connect(g2, 950, 510, 680, 196);
        connect(g2, 950, 510, 680, 416);
        connect(g2, 950, 510, 680, 471);
        connect(g2, 950, 510, 680, 526);
        connect(g2, 950, 510, 680, 636);

        // Dependency arrow between UC 3 and UC 4 (include)
        drawDashedArrow(g2, 550, 272, 550, 285);
        // Dependency arrow between UC 7 and UC 8 (extend)
        drawDashedArrow(g2, 550, 492, 550, 505);

        g2.dispose();
        try {
            ImageIO.write(img, "png", outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateFlowchart(File outFile) {
        int width = 900;
        int height = 800;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        g2.setFont(new Font("Times New Roman", Font.BOLD, 18));
        g2.setColor(new Color(15, 23, 42));
        g2.drawString("System Architectural Workflow & Booking Lifecycle", 220, 40);

        int cx = width / 2;

        // Flowchart steps
        drawFlowPill(g2, cx, 80, 220, 38, "Start / Launch Application", new Color(16, 185, 129));
        drawArrow(g2, cx, 99, cx, 130);

        drawFlowRect(g2, cx, 150, 260, 40, "User Login & Role Authentication", new Color(79, 70, 229));
        drawArrow(g2, cx, 170, cx, 205);

        drawFlowDiamond(g2, cx, 240, 180, 70, "Valid Role & Account?", new Color(245, 158, 11));
        drawArrow(g2, cx, 275, cx, 315);
        g2.setFont(new Font("Times New Roman", Font.BOLD, 11));
        g2.drawString("Yes", cx + 8, 295);

        // Invalid branch
        drawArrow(g2, cx - 90, 240, cx - 180, 240);
        g2.drawString("No", cx - 140, 232);
        drawFlowRect(g2, cx - 260, 240, 160, 36, "Show Error / Register", new Color(239, 68, 68));
        // loop back to login
        drawArrow(g2, cx - 260, 222, cx - 260, 150);
        drawArrow(g2, cx - 260, 150, cx - 130, 150);

        drawFlowRect(g2, cx, 335, 280, 40, "Open Dashboard & Load Equipment DB", new Color(79, 70, 229));
        drawArrow(g2, cx, 355, cx, 395);

        drawFlowRect(g2, cx, 415, 280, 40, "User Selects Equipment & Time Slot", new Color(79, 70, 229));
        drawArrow(g2, cx, 435, cx, 475);

        drawFlowDiamond(g2, cx, 510, 180, 70, "Slot Available / No Conflict?", new Color(245, 158, 11));
        drawArrow(g2, cx, 545, cx, 585);
        g2.drawString("Yes", cx + 8, 565);

        // Conflict branch
        drawArrow(g2, cx + 90, 510, cx + 180, 510);
        g2.drawString("Conflict", cx + 105, 502);
        drawFlowRect(g2, cx + 260, 510, 160, 36, "Notify Conflict Alert", new Color(239, 68, 68));
        drawArrow(g2, cx + 260, 492, cx + 260, 415);
        drawArrow(g2, cx + 260, 415, cx + 140, 415);

        drawFlowRect(g2, cx, 605, 280, 40, "Commit Reservation & Update Status", new Color(16, 185, 129));
        drawArrow(g2, cx, 625, cx, 665);

        drawFlowRect(g2, cx, 685, 280, 40, "Return Verification & Maintenance Logging", new Color(79, 70, 229));
        drawArrow(g2, cx, 705, cx, 745);

        drawFlowPill(g2, cx, 765, 200, 38, "Audit Log & Complete", new Color(16, 185, 129));

        g2.dispose();
        try {
            ImageIO.write(img, "png", outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void drawActor(Graphics2D g2, int x, int y, String name) {
        g2.setColor(new Color(30, 41, 59));
        g2.setStroke(new BasicStroke(2.2f));

        // Head
        g2.drawOval(x - 12, y - 40, 24, 24);
        // Body
        g2.drawLine(x, y - 16, x, y + 14);
        // Arms
        g2.drawLine(x - 20, y - 4, x + 20, y - 4);
        // Left Leg
        g2.drawLine(x, y + 14, x - 14, y + 36);
        // Right Leg
        g2.drawLine(x, y + 14, x + 14, y + 36);

        // Name
        g2.setFont(new Font("Times New Roman", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x - fm.stringWidth(name) / 2;
        g2.drawString(name, tx, y + 54);
    }

    private static void connect(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.drawLine(x1, y1, x2, y2);
    }

    private static void drawDashedArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        Stroke old = g2.getStroke();
        float[] dash = {4f, 4f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2.setColor(new Color(99, 102, 241));
        g2.drawLine(x1, y1, x2, y2);
        g2.setStroke(old);
        // arrowhead
        g2.drawLine(x2 - 4, y2 - 6, x2, y2);
        g2.drawLine(x2 + 4, y2 - 6, x2, y2);
    }

    private static void drawFlowPill(Graphics2D g2, int cx, int cy, int w, int h, String text, Color c) {
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 25));
        g2.fillRoundRect(cx - w / 2, cy - h / 2, w, h, h, h);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(cx - w / 2, cy - h / 2, w, h, h, h);

        g2.setFont(new Font("Times New Roman", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(15, 23, 42));
        g2.drawString(text, cx - fm.stringWidth(text) / 2, cy + fm.getAscent() / 2 - 2);
    }

    private static void drawFlowRect(Graphics2D g2, int cx, int cy, int w, int h, String text, Color c) {
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 20));
        g2.fillRoundRect(cx - w / 2, cy - h / 2, w, h, 8, 8);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(cx - w / 2, cy - h / 2, w, h, 8, 8);

        g2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(15, 23, 42));
        g2.drawString(text, cx - fm.stringWidth(text) / 2, cy + fm.getAscent() / 2 - 2);
    }

    private static void drawFlowDiamond(Graphics2D g2, int cx, int cy, int w, int h, String text, Color c) {
        Polygon p = new Polygon();
        p.addPoint(cx, cy - h / 2);
        p.addPoint(cx + w / 2, cy);
        p.addPoint(cx, cy + h / 2);
        p.addPoint(cx - w / 2, cy);

        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 25));
        g2.fillPolygon(p);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawPolygon(p);

        g2.setFont(new Font("Times New Roman", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(15, 23, 42));
        g2.drawString(text, cx - fm.stringWidth(text) / 2, cy + fm.getAscent() / 2 - 2);
    }

    private static void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(100, 116, 139));
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawLine(x1, y1, x2, y2);
        if (x1 == x2 && y2 > y1) {
            g2.drawLine(x2 - 4, y2 - 6, x2, y2);
            g2.drawLine(x2 + 4, y2 - 6, x2, y2);
        } else if (y1 == y2 && x2 > x1) {
            g2.drawLine(x2 - 6, y2 - 4, x2, y2);
            g2.drawLine(x2 - 6, y2 + 4, x2, y2);
        } else if (y1 == y2 && x2 < x1) {
            g2.drawLine(x2 + 6, y2 - 4, x2, y2);
            g2.drawLine(x2 + 6, y2 + 4, x2, y2);
        } else if (x1 == x2 && y2 < y1) {
            g2.drawLine(x2 - 4, y2 + 6, x2, y2);
            g2.drawLine(x2 + 4, y2 + 6, x2, y2);
        }
    }
}
