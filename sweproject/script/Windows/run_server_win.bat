@echo off
setlocal

set "SRC_DIR=src"
set "OUT_DIR=out"
set "BUILD_DIR=build"
set "JAR_NAME=Server.jar"
set "MAIN_CLASS=server.Server"
set "CLASSPATH=lib/*"

echo Cleaning previous builds...
rmdir /s /q "%OUT_DIR%" 2>nul
rmdir /s /q "%BUILD_DIR%" 2>nul
mkdir "%OUT_DIR%"
mkdir "%BUILD_DIR%"

echo Compiling Java files...
for /f "delims=" %%f in ('dir /s /b "%SRC_DIR%\*.java"') do (
    echo %%f
)

javac -cp "%CLASSPATH%" -d "%OUT_DIR%" %SRC_DIR%\**\*.java
if errorlevel 1 (
    echo ❌ Compilation failed.
    exit /b 1
)

echo Creating JAR...
jar cfe "%BUILD_DIR%\%JAR_NAME%" %MAIN_CLASS% -C "%OUT_DIR%" .
if errorlevel 1 (
    echo ❌ JAR creation failed.
    exit /b 1
)

echo Starting server in a new command window...
start "Server" cmd /k "java -cp "%CLASSPATH%;%BUILD_DIR%\%JAR_NAME%" %MAIN_CLASS%"

echo ✅ Server started in new Command Prompt window.

endlocal
