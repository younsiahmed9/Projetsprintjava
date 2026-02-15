@echo off
REM Script de lancement FinTrack
REM Ce script lance l'application en utilisant IntelliJ ou Maven

setlocal enabledelayedexpansion

echo.
echo ========================================
echo   Lancement de FinTrack
echo ========================================
echo.

cd /d C:\Users\Mega-PC\IdeaProjects\FinTrack

REM Verifier si target/classes existe
if not exist "target\classes" (
    echo.
    echo ERREUR: target\classes n'existe pas!
    echo.
    echo SOLUTION: Compilez d'abord le projet via IntelliJ IDEA
    echo Build menu - Build Project (ou Ctrl+F9)
    echo.
    pause
    exit /b 1
)

echo. Repertoire cible trouve: target\classes
echo.
echo ========================================
echo   Lancement de l'application via IntelliJ...
echo ========================================
echo.

REM La meilleure facon est de lancer via IntelliJ
REM Chercher IntelliJ et lancer MainFx
for /f "tokens=*" %%A in ('dir /b "C:\Program Files\JetBrains\IntelliJ*" 2^>nul') do (
    set INTELLIJ_HOME=C:\Program Files\JetBrains\%%A
    if exist "!INTELLIJ_HOME!\bin\idea.exe" (
        echo Lancement via IntelliJ...
        start "" "!INTELLIJ_HOME!\bin\idea.exe" --line 1 "src\main\java\Test\MainFx.java"
        timeout /t 3
        echo.
        echo IntelliJ se lance. Cliquez sur le bouton RUN vert pour lancer l'application.
        pause
        exit /b 0
    )
)

REM Si IntelliJ n'est pas trouve, essayer de lancer directement avec Java et le classpath Maven
echo IntelliJ non trouve. Tentative de lancement direct...
echo.

REM Construire le classpath avec les dependances Maven
set CLASSPATH=target\classes

REM Ajouter les JAR de Maven
for /r "%USERPROFILE%\.m2\repository\org\openjfx" %%F in (*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%F
)

for /r "%USERPROFILE%\.m2\repository\com\mysql" %%F in (*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%F
)

for /r "%USERPROFILE%\.m2\repository\com\google\protobuf" %%F in (*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%F
)

echo.
echo Lancement avec le classpath Maven...
echo.

REM Lancer avec java
java -cp "%CLASSPATH%" Test.MainFx

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Application terminee avec succes!
) else (
    echo.
    echo ERREUR lors de l'execution de l'application!
    echo Code d'erreur: %ERRORLEVEL%
)

echo.
pause

