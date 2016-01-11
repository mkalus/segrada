#!/usr/bin/env bash
# Linux start script

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd "$DIR"

if [ -n "$JAVA_HOME" ]; then
  JAVARUN="$JAVA_HOME/bin/java"
else
  JAVARUN="java"
fi

$JAVARUN -jar ./segrada-1.0-SNAPSHOT.jar "$@"
cd $OLDPWD