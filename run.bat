@echo off
REM =====================================================================
REM Lab Equipment Booking and Maintenance System Runner Script (Windows)
REM =====================================================================
title Lab Equipment Booking and Maintenance System - Poornima Institute

cd /d "%~dp0"

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Java 17+ is required to run this application.
    echo Please install OpenJDK 17 or Oracle JDK 17 and add it to your PATH.
    pause
    exit /b 1
)

echo =====================================================================
echo  Starting Lab Equipment Booking & Maintenance System
echo  Department of Computer Engineering - Poornima Institute
echo =====================================================================
java -jar target\LabEquipmentSystem.jar
pause
