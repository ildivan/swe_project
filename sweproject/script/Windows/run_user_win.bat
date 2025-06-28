@echo off
set SRC_DIR=src
set OUT_DIR=out_user_client
set BUILD_DIR=build_user_client
set JAR_NAME=UserClient.jar
set MAIN_CLASS=frontend.TerminalUser

echo Pulizia...
rmdir /S /Q %OUT_DIR%
rmdir /S /Q %BUILD_DIR%
mkdir %OUT_DIR%
mkdir %BUILD_DIR%

echo Compilazione...
javac -cp "lib/*" -d %OUT_DIR% %SRC_DIR%\frontend\TerminalUser.java
if %errorlevel% neq 0 (
    echo ❌ Errore durante la compilazione.
    exit /b 1
)

echo Creazione del JAR...
jar cfe %BUILD_DIR%\%JAR_NAME% %MAIN_CLASS% -C %OUT_DIR% .
if %errorlevel% neq 0 (
    echo ❌ Errore nella creazione del JAR.
    exit /b 1
)

echo Avvio del client in una nuova finestra...
start cmd /k "java -cp lib/*;%BUILD_DIR%\%JAR_NAME% %MAIN_CLASS%"
