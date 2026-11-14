@echo off
chcp 65001 >nul
title Khởi động Pilates Backend (Port 8080)
echo ===================================================
echo   Đang khởi động Pilates Backend (Port 8080)...
echo ===================================================
cd /d "%~dp0backend"

:: 1. Kiểm tra nếu máy đã có sẵn lệnh mvn trong PATH
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    echo Đã tìm thấy Maven trong biến môi trường PATH hệ thống.
    mvn spring-boot:run
    goto end
)

:: 2. Kiểm tra Maven Wrapper nếu có
if exist "mvnw.cmd" (
    echo Sử dụng Maven Wrapper cục bộ...
    call mvnw.cmd spring-boot:run
    goto end
)

:: 3. Dự phòng đường dẫn cấu hình máy cá nhân
set "PATH=C:\Users\Admin\.m2\wrapper\dists\apache-maven-3.9.11\d6d3cbd4012d4c1d840e93277aca316c\bin;C:\Users\Admin\Documents\apache-maven-3.9.11\bin;%PATH%"
mvn spring-boot:run

:end
pause

