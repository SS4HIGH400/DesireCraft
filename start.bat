@echo off
color 0B
title DesireCraft

echo ==========================================
echo                 DesireCraft
echo ==========================================
echo.

set "JAVA_EXE=%~dp0.runtime\java17\bin\java.exe"

if not exist "%JAVA_EXE%" (
    color 0C
    echo [ERROR] Bundled Java 17 was not found.
    pause
    exit /b 1
)

if not exist "server.jar" (
    color 0C
    echo [ERROR] server.jar was not found.
    pause
    exit /b 1
)

"%JAVA_EXE%" -Djline.terminal=jline.UnsupportedTerminal -DPaper.IgnoreJavaVersion=true -Xms3G -Xmx3G -jar server.jar nogui

echo.
echo DesireCraft server stopped.
pause
