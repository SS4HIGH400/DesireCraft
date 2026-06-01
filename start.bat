@echo off
cd /d "%~dp0"

set "JAVA_EXE="

if defined JAVA_EXE_OVERRIDE (
  if exist "%JAVA_EXE_OVERRIDE%" set "JAVA_EXE=%JAVA_EXE_OVERRIDE%"
)

if not defined JAVA_EXE (
  if exist "%~dp0.runtime\java17\bin\java.exe" set "JAVA_EXE=%~dp0.runtime\java17\bin\java.exe"
)

if not defined JAVA_EXE (
  if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
  )
)

if not defined JAVA_EXE (
  where java.exe >nul 2>nul
  if not errorlevel 1 set "JAVA_EXE=java.exe"
)

if not defined JAVA_EXE (
  echo Java 17 was not found.
  echo Install Java 17, set JAVA_HOME, or put Java into .runtime\java17.
  pause
  exit /b 1
)

"%JAVA_EXE%" -DPaper.IgnoreJavaVersion=true -Xmx4G -Xms4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -jar server.jar nogui

pause
