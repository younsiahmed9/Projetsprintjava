@echo off
rem ------------------------------------------------------------------------------
rem Maven Wrapper startup batch script, version 3.3.2
rem ------------------------------------------------------------------------------
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR%"=="" set "MAVEN_PROJECTBASEDIR=."

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
  echo.
  echo Maven wrapper jar not found at "%WRAPPER_JAR%".
  echo Please check your project is correct.
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

set "MAVEN_OPTS=-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%"

"%JAVA_EXE%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
