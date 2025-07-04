@echo off
echo Starting MCP Alarm Center...
echo.

echo Checking Java version...
java -version
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo Building and starting the application...
mvn spring-boot:run

pause 