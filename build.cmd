@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>&1
if errorlevel 1 (
  echo JDK not found. Install JDK 11 or newer and add it to PATH.
  exit /b 1
)
if not exist "build\classes" mkdir "build\classes"
javac --release 8 -Xlint:-options -encoding UTF-8 -d "build\classes" src\main\java\ErrorAnalyzer.java src\main\java\JavaErrorHelper.java
if errorlevel 1 exit /b 1
jar cfe "java-error-helper.jar" JavaErrorHelper -C "build\classes" .
if errorlevel 1 exit /b 1
echo Built java-error-helper.jar
