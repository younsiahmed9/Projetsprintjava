@echo off
REM Script pour lancer FinTrack directement sans Maven
REM Usage: run-direct.cmd

echo === Lancement Direct de FinTrack ===
echo.

REM Définir JAVA_HOME
set JAVA_HOME=C:\Users\HP\Downloads\jdk-21_windows-x64_bin\jdk-21.0.10
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if not exist "%JAVA_EXE%" (
    echo ERREUR: Java non trouve a %JAVA_EXE%
    echo Veuillez ajuster JAVA_HOME dans ce script
    pause
    exit /b 1
)

echo Java trouve: %JAVA_EXE%
echo.

REM Aller dans le repertoire du projet
cd /d %~dp0

REM Definir le classpath
set CP=target\classes
set CP=%CP%;%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\9.1.0\mysql-connector-j-9.1.0.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.6\javafx-controls-21.0.6.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.6\javafx-controls-21.0.6-win.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\21.0.6\javafx-graphics-21.0.6.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\21.0.6\javafx-graphics-21.0.6-win.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\21.0.6\javafx-base-21.0.6.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\21.0.6\javafx-base-21.0.6-win.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjfx\javafx-fxml\21.0.6\javafx-fxml-21.0.6.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\org\openjfx\javafx-fxml\21.0.6\javafx-fxml-21.0.6-win.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\net\java\dev\jna\jna\5.14.0\jna-5.14.0.jar
set CP=%CP%;%USERPROFILE%\.m2\repository\net\java\dev\jna\jna-platform\5.14.0\jna-platform-5.14.0.jar

echo Lancement de FinTrack...
echo.

"%JAVA_EXE%" -cp "%CP%" Controllers.FinTrackApp

pause
