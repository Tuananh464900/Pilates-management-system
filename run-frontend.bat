@echo off
chcp 65001 >nul
title Khởi động Pilates Frontend (Port 5173)
echo ===================================================
echo   Đang khởi động Pilates Frontend React Vite...
echo ===================================================
cd /d "%~dp0frontend"
npm run dev
pause

