#!/usr/bin/env python3
import os
import zipfile
import xml.etree.ElementTree as ET

def build_docx_report():
    template_docx = "NSP Report Format 3rd Semester.docx"
    output_docx = "Lab_Equipment_Booking_and_Maintenance_System_Report.docx"
    screenshots_dir = "screenshots"

    # List of figures to embed: (rel_id, filename, caption, width_emu, height_emu)
    # 1 inch = 914400 EMUs
    # A4 width = 11909 dxa = 8.27 in. Margin = 1 in each side -> printable = 6.27 in = 5,733,288 EMUs.
    # For large screenshots: width ~ 5.8 in = 5,303,520 EMUs. height ~ 3.6 in = 3,291,840 EMUs.
    # For dialogs: width ~ 4.0 in = 3,657,600 EMUs. height ~ 4.2 in = 3,840,480 EMUs.

    figures = [
        ("rId101", "figure_3_1_use_case_diagram.png", "Figure 3.1: Use Case Diagram for Lab Equipment Booking and Maintenance System", 5300000, 3600000),
        ("rId102", "figure_3_2_flowchart.png", "Figure 3.2: System Architectural Workflow & Reservation Lifecycle Flowchart", 5000000, 4400000),
        ("rId103", "figure_5_1_login.png", "Figure 5.1: System Authentication and Role-Based Login Interface", 5200000, 3300000),
        ("rId104", "figure_5_2_registration.png", "Figure 5.2: Student and Faculty Account Registration Portal", 3800000, 4400000),
        ("rId105", "figure_5_3_dashboard.png", "Figure 5.3: Main Laboratory Dashboard with Real-Time KPI Cards & Overview", 5300000, 3300000),
        ("rId106", "figure_5_4_equipment_catalog.png", "Figure 5.4: Equipment Inventory Catalog with Multi-Criteria Live Search & Filters", 5300000, 3300000),
        ("rId107", "figure_5_5_equipment_dialog.png", "Figure 5.5: Laboratory Asset Registration and Technical Specs Dialog", 3900000, 4600000),
        ("rId108", "figure_5_6_bookings.png", "Figure 5.6: Equipment Booking Schedule & Real-Time Reservation Management", 5300000, 3300000),
        ("rId109", "figure_5_7_booking_dialog.png", "Figure 5.7: Equipment Reservation Dialog with Time Slot & Conflict Detection", 3900000, 4200000),
        ("rId110", "figure_5_8_maintenance.png", "Figure 5.8: Maintenance & Corrective Repair Service Logs Overview", 5300000, 3300000),
        ("rId111", "figure_5_9_maintenance_dialog.png", "Figure 5.9: Preventive Maintenance and Service Ticket Sign-Off Dialog", 3900000, 4300000),
        ("rId112", "figure_5_10_users.png", "Figure 5.10: Administrator User Management and Role Allocation Panel", 5300000, 3300000),
        ("rId113", "figure_5_11_reports.png", "Figure 5.11: Executive Audit Report Generation & CSV Dataset Export Dialog", 5100000, 3800000),
        ("rId114", "figure_5_12_change_password.png", "Figure 5.12: User Security & Password Reset Interface", 3800000, 3400000),
    ]

    # Helper XML generators
    def p_cover(text, size=24, bold=True, space_after=120, space_before=0, color="000000", center=True):
        align = '<w:jc w:val="center"/>' if center else '<w:jc w:val="left"/>'
        b_tag = '<w:b/>' if bold else ''
        return f'''<w:p>
            <w:pPr>
                {align}
                <w:spacing w:before="{space_before}" w:after="{space_after}" w:line="240" w:lineRule="auto"/>
            </w:pPr>
            <w:r>
                <w:rPr>
                    <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>
                    <w:color w:val="{color}"/>
                    {b_tag}
                    <w:sz w:val="{size}"/>
                </w:rPr>
                <w:t>{escape(text)}</w:t>
            </w:r>
        </w:p>'''

    def p_chapter_head(text):
        return f'''<w:p>
            <w:pPr>
                <w:pageBreakBefore/>
                <w:jc w:val="center"/>
                <w:spacing w:before="240" w:after="240" w:line="360" w:lineRule="auto"/>
            </w:pPr>
            <w:r>
                <w:rPr>
                    <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>
                    <w:b/>
                    <w:sz w:val="32"/>
                </w:rPr>
                <w:t>{escape(text)}</w:t>
            </w:r>
        </w:p>'''

    def p_sec_head(text):
        return f'''<w:p>
            <w:pPr>
                <w:jc w:val="left"/>
                <w:spacing w:before="200" w:after="120" w:line="360" w:lineRule="auto"/>
            </w:pPr>
            <w:r>
                <w:rPr>
                    <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>
                    <w:b/>
                    <w:sz w:val="28"/>
                </w:rPr>
                <w:t>{escape(text)}</w:t>
            </w:r>
        </w:p>'''

    def p_subsec_head(text):
        return f'''<w:p>
            <w:pPr>
                <w:jc w:val="left"/>
                <w:spacing w:before="160" w:after="100" w:line="360" w:lineRule="auto"/>
            </w:pPr>
            <w:r>
                <w:rPr>
                    <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>
                    <w:b/>
                    <w:sz w:val="26"/>
                </w:rPr>
                <w:t>{escape(text)}</w:t>
            </w:r>
        </w:p>'''

    def p_body(text, indent=False, bullet=False):
        ind = '<w:ind w:firstLine="400"/>' if indent else ''
        blt_prefix = "•  " if bullet else ""
        return f'''<w:p>
            <w:pPr>
                <w:jc w:val="both"/>
                {ind}
                <w:spacing w:before="0" w:after="120" w:line="360" w:lineRule="auto"/>
            </w:pPr>
            <w:r>
                <w:rPr>
                    <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>
                    <w:sz w:val="24"/>
                </w:rPr>
                <w:t xml:space="preserve">{escape(blt_prefix + text)}</w:t>
            </w:r>
        </w:p>'''

    def p_image(rel_id, width_emu, height_emu, pic_id):
        return f'''<w:p>
            <w:pPr>
                <w:jc w:val="center"/>
                <w:spacing w:before="140" w:after="80"/>
            </w:pPr>
            <w:r>
                <w:drawing>
                    <wp:inline distT="0" distB="0" distL="0" distR="0">
                        <wp:extent cx="{width_emu}" cy="{height_emu}"/>
                        <wp:docPr id="{pic_id}" name="Picture {pic_id}"/>
                        <wp:cNvGraphicFramePr>
                            <a:graphicFrameLocks xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" noChangeAspect="1"/>
                        </wp:cNvGraphicFramePr>
                        <a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
                            <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
                                <pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
                                    <pic:nvPicPr>
                                        <pic:cNvPr id="0" name="Picture {pic_id}"/>
                                        <pic:cNvPicPr><a:picLocks noChangeAspect="1" noChangeArrowheads="1"/></pic:cNvPicPr>
                                    </pic:nvPicPr>
                                    <pic:blipFill>
                                        <a:blip r:embed="{rel_id}"/>
                                        <a:stretch><a:fillRect/></a:stretch>
                                    </pic:blipFill>
                                    <pic:spPr bwMode="auto">
                                        <a:xfrm><a:off x="0" y="0"/><a:ext cx="{width_emu}" cy="{height_emu}"/></a:xfrm>
                                        <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                                    </pic:spPr>
                                </pic:pic>
                            </a:graphicData>
                        </a:graphic>
                    </wp:inline>
                </w:drawing>
            </w:r>
        </w:p>'''

    def p_caption(text):
        return f'''<w:p>
            <w:pPr>
                <w:jc w:val="center"/>
                <w:spacing w:before="60" w:after="160" w:line="240" w:lineRule="auto"/>
            </w:pPr>
            <w:r>
                <w:rPr>
                    <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>
                    <w:b/>
                    <w:sz w:val="20"/>
                </w:rPr>
                <w:t>{escape(text)}</w:t>
            </w:r>
        </w:p>'''

    def make_table(headers, rows, col_widths):
        # col_widths in dxa (total width ~ 8800 dxa)
        table_xml = ['<w:tbl>',
                     '<w:tblPr>',
                     '<w:tblW w:w="8800" w:type="dxa"/>',
                     '<w:jc w:val="center"/>',
                     '<w:tblBorders>',
                     '<w:top w:val="single" w:sz="6" w:space="0" w:color="000000"/>',
                     '<w:left w:val="single" w:sz="6" w:space="0" w:color="000000"/>',
                     '<w:bottom w:val="single" w:sz="6" w:space="0" w:color="000000"/>',
                     '<w:right w:val="single" w:sz="6" w:space="0" w:color="000000"/>',
                     '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>',
                     '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>',
                     '</w:tblBorders>',
                     '</w:tblPr>']

        # Header Row
        table_xml.append('<w:tr><w:trPr><w:tblHeader/></w:trPr>')
        for idx, h in enumerate(headers):
            w = col_widths[idx]
            table_xml.append(f'''<w:tc>
                <w:tcPr>
                    <w:tcW w:w="{w}" w:type="dxa"/>
                    <w:shd w:val="clear" w:color="auto" w:fill="EEF2FF"/>
                    <w:tcMar><w:top w:w="120" w:type="dxa"/><w:bottom w:w="120" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
                </w:tcPr>
                <w:p>
                    <w:pPr><w:jc w:val="center"/><w:spacing w:before="40" w:after="40"/></w:pPr>
                    <w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="20"/></w:rPr><w:t>{escape(h)}</w:t></w:r>
                </w:p>
            </w:tc>''')
        table_xml.append('</w:tr>')

        # Data Rows
        for r_idx, row in enumerate(rows):
            fill = "FFFFFF" if r_idx % 2 == 0 else "F8FAFC"
            table_xml.append('<w:tr>')
            for c_idx, cell in enumerate(row):
                w = col_widths[c_idx]
                align = "center" if c_idx == 0 or c_idx == 1 else "left"
                table_xml.append(f'''<w:tc>
                    <w:tcPr>
                        <w:tcW w:w="{w}" w:type="dxa"/>
                        <w:shd w:val="clear" w:color="auto" w:fill="{fill}"/>
                        <w:tcMar><w:top w:w="80" w:type="dxa"/><w:bottom w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>
                    </w:tcPr>
                    <w:p>
                        <w:pPr><w:jc w:val="{align}"/><w:spacing w:before="40" w:after="40"/></w:pPr>
                        <w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="20"/></w:rPr><w:t>{escape(str(cell))}</w:t></w:r>
                    </w:p>
                </w:tc>''')
            table_xml.append('</w:tr>')

        table_xml.append('</w:tbl>')
        return ''.join(table_xml)

    def escape(s):
        return (s.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace('"', "&quot;")
                 .replace("'", "&apos;"))

    # Build the document body
    body_parts = []

    # ==================== COVER PAGE ====================
    body_parts.append(p_cover("SOFTWARE REQUIREMENT SPECIFICATION (SRS)", size=32, bold=True, space_after=100, space_before=200))
    body_parts.append(p_cover("FOR", size=24, bold=True, space_after=100))
    body_parts.append(p_cover("LAB EQUIPMENT BOOKING AND MAINTENANCE SYSTEM", size=30, bold=True, space_after=200, color="4F46E5"))
    body_parts.append(p_cover("MINI PROJECT (NON-SYLLABUS PROJECT)", size=24, bold=True, space_after=240))

    # Embed College Logo (rId8 in template rels)
    body_parts.append(f'''<w:p>
        <w:pPr><w:jc w:val="center"/><w:spacing w:before="120" w:after="240"/></w:pPr>
        <w:r>
            <w:drawing>
                <wp:inline distT="0" distB="0" distL="0" distR="0">
                    <wp:extent cx="1750755" cy="2065020"/>
                    <wp:docPr id="1" name="College Logo"/>
                    <wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" noChangeAspect="1"/></wp:cNvGraphicFramePr>
                    <a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
                        <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
                            <pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
                                <pic:nvPicPr><pic:cNvPr id="0" name="Logo"/><pic:cNvPicPr><a:picLocks noChangeAspect="1"/></pic:cNvPicPr></pic:nvPicPr>
                                <pic:blipFill><a:blip r:embed="rId8"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>
                                <pic:spPr bwMode="auto"><a:xfrm><a:off x="0" y="0"/><a:ext cx="1750755" cy="2065020"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr>
                            </pic:pic>
                        </a:graphicData>
                    </a:graphic>
                </wp:inline>
            </w:drawing>
        </w:r>
    </w:p>''')

    body_parts.append(p_cover("SUBMITTED TO", size=24, bold=True, space_after=40))
    body_parts.append(p_cover("Dr. Rekha Jain / Faculty Coordinator", size=24, bold=False, space_after=20))
    body_parts.append(p_cover("Associate Professor, Department of Computer Engineering", size=22, bold=False, space_after=180))

    body_parts.append(p_cover("SUBMITTED BY", size=24, bold=True, space_after=40))
    body_parts.append(p_cover("Ayush Kumar", size=24, bold=False, space_after=20))
    body_parts.append(p_cover("Roll No: PIET/CE/2026/042 | 3rd Semester", size=22, bold=False, space_after=260))

    body_parts.append(p_cover("DEPARTMENT OF COMPUTER ENGINEERING", size=26, bold=True, space_after=40))
    body_parts.append(p_cover("POORNIMA INSTITUTE OF ENGINEERING & TECHNOLOGY, JAIPUR", size=24, bold=True, space_after=40))
    body_parts.append(p_cover("ACADEMIC YEAR 2026–27", size=22, bold=True, space_after=100))

    # ==================== CHAPTER 1 ====================
    body_parts.append(p_chapter_head("CHAPTER 1: INTRODUCTION OF THE PROJECT"))
    body_parts.append(p_sec_head("1.1 Objective of the Project"))
    body_parts.append(p_body(
        "In modern higher educational institutions, state-of-the-art laboratory facilities housing advanced engineering equipment—such as High-Performance Computing (HPC) GPU workstations, digital storage oscilloscopes, vector network analyzers, embedded IoT development clusters, 3D rapid prototyping systems, and precision testbenches—constitute critical infrastructure for both undergraduate curriculum practicals and advanced faculty research. Historically, the management of these shared laboratory assets has relied upon manual physical logbooks, paper requisitions, or ad-hoc verbal scheduling. This legacy manual model exhibits severe operational deficiencies, including double-booking conflicts, lack of visibility into real-time equipment availability, untracked asset degradation, unscheduled equipment breakdowns, unaccounted calibration drift, and the absence of verifiable audit trails for accreditation bodies (such as NAAC and NBA)."
    ))
    body_parts.append(p_body(
        "The primary objective of developing the Lab Equipment Booking and Maintenance System is to engineer an enterprise-grade, desktop-based management application utilizing Java, Java Swing with modern FlatLaf aesthetics, JDBC connectivity, and relational database management (MySQL and SQLite). The specific objectives achieved by the system include:"
    ))
    body_parts.append(p_body("Digitize and centralize the complete laboratory asset inventory across multiple departmental labs with unique asset tags, technical specifications, and real-time operational status indicators.", bullet=True))
    body_parts.append(p_body("Automate equipment scheduling with algorithmic conflict detection to guarantee zero overlapping bookings and transparent slot allocation for students and faculty.", bullet=True))
    body_parts.append(p_body("Streamline physical equipment check-out and return workflows with inspection sign-offs and automatic status restoration.", bullet=True))
    body_parts.append(p_body("Implement structured preventive and corrective maintenance tracking, recording service diagnosis, calibration cycles, technician allocation, and financial costs.", bullet=True))
    body_parts.append(p_body("Enforce robust Role-Based Access Control (RBAC) ensuring appropriate separation of duties between Administrators, Faculty, Students, and Lab Technicians.", bullet=True))
    body_parts.append(p_body("Provide one-click executive data export to standard CSV spreadsheets and formatted audit reports for departmental asset accountability.", bullet=True))

    body_parts.append(p_sec_head("1.2 Types of Users"))
    body_parts.append(p_body(
        "The system identifies four distinct categories of authorized users, each with tailored operational roles, privileges, and interface views:"
    ))
    body_parts.append(p_body("1. Administrator / Lab In-Charge: Holds ultimate operational authority over the laboratory portal. Responsibilities include registering and modifying hardware asset records, creating categories, managing user accounts, adjusting roles, resetting passwords, reviewing comprehensive audit logs, and generating departmental reports.", bullet=False))
    body_parts.append(p_body("2. Faculty Member / Researcher: Engaged in course instruction, sponsored research projects, and student thesis supervision. Faculty can search equipment across all laboratories, reserve high-end computing or testing resources for scheduled project experiments, approve or endorse student reservation requests, and review historical usage patterns.", bullet=False))
    body_parts.append(p_body("3. Student / Lab Assistant: Undergraduate and postgraduate engineering students who require laboratory equipment for lab coursework, minor/major capstone projects, and club innovations. Students can inspect real-time availability, schedule slots, submit reservation requests with designated academic purposes, monitor booking status, and request cancellations.", bullet=False))
    body_parts.append(p_body("4. Maintenance Technician / Vendor: Technical laboratory staff responsible for equipment upkeep. Technicians can open service tickets, log diagnostic findings, record calibration certificates, input parts replacement costs, and mark completed repairs to return assets to available inventory.", bullet=False))

    body_parts.append(p_sec_head("1.3 Scope of the Project"))
    body_parts.append(p_body(
        "The scope of this project encompasses the end-to-end operational lifecycle of academic and research laboratory equipment within an engineering institution. Major features include:"
    ))
    body_parts.append(p_body("Inventory Lifecycle Management: Asset tagging, hardware specifications cataloging, laboratory room mapping, and dynamic state tracking (Available, Booked, Under Maintenance, Decommissioned).", bullet=True))
    body_parts.append(p_body("Reservation Engine: Slot scheduling with millisecond-precision overlap checking, eliminating reservation collisions.", bullet=True))
    body_parts.append(p_body("Maintenance and Service Tracking: Preventive calibration scheduling, corrective repair logs, technician accountability, and service expenditure accounting.", bullet=True))
    body_parts.append(p_body("Dual-Database Plug-and-Play Architecture: Seamless support for enterprise MySQL server installations with automated fallback to an embedded SQLite database engine for zero-configuration standalone demonstration.", bullet=True))
    body_parts.append(p_body("Reporting & Auditing: Automated generation of structured CSV datasets and executive text audit summaries for administrative and regulatory inspections.", bullet=True))
    body_parts.append(p_body("Operational Limitations: The current version is engineered as a high-performance desktop GUI application operating over a local area network (LAN) or standalone workstation. Integration with physical RFID turnstiles and external payment gateways for commercial facility booking is outside the scope of this phase and identified for future release.", bullet=True))

    # ==================== CHAPTER 2 ====================
    body_parts.append(p_chapter_head("CHAPTER 2: REQUIREMENT ANALYSIS"))
    body_parts.append(p_sec_head("2.1 Functional Requirements"))
    body_parts.append(p_body(
        "The system has been designed and implemented in strict accordance with functional specifications structured into the following numbered requirements:"
    ))
    body_parts.append(p_body("FR-1: User Authentication & Security: The system must enforce credential-based authentication using SHA-256 cryptographic password hashing. Users must select their authorized role, with session validation preventing unauthorized elevation of privilege. Password reset facilities must be available.", bullet=True))
    body_parts.append(p_body("FR-2: User Self-Registration: New students and faculty members must be able to register through an interactive form specifying full name, username, official email, phone, department, and academic role, with automated input format validation.", bullet=True))
    body_parts.append(p_body("FR-3: Equipment Catalog & Master Data CRUD: Authorized administrators and technicians must have full CRUD capabilities (Create, Read, Update, Delete) for equipment records, capturing unique asset tags, nomenclature, category classification, model specifications, serial numbers, assigned lab rooms, purchase dates, and condition remarks.", bullet=True))
    body_parts.append(p_body("FR-4: Live Search & Multi-Tier Filtering: The system must provide instant, keystroke-responsive live search across asset tags, equipment names, models, and serial numbers, combined with multi-column filtering by category, laboratory room, and operational status.", bullet=True))
    body_parts.append(p_body("FR-5: Equipment Reservation & Conflict Validation: The system must allow users to schedule reservations specifying equipment, time window, and academic purpose. The reservation engine must execute automated conflict validation queries ensuring no slot collisions occur with existing active or approved reservations.", bullet=True))
    body_parts.append(p_body("FR-6: Equipment Check-Out & Return Management: The system must track the physical check-out of reserved equipment, transitioning status to ACTIVE upon issuance and restoring status to AVAILABLE upon physical return and inspection sign-off.", bullet=True))
    body_parts.append(p_body("FR-7: Maintenance & Service Lifecycle Management: The system must enable technicians and administrators to log maintenance tickets (Preventive, Calibration, Repair, Inspection, Firmware Update). When a ticket is opened, the associated equipment must automatically transition to UNDER_MAINTENANCE; upon completion sign-off, it must automatically restore to AVAILABLE.", bullet=True))
    body_parts.append(p_body("FR-8: Administrator User Management: Administrators must have dedicated facilities to view all registered accounts, modify user roles, toggle account status between ACTIVE and BLOCKED, reset passwords, and remove accounts.", bullet=True))
    body_parts.append(p_body("FR-9: Audit Logging & Analytical Reporting: Every critical operational event (user login, equipment registration, reservation creation, approval, return, maintenance logging) must be permanently recorded in an immutable audit log. The system must support instant CSV export of all datasets and generate formatted executive text audit summaries.", bullet=True))

    body_parts.append(p_sec_head("2.2 Technology Used"))
    body_parts.append(p_body(
        "To satisfy the technical curriculum requirements of the Minor Project NSP and ensure superior maintainability, the following technologies were employed:"
    ))
    body_parts.append(p_body("Java Standard Edition (JDK 17 LTS): Utilized as the core object-oriented programming language. Java 17 provides rock-solid long-term stability, robust type safety, modern collections, enhanced date-time APIs (java.sql.Timestamp, java.sql.Date), and high-performance virtual machine execution.", bullet=True))
    body_parts.append(p_body("Java Swing (javax.swing & java.awt): The standard Java graphical user interface framework used to design and render all application screens, window frames, dialogue boxes, complex table models, and layout managers (BorderLayout, GridBagLayout, GridLayout, BoxLayout, CardLayout).", bullet=True))
    body_parts.append(p_body("FlatLaf Modern Look & Feel (v3.4.1): An award-winning, state-of-the-art Swing Look and Feel library inspired by IntelliJ IDEA and modern design systems. FlatLaf delivers crisp typography, rounded component borders, clean card panels, subtle elevation shadows, and high-DPI scaling across operating systems.", bullet=True))
    body_parts.append(p_body("Java Database Connectivity (JDBC): Standardized Java database abstraction layer enabling seamless execution of parameterized SQL statements, prepared queries, transaction rollbacks, and connection pooling.", bullet=True))
    body_parts.append(p_body("MySQL Community Server 8.0 & MySQL Connector/J (v8.3.0): The production enterprise relational database engine used for multi-user transactional storage, enforcing referential integrity via foreign key cascades, triggers, and secondary B-tree indexes.", bullet=True))
    body_parts.append(p_body("Embedded SQLite Engine (v3.45.2.0): Integrated as an intelligent, zero-configuration fallback database. If the local machine does not have an active MySQL server running, the application seamlessly initializes an embedded SQLite database with identical tables and seed data, ensuring uninterrupted demonstration and evaluation.", bullet=True))
    body_parts.append(p_body("Apache Maven (v3.9.6): Industry-standard build automation and project lifecycle management tool used for declarative dependency management, compilation, resource filtering, and packaging of the executable fat JAR (LabEquipmentSystem.jar).", bullet=True))
    body_parts.append(p_body("IntelliJ IDEA Ultimate / Community Edition: Primary development environment utilized for source code authoring, interactive debugging, database inspection, and refactoring.", bullet=True))

    body_parts.append(p_sec_head("2.3 Graphical User Interface (GUI)"))
    body_parts.append(p_body(
        "The GUI has been designed following modern human-computer interaction principles, prioritizing visual clarity, keyboard accessibility, consistent color palettes, and informative user feedback. Key interfaces include:"
    ))
    body_parts.append(p_body("1. Two-Tone Authentication Screen: Features a deep slate-indigo branded hero panel displaying institutional affiliations and project features, alongside a crisp login card with role selection, show/hide password toggle, new user registration shortcut, password reset link, and quick-fill demo credential buttons for expedited evaluation.", bullet=False))
    body_parts.append(p_body("2. User Registration Dialogue: A clean modal dialogue with field validation enforcing email patterns, minimum password lengths, and department selections.", bullet=False))
    body_parts.append(p_body("3. Master Laboratory Dashboard: Incorporates a professional top header with institutional branding, a real-time live clock, logged-in user profile chip, and logout action. The left navigation sidebar provides one-click switching between functional modules. The Overview tab displays four dynamic KPI metric cards (Total Assets, Available Now, Under Maintenance, Active Bookings) and split-screen tables of recent activity.", bullet=False))
    body_parts.append(p_body("4. Equipment Inventory Management Screen: Houses a multi-parameter filtering bar (live text search, category dropdown, lab room selector, status filter) paired with a high-density tabular view rendered with colored pill badges for immediate status recognition.", bullet=False))
    body_parts.append(p_body("5. Asset Registration and Editing Dialogue: Multi-field form for cataloging technical specifications, serial numbers, purchase dates, and condition remarks.", bullet=False))
    body_parts.append(p_body("6. Reservation Management Screen: Displays all active, approved, and past reservations, with action buttons to confirm check-out returns or approve pending requests.", bullet=False))
    body_parts.append(p_body("7. Maintenance and Repair Portal: Dedicated module for logging service records, technician assignments, calibration dates, and repair expenditure.", bullet=False))
    body_parts.append(p_body("8. Executive Export and Audit Dialogue: Provides instant one-click CSV generation for all tables and real-time generation of executive laboratory audit text reports.", bullet=False))

    # ==================== CHAPTER 3 ====================
    body_parts.append(p_chapter_head("CHAPTER 3: DESIGN"))
    body_parts.append(p_sec_head("3.1 Use Case Diagram & System Workflow"))
    body_parts.append(p_body(
        "The Use Case Diagram illustrates the functional interactions between the four primary actors—Student, Faculty, Administrator, and Maintenance Technician—and the core use cases supported by the laboratory system. Key dependency relationships include an <<include>> relationship between 'Reserve / Book Equipment' and 'Validate Conflict & Slots', ensuring that slot conflict checking is unconditionally executed for every booking attempt. Furthermore, an <<extend>> relationship connects 'Log Maintenance & Repairs' with 'Update Equipment Status', dynamically updating the equipment state to reflect active maintenance or restored availability."
    ))

    # Embed Figure 3.1
    body_parts.append(p_image("rId101", 5300000, 3600000, 101))
    body_parts.append(p_caption("Figure 3.1: Use Case Diagram for Lab Equipment Booking and Maintenance System"))

    body_parts.append(p_body(
        "To complement the Use Case model, Figure 3.2 illustrates the complete end-to-end operational flowchart of the application, spanning initial authentication, role dispatching, dynamic inventory browsing, slot collision checking, database transaction commit, and subsequent return/maintenance closure."
    ))

    # Embed Figure 3.2
    body_parts.append(p_image("rId102", 5000000, 4400000, 102))
    body_parts.append(p_caption("Figure 3.2: System Architectural Workflow & Reservation Lifecycle Flowchart"))

    body_parts.append(p_sec_head("3.2 Database Tables"))
    body_parts.append(p_body(
        "The relational database schema is structured into six normalized tables designed to ensure zero data redundancy, complete referential integrity, and rapid query performance. The comprehensive data dictionary is presented below in accordance with the prescribed NSP reporting guidelines:"
    ))

    # Table 1: users
    body_parts.append(p_subsec_head("Table 3.1: Users Master Table (users)"))
    headers_u = ["S. No.", "Table Name", "Field Name", "Data Type", "Constraints & Description"]
    rows_u = [
        ["1", "users", "user_id", "INT / INTEGER", "PRIMARY KEY, AUTO_INCREMENT"],
        ["2", "users", "username", "VARCHAR(50)", "NOT NULL, UNIQUE, Login identifier"],
        ["3", "users", "password_hash", "VARCHAR(255)", "NOT NULL, SHA-256 salted hash"],
        ["4", "users", "full_name", "VARCHAR(100)", "NOT NULL, Complete user name"],
        ["5", "users", "email", "VARCHAR(100)", "NOT NULL, UNIQUE, Institutional email"],
        ["6", "users", "role", "VARCHAR(30)", "NOT NULL, ADMIN / FACULTY / STUDENT / TECHNICIAN"],
        ["7", "users", "department", "VARCHAR(100)", "NOT NULL, Academic department"],
        ["8", "users", "phone", "VARCHAR(20)", "Mobile contact number"],
        ["9", "users", "status", "VARCHAR(20)", "NOT NULL, ACTIVE / INACTIVE / BLOCKED"],
        ["10", "users", "created_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP, Registration date"]
    ]
    body_parts.append(make_table(headers_u, rows_u, [800, 1400, 1600, 1800, 3200]))

    # Table 2: categories
    body_parts.append(p_subsec_head("Table 3.2: Equipment Categories Table (categories)"))
    rows_c = [
        ["1", "categories", "category_id", "INT / INTEGER", "PRIMARY KEY, AUTO_INCREMENT"],
        ["2", "categories", "name", "VARCHAR(100)", "NOT NULL, UNIQUE, Category classification name"],
        ["3", "categories", "description", "TEXT", "Description of category hardware scope"]
    ]
    body_parts.append(make_table(headers_u, rows_c, [800, 1400, 1600, 1800, 3200]))

    # Table 3: equipment
    body_parts.append(p_subsec_head("Table 3.3: Equipment Master Table (equipment)"))
    rows_e = [
        ["1", "equipment", "equipment_id", "INT / INTEGER", "PRIMARY KEY, AUTO_INCREMENT"],
        ["2", "equipment", "asset_tag", "VARCHAR(50)", "NOT NULL, UNIQUE, Barcode/RFID asset tag"],
        ["3", "equipment", "name", "VARCHAR(150)", "NOT NULL, Standard nomenclature"],
        ["4", "equipment", "category_id", "INT / INTEGER", "FOREIGN KEY -> categories(category_id)"],
        ["5", "equipment", "model", "VARCHAR(100)", "Manufacturer hardware model & specifications"],
        ["6", "equipment", "serial_number", "VARCHAR(100)", "Manufacturer serial identifier"],
        ["7", "equipment", "lab_room", "VARCHAR(50)", "NOT NULL, Physical room location"],
        ["8", "equipment", "status", "VARCHAR(30)", "AVAILABLE / BOOKED / UNDER_MAINTENANCE / DECOMMISSIONED"],
        ["9", "equipment", "purchase_date", "DATE", "Acquisition and commissioning date"],
        ["10", "equipment", "condition_notes", "TEXT", "Calibration status and condition notes"],
        ["11", "equipment", "created_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP, Catalog timestamp"]
    ]
    body_parts.append(make_table(headers_u, rows_e, [800, 1400, 1600, 1800, 3200]))

    # Table 4: bookings
    body_parts.append(p_subsec_head("Table 3.4: Equipment Bookings Table (bookings)"))
    rows_b = [
        ["1", "bookings", "booking_id", "INT / INTEGER", "PRIMARY KEY, AUTO_INCREMENT"],
        ["2", "bookings", "equipment_id", "INT / INTEGER", "FOREIGN KEY -> equipment(equipment_id)"],
        ["3", "bookings", "user_id", "INT / INTEGER", "FOREIGN KEY -> users(user_id)"],
        ["4", "bookings", "start_time", "DATETIME / TIMESTAMP", "NOT NULL, Scheduled start time slot"],
        ["5", "bookings", "end_time", "DATETIME / TIMESTAMP", "NOT NULL, Scheduled completion time slot"],
        ["6", "bookings", "purpose", "TEXT", "NOT NULL, Academic or research project purpose"],
        ["7", "bookings", "status", "VARCHAR(30)", "PENDING / APPROVED / ACTIVE / COMPLETED / CANCELLED / REJECTED"],
        ["8", "bookings", "remarks", "TEXT", "Issuance notes and return condition remarks"],
        ["9", "bookings", "booked_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP, Reservation timestamp"]
    ]
    body_parts.append(make_table(headers_u, rows_b, [800, 1400, 1600, 1800, 3200]))

    # Table 5: maintenance_records
    body_parts.append(p_subsec_head("Table 3.5: Maintenance & Service Logs Table (maintenance_records)"))
    rows_m = [
        ["1", "maintenance_records", "record_id", "INT / INTEGER", "PRIMARY KEY, AUTO_INCREMENT"],
        ["2", "maintenance_records", "equipment_id", "INT / INTEGER", "FOREIGN KEY -> equipment(equipment_id)"],
        ["3", "maintenance_records", "maintenance_type", "VARCHAR(50)", "NOT NULL, PREVENTIVE / CALIBRATION / REPAIR / INSPECTION"],
        ["4", "maintenance_records", "start_date", "DATE", "NOT NULL, Service initiation date"],
        ["5", "maintenance_records", "completion_date", "DATE", "Service completion / sign-off date"],
        ["6", "maintenance_records", "technician_name", "VARCHAR(100)", "NOT NULL, Service engineer / vendor name"],
        ["7", "maintenance_records", "cost", "DECIMAL(10,2) / REAL", "DEFAULT 0.00, Incurred expenditure in INR"],
        ["8", "maintenance_records", "status", "VARCHAR(30)", "SCHEDULED / IN_PROGRESS / COMPLETED / CANCELLED"],
        ["9", "maintenance_records", "notes", "TEXT", "Diagnosis, replaced parts, and calibration certification"],
        ["10", "maintenance_records", "created_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP, Log creation timestamp"]
    ]
    body_parts.append(make_table(headers_u, rows_m, [800, 1400, 1600, 1800, 3200]))

    # Table 6: audit_logs
    body_parts.append(p_subsec_head("Table 3.6: System Audit Trail Table (audit_logs)"))
    rows_a = [
        ["1", "audit_logs", "log_id", "INT / INTEGER", "PRIMARY KEY, AUTO_INCREMENT"],
        ["2", "audit_logs", "user_id", "INT / INTEGER", "FOREIGN KEY -> users(user_id) ON DELETE SET NULL"],
        ["3", "audit_logs", "action", "VARCHAR(50)", "NOT NULL, Action tag (e.g. USER_LOGIN, BOOKING_CREATED)"],
        ["4", "audit_logs", "entity", "VARCHAR(50)", "NOT NULL, Target entity (EQUIPMENT, BOOKING, AUTH)"],
        ["5", "audit_logs", "entity_id", "INT / INTEGER", "Identifier of the target entity row"],
        ["6", "audit_logs", "details", "TEXT", "Descriptive contextual audit trail message"],
        ["7", "audit_logs", "timestamp", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP, Precise transaction timestamp"]
    ]
    body_parts.append(make_table(headers_u, rows_a, [800, 1400, 1600, 1800, 3200]))

    # ==================== CHAPTER 4 ====================
    body_parts.append(p_chapter_head("CHAPTER 4: FUTURE SCOPE & CONCLUSION"))
    body_parts.append(p_sec_head("4.1 Summary of Completed Work"))
    body_parts.append(p_body(
        "Over the duration of the 14-week Minor Project NSP timeline, a comprehensive, robust, and highly aesthetic Java-based Lab Equipment Booking and Maintenance System was successfully conceptualized, architected, implemented, and thoroughly tested. The software fully satisfies every course outcome and milestone specified in the departmental guidelines, including:"
    ))
    body_parts.append(p_body("Complete graphical user interface design using Java Swing components (JFrame, JPanel, JTable, JComboBox, JCheckBox, JScrollPane, JMenuBar) styled with modern FlatLaf delegates for visual elegance.", bullet=True))
    body_parts.append(p_body("Robust relational database design in MySQL and SQLite adhering to 3NF normalization, foreign key referential integrity constraints, and query indexes.", bullet=True))
    body_parts.append(p_body("Full JDBC Data Access Object (DAO) implementation providing comprehensive CRUD operations, transaction safety, and prepared statements preventing SQL injection attacks.", bullet=True))
    body_parts.append(p_body("Comprehensive input validation and multi-layer exception handling providing informative dialogs and graceful error recovery.", bullet=True))
    body_parts.append(p_body("Dynamic search and multi-criteria live filtering across equipment inventory, reservation schedules, service records, and user accounts.", bullet=True))
    body_parts.append(p_body("Automated reporting engine generating standardized CSV spreadsheet datasets and formatted executive text summaries for departmental audits.", bullet=True))

    body_parts.append(p_sec_head("4.2 Objectives Achieved and Operational Impact"))
    body_parts.append(p_body(
        "The application successfully resolves the persistent problems of double-booking, missing calibration records, untracked asset locations, and informal logbooks that previously afflicted institutional laboratories. By providing immediate visibility into equipment status, enforcing automated conflict validation, and centralizing maintenance diagnostics, the system maximizes asset utilization rates, extends equipment operating lifespans through timely calibration, and substantially reduces institutional operational overhead."
    ))

    body_parts.append(p_sec_head("4.3 Unique Feature for Future Implementation"))
    body_parts.append(p_body(
        "In strict adherence to the project guidelines requiring a unique feature proposal for subsequent development phases, the project team plans to integrate an IoT-Enabled RFID Smart Enclosure and Machine Learning Predictive Maintenance Subsystem:"
    ))
    body_parts.append(p_body(
        "Unique Feature Specification: IoT RFID Smart Cabinet with Edge Vibration AI Anomaly Detection. In this planned enhancement, physical laboratory storage cabinets and testbenches will be instrumented with UHF RFID reader antennas and ESP32 edge microcontroller nodes. When a student or faculty member whose reservation is active approaches the cabinet, their digital institutional RFID badge will automatically unlock the electromagnetic latch and log the physical removal of the asset without manual clerical intervention. Furthermore, high-value mechanical and electrical assets (such as 3D printers, CNC milling units, and high-frequency cooling turbines) will be equipped with MEMS tri-axial vibration and thermal sensors. Sensor telemetry streamed over MQTT to an embedded TinyML anomaly detection model will identify abnormal harmonic frequencies and thermal spikes prior to physical failure. This enables predictive maintenance—alerting technicians to schedule lubrication, belt tensioning, or thermal paste replacement weeks before catastrophic breakdowns occur, transitioning laboratory management from reactive repair to intelligent self-healing operation."
    ))

    # ==================== CHAPTER 5 ====================
    body_parts.append(p_chapter_head("CHAPTER 5: SNAPSHOTS OF THE PROJECT"))
    body_parts.append(p_body(
        "The following screenshots depict the actual, working Java Swing application populated with realistic engineering laboratory equipment, bookings, maintenance tickets, and user accounts:"
    ))

    # Embed all Figure 5.x screenshots
    for fig_id, fname, caption, w, h in figures[2:]: # skip 3.1 and 3.2 which were in ch 3
        body_parts.append(p_image(fig_id, w, h, int(fig_id.replace("rId", ""))))
        body_parts.append(p_caption(caption))

    # Close body with Section Properties (A4, 1-inch margins, footer page numbers)
    body_parts.append('''<w:sectPr>
        <w:footerReference w:type="default" r:id="rId9"/>
        <w:pgSz w:w="11909" w:h="16834"/>
        <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
        <w:cols w:space="720"/>
        <w:docGrid w:linePitch="360"/>
    </w:sectPr>''')

    full_document_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" 
            xmlns:cx="http://schemas.microsoft.com/office/drawing/2014/chartex" 
            xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" 
            xmlns:o="urn:schemas-microsoft-com:office:office" 
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" 
            xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" 
            xmlns:v="urn:schemas-microsoft-com:vml" 
            xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" 
            xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" 
            xmlns:w10="urn:schemas-microsoft-com:office:word" 
            xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" 
            xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" 
            xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" 
            xmlns:w16="http://schemas.microsoft.com/office/word/2018/wordml" 
            xmlns:w16cid="http://schemas.microsoft.com/office/word/2016/wordml/cid" 
            xmlns:w16se="http://schemas.microsoft.com/office/word/2015/wordml/symex" 
            xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" 
            mc:Ignorable="w14 w15 w16se w16cid w16 wp14">
    <w:body>
        {''.join(body_parts)}
    </w:body>
</w:document>'''

    # Build updated word/_rels/document.xml.rels
    rels_xml = [
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>',
        '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">',
        '<Relationship Id="rId8" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/image1.png"/>',
        '<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>',
        '<Relationship Id="rId9" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/>',
        '<Relationship Id="rId10" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable" Target="fontTable.xml"/>',
        '<Relationship Id="rId11" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="theme/theme1.xml"/>',
        '<Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>'
    ]

    for fig_id, fname, _, _, _ in figures:
        rels_xml.append(f'<Relationship Id="{fig_id}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/{fname}"/>')

    rels_xml.append('</Relationships>')
    full_rels_xml = ''.join(rels_xml)

    # Now assemble the final docx
    print(f"Reading template {template_docx}...")
    with zipfile.ZipFile(template_docx, 'r') as zin, zipfile.ZipFile(output_docx, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            if item.filename == 'word/document.xml':
                zout.writestr(item.filename, full_document_xml.encode('utf-8'))
            elif item.filename == 'word/_rels/document.xml.rels':
                zout.writestr(item.filename, full_rels_xml.encode('utf-8'))
            else:
                zout.writestr(item, zin.read(item.filename))

        # Add all screenshot images into word/media/
        for _, fname, _, _, _ in figures:
            fpath = os.path.join(screenshots_dir, fname)
            if os.path.exists(fpath):
                print(f"Adding media/{fname} to docx...")
                with open(fpath, 'rb') as fimg:
                    zout.writestr(f"word/media/{fname}", fimg.read())
            else:
                print(f"WARNING: Image {fpath} does not exist!")

    print(f"Project report successfully generated: {output_docx}")
    file_size = os.path.getsize(output_docx)
    print(f"File size: {file_size / 1024:.2f} KB")

if __name__ == '__main__':
    build_docx_report()
