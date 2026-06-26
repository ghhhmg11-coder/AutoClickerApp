#!/bin/sh
  #
  # Copyright © 2015-2021 the original authors.
  # Gradle wrapper script for Unix/macOS/Linux
  #

  APP_NAME="Gradle"
  APP_BASE_NAME=`basename "$0"`
  APP_HOME=`pwd -P`

  MAX_FD="maximum"
  warn () { echo "$*"; }
  die () { echo; echo "$*"; echo; exit 1; }

  OS=`uname`

  # Attempt to set APP_HOME
  cd "$APP_HOME/.." || die "Working dir not set."
  APP_HOME=`pwd -P`

  CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

  JAVA_EXE=java
  which java > /dev/null 2>&1 || die "ERROR: JAVA_HOME not set and 'java' not found."

  exec "$JAVA_EXE" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
  