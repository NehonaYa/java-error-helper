@echo off
setlocal
cd /d "%~dp0"
call build.cmd
if errorlevel 1 exit /b 1
if not exist "build\test-classes" mkdir "build\test-classes"
javac --release 8 -Xlint:-options -encoding UTF-8 -cp "build\classes" -d "build\test-classes" src\test\java\ErrorAnalyzerTest.java
if errorlevel 1 exit /b 1
java -cp "build\classes;build\test-classes" ErrorAnalyzerTest
if errorlevel 1 exit /b 1
