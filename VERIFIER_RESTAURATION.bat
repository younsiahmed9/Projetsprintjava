@echo off
REM Script de vérification de restauration
REM Vérifie que tous les fichiers restaurés sont présents

echo.
echo ╔════════════════════════════════════════════╗
echo ║   FinTrack - Vérification de Restauration ║
echo ╚════════════════════════════════════════════╝
echo.

setlocal enabledelayedexpansion
set "missingCount=0"
set "foundCount=0"

REM Vérifier les services
echo 📋 Vérification des Services...
for %%F in (
    "src\main\java\Services\ServiceScanner.java"
    "src\main\java\Services\ServiceDoublon.java"
    "src\main\java\Services\ServiceEcheance.java"
    "src\main\java\Services\ServiceTraduction.java"
) do (
    if exist %%F (
        echo   ✅ %%F
        set /a "foundCount+=1"
    ) else (
        echo   ❌ %%F
        set /a "missingCount+=1"
    )
)
echo.

REM Vérifier les contrôleurs
echo 📋 Vérification des Contrôleurs...
for %%F in (
    "src\main\java\Controllers\EcheanceDashboardController.java"
    "src\main\java\Controllers\DoublonDetectionPanel.java"
    "src\main\java\Controllers\IntegrationGuide.java"
) do (
    if exist %%F (
        echo   ✅ %%F
        set /a "foundCount+=1"
    ) else (
        echo   ❌ %%F
        set /a "missingCount+=1"
    )
)
echo.

REM Vérifier les modèles
echo 📋 Vérification des Modèles...
if exist "src\main\java\Models\Echeance.java" (
    echo   ✅ Echeance.java
    set /a "foundCount+=1"
) else (
    echo   ❌ Echeance.java
    set /a "missingCount+=1"
)
if exist "src\main\java\utils\DataSource.java" (
    echo   ✅ DataSource.java
    set /a "foundCount+=1"
) else (
    echo   ❌ DataSource.java
    set /a "missingCount+=1"
)
echo.

REM Vérifier les fichiers SQL
echo 📋 Vérification des Scripts SQL...
if exist "fintrack_complete_database.sql" (
    echo   ✅ fintrack_complete_database.sql
    set /a "foundCount+=1"
) else (
    echo   ❌ fintrack_complete_database.sql
    set /a "missingCount+=1"
)
if exist "create_echeance_table.sql" (
    echo   ✅ create_echeance_table.sql
    set /a "foundCount+=1"
) else (
    echo   ❌ create_echeance_table.sql
    set /a "missingCount+=1"
)
echo.

REM Vérifier la documentation
echo 📋 Vérification de la Documentation...
if exist "README_SERVICES.md" (
    echo   ✅ README_SERVICES.md
    set /a "foundCount+=1"
) else (
    echo   ❌ README_SERVICES.md
    set /a "missingCount+=1"
)
if exist "CHANGELOG.md" (
    echo   ✅ CHANGELOG.md
    set /a "foundCount+=1"
) else (
    echo   ❌ CHANGELOG.md
    set /a "missingCount+=1"
)
echo.

REM Résumé
echo ╔════════════════════════════════════════════╗
if %missingCount% equ 0 (
    echo ║   ✅ RESTAURATION VALIDÉE!               ║
) else (
    echo ║   ⚠️  RESTAURATION INCOMPLÈTE            ║
)
echo ║                                            ║
echo ║   Fichiers trouvés: %foundCount%                       ║
echo ║   Fichiers manquants: %missingCount%                    ║
echo ╚════════════════════════════════════════════╝
echo.

if %missingCount% equ 0 (
    echo ✅ Tous les fichiers restaurés sont présents!
    echo.
    echo Prochaines étapes:
    echo   1. Compiler: mvn clean compile
    echo   2. Tester: mvn test
    echo   3. Lancer: mvn javafx:run
) else (
    echo ❌ Certains fichiers sont manquants!
    echo    Relancez la restauration.
)
echo.

pause

