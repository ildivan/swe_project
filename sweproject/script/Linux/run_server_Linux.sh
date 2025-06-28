#!/bin/bash

SRC_DIR="src"
OUT_DIR="out"
BUILD_DIR="build"
JAR_NAME="Server.jar"
MAIN_CLASS="server.Server"
CLASSPATH="lib/*"

echo "Pulizia delle build precedenti..."
rm -rf "$OUT_DIR" "$BUILD_DIR"
mkdir -p "$OUT_DIR" "$BUILD_DIR"

echo "Compilazione dei file Java..."
if ! javac -cp "$CLASSPATH" -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java"); then
    echo "Errore durante la compilazione."
    exit 1
fi

echo "Creazione del file JAR..."
if ! jar cfe "$BUILD_DIR/$JAR_NAME" "$MAIN_CLASS" -C "$OUT_DIR" .; then
    echo "Errore durante la creazione del JAR."
    exit 1
fi

echo "Avvio del server in una nuova finestra del terminale..."
osascript <<EOF
tell application "Terminal"
    do script "cd '$PWD' && java -cp '$CLASSPATH:$BUILD_DIR/$JAR_NAME' $MAIN_CLASS"
end tell
EOF

echo "Server avviato in una nuova finestra del terminale."
