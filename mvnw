#!/bin/sh

MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Maven wrapper jar not found. Downloading..."
  (cd "$MAVEN_PROJECTBASEDIR/.mvn/wrapper" && javac MavenWrapperDownloader.java && java -cp . MavenWrapperDownloader)
fi

if [ -z "$JAVA_HOME" ]; then
  echo "JAVA_HOME is not set. Please set JAVA_HOME to your JDK path." >&2
  exit 1
fi

exec "$JAVA_HOME/bin/java" -classpath "$WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
