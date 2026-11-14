@echo off
chcp 65001 >nul
title Pilates Frontend Server (Port 5173)
echo ===================================================
echo   Khoi dong Pilates Frontend React Vite (Port 5173)...
echo ===================================================
cd /d "%~dp0frontend"

:: Kiem tra neu thu muc node_modules chua co thi tu dong cai dat
if not exist "node_modules\" (
    echo [INFO] Phat hien chua co node_modules. Dang tu dong cai dat dependencies...
    call npm install
)

echo [OK] Dang khoi chay Frontend Dev Server...
call npm run dev
pause
