#!/bin/bash

echo "Compilazione di tutti i file Java..."
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java") || { echo "Errore nella compilazione"; exit 1; }

echo "Creazione di Server.jar..."
mkdir -p build
jar cfe build/Server.jar server.Server -C out . || { echo "Errore nella creazione del jar"; exit 1; }

echo "Avvio del Server in una nuova finestra del Terminale..."
osascript -e 'tell application "Terminal" to do script "cd \"'"$PWD"'\" && java -cp \"lib/*:build/Server.jar\" server.Server"'
