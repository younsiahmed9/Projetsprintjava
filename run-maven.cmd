@echo off
setlocal

rem Safe Maven launcher for this project (Windows)
rem - Forces a real JDK java.exe (avoids Oracle javapath stub)
rem - Works with paths containing spaces

rem If JAVA_HOME is not set, or invalid, try common JDK locations.
if defined JAVA_HOME (
  if not exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA_HOME="
  )
)

if not defined JAVA_HOME (
  for %%J in (
    "C:\Program Files\Java\jdk-21"
    "C:\Program Files\Java\jdk-21.0.4"
    "C:\Program Files\Eclipse Adoptium\jdk-21*"
    "C:\Program Files\Eclipse Adoptium\jdk-17*"
    "C:\Users\HP\Downloads\jdk-21_windows-x64_bin\jdk-21*"
  ) do (
    if exist %%~J\bin\java.exe (
      set "JAVA_HOME=%%~J"
      goto :java_found
    )
  )
)

:java_found
if not defined JAVA_HOME (
  echo [run-maven] JAVA_HOME is not set or invalid.
  echo [run-maven] Please set JAVA_HOME to a JDK folder that contains bin\java.exe
  echo [run-maven] Example: set "JAVA_HOME=C:\Program Files\Java\jdk-21"
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

rem Tip: You can pass goals like: javafx:run, clean test, etc.
call "%~dp0mvnw.cmd" %*