@echo off
echo ========================================
echo  FinTrack - Gestion Documents
echo  Lancement de l'application...
echo ========================================
echo.

cd /d "%~dp0"

echo Compilation du projet...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: La compilation a echoue!
    pause
    exit /b 1
)

echo.
echo Lancement de l'application JavaFX...
call mvn javafx:run

pause

