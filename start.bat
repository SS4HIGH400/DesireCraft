@echo off
chcp 65001 >nul
color 0B
title the best discord leak - dsc.gg/roomdevs

echo.
echo  ██████╗  ██████╗  ██████╗ ███╗   ███╗██████╗ ███████╗██╗   ██╗
echo  ██╔══██╗██╔═══██╗██╔═══██╗████╗ ████║██╔══██╗██╔════╝██║   ██║
echo  ██████╔╝██║   ██║██║   ██║██╔████╔██║██║  ██║█████╗  ██║   ██║
echo  ██╔══██╗██║   ██║██║   ██║██║╚██╔╝██║██║  ██║██╔══╝  ╚██╗ ██╔╝
echo  ██║  ██║╚██████╔╝╚██████╔╝██║ ╚═╝ ██║██████╔╝███████╗ ╚████╔╝
echo  ╚═╝  ╚═╝ ╚═════╝  ╚═════╝ ╚═╝     ╚═╝╚═════╝ ╚══════╝  ╚═══╝
echo.
echo           https://dsc.gg/roomdevs
echo.

timeout /t 2 >nul
cls

:: Проверка Java
if not exist "C:\Program Files\Java\jdk-21.0.11\bin\java.exe" (
    color 0C
    echo.
    echo [ОШИБКА] Java 21 не найдена!
    echo.
    pause
    exit
)

:: Проверка server.jar
if not exist "server.jar" (
    color 0C
    echo.
    echo [ОШИБКА] Файл server.jar не найден!
    echo.
    pause
    exit
)

echo ==========================================
echo           Подпишитесь на нас в Telegram: t.me/roomdevs
echo ==========================================
echo.

"C:\Program Files\Java\jdk-21.0.11\bin\java.exe" ^
-Djline.terminal=jline.UnsupportedTerminal ^
-DPaper.IgnoreJavaVersion=true ^
-Xms3G -Xmx3G ^
-jar server.jar nogui

echo.
echo Сервер остановлен.
echo.

pause