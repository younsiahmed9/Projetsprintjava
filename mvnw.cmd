@echo off
rem ------------------------------------------------------------------------------
rem Maven Wrapper startup batch script (Windows)
rem ------------------------------------------------------------------------------
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR%"=="" set "MAVEN_PROJECTBASEDIR=."

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
  echo Maven wrapper jar not found at "%WRAPPER_JAR%".
  exit /b 1
)

if not defined JAVA_HOME (
  echo JAVA_HOME is not set.
  exit /b 1
)

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXE%" (
  echo java.exe not found under JAVA_HOME: "%JAVA_EXE%"
  exit /b 1
)

"%JAVA_EXE%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*