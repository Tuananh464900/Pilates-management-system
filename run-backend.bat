@echo off
chcp 65001 >nul
title Pilates Backend Server (Port 8080)
echo ===================================================
echo   Khoi dong Pilates Backend Server (Port 8080)...
echo ===================================================
cd /d "%~dp0backend"

:: 1. Kiem tra neu may da co san lenh mvn trong PATH
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Da tim thay Maven trong bien moi truong PATH.
    call mvn spring-boot:run
    goto end
)

:: 2. Kiem tra Maven Wrapper neu co
if exist "mvnw.cmd" (
    echo [OK] Su dung Maven Wrapper cuc bo (mvnw.cmd)...
    call mvnw.cmd spring-boot:run
    goto end
)

:: 3. Du phong duong dan cau hinh may ca nhan
echo [INFO] Su dung duong dan Maven du phong...
set "PATH=C:\Users\Admin\.m2\wrapper\dists\apache-maven-3.9.11\d6d3cbd4012d4c1d840e93277aca316c\bin;C:\Users\Admin\Documents\apache-maven-3.9.11\bin;%PATH%"
call mvn spring-boot:run

:end
pause
