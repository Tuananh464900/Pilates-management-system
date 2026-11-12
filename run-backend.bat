@echo off
chcp 65001 >nul
title Khởi động Pilates Backend (Port 8080)
echo ===================================================
echo   Đang nạp Maven và khởi động Pilates Backend...
echo ===================================================
set "PATH=C:\Users\Admin\.m2\wrapper\dists\apache-maven-3.9.11\d6d3cbd4012d4c1d840e93277aca316c\bin;C:\Users\Admin\Documents\apache-maven-3.9.11\bin;%PATH%"
cd /d "%~dp0backend"
mvn spring-boot:run
pause

