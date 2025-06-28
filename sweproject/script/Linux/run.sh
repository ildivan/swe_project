#!/bin/bash

# --- Individua la cartella root sweproject basata sulla posizione dello script ---
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Assumiamo la struttura script/<os>/run_all.sh, vogliamo risalire di 2 livelli a sweproject
ROOT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"

echo "Root directory individuata: $ROOT_DIR"

# --- Identifica sistema operativo ---
OS_TYPE=$(uname)
if [[ "$OS_TYPE" == "Darwin" ]]; then
    PLATFORM="MacOS"
elif [[ "$OS_TYPE" == "Linux" ]]; then
    PLATFORM="Linux"
else
    echo "Sistema operativo non supportato: $OS_TYPE"
    exit 1
fi

echo "Sistema operativo rilevato: $PLATFORM"

# --- Percorsi script ---
SCRIPT_DIR_PLATFORM="$ROOT_DIR/script/$PLATFORM"

# --- Funzione per aprire nuove finestre terminali ---
open_terminal() {
    local CMD="$1"
    if [[ "$PLATFORM" == "MacOS" ]]; then
        osascript -e "tell application \"Terminal\" to do script \"cd '$ROOT_DIR' && $CMD\""
    else
        # L: prova con gnome-terminal o xterm
        if command -v gnome-terminal &> /dev/null; then
            gnome-terminal -- bash -c "cd '$ROOT_DIR' && $CMD; exec bash"
        elif command -v xterm &> /dev/null; then
            xterm -e "bash -c 'cd \"$ROOT_DIR\" && $CMD; exec bash'"
        else
            echo "Nessun terminale grafico trovato (gnome-terminal o xterm)"
            echo "Eseguo comando direttamente in questo terminale:"
            bash -c "cd '$ROOT_DIR' && $CMD"
        fi
    fi
}

echo "Avvio server..."
open_terminal "bash '$SCRIPT_DIR_PLATFORM/run_server_${PLATFORM}.sh'"

# Aspetta 2 secondi per sicurezza
sleep 2

echo "Avvio terminale utente..."
open_terminal "bash '$SCRIPT_DIR_PLATFORM/run_user_${PLATFORM}.sh'"

echo "Avvio terminale backend..."
open_terminal "bash '$SCRIPT_DIR_PLATFORM/run_backend_${PLATFORM}.sh'"

echo "Tutti i processi avviati."
