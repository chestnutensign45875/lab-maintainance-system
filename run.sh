#!/bin/bash
# =====================================================================
# Lab Equipment Booking and Maintenance System Runner Script (Linux/macOS)
# =====================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check for local bundled JDK or system Java
if [ -f "$SCRIPT_DIR/.tools/jdk17/bin/java" ]; then
    JAVA_CMD="$SCRIPT_DIR/.tools/jdk17/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA_CMD="java"
else
    echo "Error: Java 17+ is required to run this application."
    echo "Please install JDK 17 or ensure 'java' is available in your PATH."
    exit 1
fi

echo "====================================================================="
echo " Starting Lab Equipment Booking & Maintenance System"
echo " Department of Computer Engineering - Poornima Institute"
echo "====================================================================="
"$JAVA_CMD" -jar target/LabEquipmentSystem.jar
