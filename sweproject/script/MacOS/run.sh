#!/bin/bash

# Directory root del progetto (aggiusta il path se serve)
ROOT_DIR="$(cd "$(dirname "$0")/../../" && pwd)"
SRC_DIR="$ROOT_DIR/src"
OUT_DIR="$ROOT_DIR/out"
BUILD_DIR="$ROOT_DIR/build"
JAR_NAME="Server.jar"
MAIN_CLASS="server.Server"
CLASSPATH="$ROOT_DIR/lib/*"

echo "Pulizia delle build precedenti..."
rm -rf "$OUT_DIR" "$BUILD_DIR"
mkdir -p "$OUT_DIR" "$BUILD_DIR"

echo "Compilazione dei file Java..."
if ! javac -cp "$CLASSPATH" -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java"); then
    echo "❌ Errore durante la compilazione."
    exit 1
fi

echo "Creazione del file JAR..."
if ! jar cfe "$BUILD_DIR/$JAR_NAME" "$MAIN_CLASS" -C "$OUT_DIR" .; then
    echo "❌ Errore durante la creazione del JAR."
    exit 1
fi

echo "Avvio del server nella finestra attuale..."
cd "$ROOT_DIR"
java -cp "$CLASSPATH:$BUILD_DIR/$JAR_NAME" "$MAIN_CLASS"

echo "Server fermo. Premi INVIO per avviare il Terminal User..."
read -r

echo "Avvio del Terminal User in nuova finestra..."
osascript <<EOF
tell application "Terminal"
    do script "cd \"$ROOT_DIR\" && ./script/MacOS/run_user_MacOS.sh"
end tell
EOF

echo "Avvio del Terminal Backend in nuova finestra..."
osascript <<EOF
tell application "Terminal"
    do script "cd \"$ROOT_DIR\" && ./script/MacOS/run_backend_MacOS.sh"
end tell
EOF

echo "Tutto avviato."
