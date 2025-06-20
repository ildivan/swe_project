#!/bin/bash

# da correggere, sono cambiati i terminali

echo "Compilazione di tutti i file Java..."
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java") || { echo "Errore nella compilazione"; exit 1; }

echo "Creazione di Terminal.jar..."
mkdir -p build
jar cfe build/Terminal.jar frontend.Terminal -C out . || { echo "Errore nella creazione del jar"; exit 1; }

echo "Avvio del Terminale in una nuova finestra del Terminale..."
osascript -e 'tell application "Terminal" to do script "cd \"'"$PWD"'\" && java -cp \"lib/*:build/Terminal.jar\" frontend.Terminal"'

