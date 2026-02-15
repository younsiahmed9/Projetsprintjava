#!/bin/bash
# Script de lancement FinTrack (pour Linux/Mac ou Git Bash sur Windows)

cd "$(dirname "$0")"

JAVA_FX_LIB="C:\javafx-sdk-17\lib"
PROJECT_DIR=$(pwd)

echo "========================================"
echo "  Lancement de FinTrack"
echo "========================================"
echo "Répertoire du projet: $PROJECT_DIR"

# Vérifier si target/classes existe
if [ ! -d "target/classes" ]; then
    echo "Compilation nécessaire... (Veuillez compiler via IntelliJ)"
    echo "target/classes n'existe pas. Compilez d'abord via IntelliJ ou IDE."
    exit 1
fi

# Lancer l'application
echo "Lancement de l'application..."

java \
    --module-path "$JAVA_FX_LIB" \
    --add-modules javafx.controls,javafx.fxml \
    -cp "target/classes:target/dependency/*" \
    Test.MainFx

exit 0

