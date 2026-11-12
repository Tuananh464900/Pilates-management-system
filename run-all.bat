@echo off
chcp 65001 >nul
title Khởi động toàn bộ Hệ thống Pilates
echo ===================================================
echo   Đang mở đồng thời Backend và Frontend...
echo ===================================================
start "Pilates Backend (8080)" cmd /k "%~dp0run-backend.bat"
start "Pilates Frontend (5173)" cmd /k "%~dp0run-frontend.bat"
echo.
echo Đã mở 2 cửa sổ chạy Backend và Frontend.
echo Hãy đợi vài giây rồi mở trình duyệt tại: http://localhost:5173
timeout /t 5

