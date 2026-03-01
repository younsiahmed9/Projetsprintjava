@echo off
cd "C:\Users\MSI\IdeaProjects\fintrack-gestion-documents"
mvn clean compile
echo.
echo Build exit code: %ERRORLEVEL%
pause

