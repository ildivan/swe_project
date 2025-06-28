@echo off
REM Ottieni la directory dello script
set SCRIPT_DIR=%~dp0
REM Risali di due livelli fino alla root sweproject
for %%i in ("%SCRIPT_DIR%..\..") do set ROOT_DIR=%%~fi

echo Root directory individuata: %ROOT_DIR%

REM Apri nuova finestra per il server
start cmd /k "cd /d %ROOT_DIR% && call script\windows\run_server_win.bat"

REM Apri nuova finestra per il terminale utente
start cmd /k "cd /d %ROOT_DIR% && call script\windows\run_user_win.bat"

REM Apri nuova finestra per il terminale backend
start cmd /k "cd /d %ROOT_DIR% && call script\windows\run_backend_win.bat"

echo Tutti i processi sono stati avviati.
pause
