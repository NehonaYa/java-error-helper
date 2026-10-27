@echo off
setlocal
cd /d "%~dp0"
if not exist "java-error-helper.jar" (
  echo Missing java-error-helper.jar. Build the project with build.cmd.
  pause
  exit /b 1
)
where javaw >nul 2>&1
if not errorlevel 1 (
  start "" javaw -jar "%~dp0java-error-helper.jar"
  exit /b 0
)
where java >nul 2>&1
if errorlevel 1 (
  echo Java is not available in PATH. Install Java 8 or newer.
  pause
  exit /b 1
)
java -jar "%~dp0java-error-helper.jar"
if errorlevel 1 pause
