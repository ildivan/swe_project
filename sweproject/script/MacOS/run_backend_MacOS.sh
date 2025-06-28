#!/bin/bash

SRC_DIR="src"
OUT_DIR="out_client"
BUILD_DIR="build_client"
JAR_NAME="Client.jar"
MAIN_CLASS="frontend.TerminalBackend"
CLASSPATH="lib/*"

echo "Pulizia..."
rm -rf "$OUT_DIR" "$BUILD_DIR"
mkdir -p "$OUT_DIR" "$BUILD_DIR"

echo "Compilazione dei file Java..."
if ! javac -cp "$CLASSPATH" -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java"); then
    echo "❌ Errore durante la compilazione."
    exit 1
fi

echo "Creazione del JAR..."
if ! jar cfe "$BUILD_DIR/$JAR_NAME" "$MAIN_CLASS" -C "$OUT_DIR" .; then
    echo "❌ Errore durante la creazione del JAR."
    exit 1
fi

echo "Avvio del client in un nuovo terminale..."
osascript <<EOF
tell application "Terminal"
    do script "cd '$PWD' && java -cp '$CLASSPATH:$BUILD_DIR/$JAR_NAME' $MAIN_CLASS"
end tell
EOF

echo "✅ Client avviato!"
