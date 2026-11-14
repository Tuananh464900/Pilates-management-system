@echo off
chcp 65001 >nul
title Khởi động toàn bộ Hệ thống Pilates (Port 8080 & 5173)
echo ===================================================
echo   Đang mở đồng thời Backend và Frontend...
echo ===================================================
start "Pilates Backend (8080)" cmd /k "%~dp0run-backend.bat"
start "Pilates Frontend (5173)" cmd /k "%~dp0run-frontend.bat"
echo.
echo [OK] Đã mở 2 cửa sổ chạy Backend và Frontend.
echo Hãy đợi vài giây để Backend nạp xong rồi mở trình duyệt tại:
echo http://localhost:5173
echo.
timeout /t 5
