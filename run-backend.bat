@echo off
chcp 65001 >nul
title Pilates Backend Server (Port 8080)
echo ===================================================
echo   Khoi dong Pilates Backend Server (Port 8080)...
echo ===================================================
cd /d "%~dp0backend"

:: 1. Kiem tra neu may da co san lenh mvn trong PATH he thong
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Da tim thay Maven trong PATH he thong.
    call mvn spring-boot:run
    goto end
)

:: 2. Kiem tra bien moi truong MAVEN_HOME hoac M2_HOME
if defined MAVEN_HOME (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        echo [OK] Su dung Maven tu MAVEN_HOME: %MAVEN_HOME%
        set "PATH=%MAVEN_HOME%\bin;%PATH%"
        call mvn spring-boot:run
        goto end
    )
)
if defined M2_HOME (
    if exist "%M2_HOME%\bin\mvn.cmd" (
        echo [OK] Su dung Maven tu M2_HOME: %M2_HOME%
        set "PATH=%M2_HOME%\bin;%PATH%"
        call mvn spring-boot:run
        goto end
    )
)

:: 3. Kiem tra cac thu muc Maven pho bien tren may qua bien moi truong %%USERPROFILE%%
if exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.11\d6d3cbd4012d4c1d840e93277aca316c\bin\mvn.cmd" (
    echo [OK] Su dung Maven tu thu vien cuc bo (.m2/wrapper)...
    set "PATH=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.11\d6d3cbd4012d4c1d840e93277aca316c\bin;%PATH%"
    call mvn spring-boot:run
    goto end
)
if exist "%USERPROFILE%\Documents\apache-maven-3.9.11\bin\mvn.cmd" (
    echo [OK] Su dung Maven tu Documents...
    set "PATH=%USERPROFILE%\Documents\apache-maven-3.9.11\bin;%PATH%"
    call mvn spring-boot:run
    goto end
)
if exist "C:\Program Files\apache-maven\bin\mvn.cmd" (
    echo [OK] Su dung Maven tu Program Files...
    set "PATH=C:\Program Files\apache-maven\bin;%PATH%"
    call mvn spring-boot:run
    goto end
)

:: 4. Thong bao huong dan neu he thong chua cai dat Maven
echo.
echo [CANH BAO] Khong tim thay lenh 'mvn' tren he thong.
echo Vui long cai dat Apache Maven (https://maven.apache.org/) va them thu muc bin vao PATH.
echo.

:end
pause
