#!/bin/bash

SRC_DIR="src"
OUT_DIR="out_user_client"
BUILD_DIR="build_user_client"
JAR_NAME="UserClient.jar"
MAIN_CLASS="frontend.TerminalUser"
CLASSPATH="lib/*"

echo "Pulizia..."
rm -rf "$OUT_DIR" "$BUILD_DIR"
mkdir -p "$OUT_DIR" "$BUILD_DIR"

echo "Compilazione..."
if ! javac -cp "$CLASSPATH" -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java"); then
    echo "❌ Errore durante la compilazione."
    exit 1
fi

echo "Creazione del JAR..."
if ! jar cfe "$BUILD_DIR/$JAR_NAME" "$MAIN_CLASS" -C "$OUT_DIR" .; then
    echo "❌ Errore nella creazione del JAR."
    exit 1
fi

echo "Avvio del client in un nuovo terminale..."
gnome-terminal -- bash -c "cd '$PWD'; java -cp '$CLASSPATH:$BUILD_DIR/$JAR_NAME' $MAIN_CLASS; exec bash"
