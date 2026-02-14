@echo off
setlocal

REM Lightweight Maven wrapper for Windows when mvn isn't on PATH.
REM It delegates to the Maven bundled with IntelliJ IDEA if available.

set "MAVEN_BIN="

REM Try common IntelliJ Maven locations (Ultimate/Community).
for %%D in (
  "%ProgramFiles%\JetBrains"
  "%ProgramFiles(x86)%\JetBrains"
) do (
  if exist "%%~D" (
    for /d %%I in ("%%~D\IntelliJ IDEA*" "%%~D\IntelliJ IDEA Community Edition*") do (
      if exist "%%~I\plugins\maven\lib\maven3\bin\mvn.cmd" (
        set "MAVEN_BIN=%%~I\plugins\maven\lib\maven3\bin\mvn.cmd"
      )
    )
  )
)

if not defined MAVEN_BIN (
echo mvnw.cmd: Maven not found. ^
echo - Option 1: Run from IntelliJ using a Maven Run Configuration: clean javafx:run ^
echo - Option 2: Install Apache Maven and ensure mvn is on PATH ^
echo - Option 3: Edit mvnw.cmd to point to your mvn.cmd ^
exit /b 1
)

call "%MAVEN_BIN%" %*

