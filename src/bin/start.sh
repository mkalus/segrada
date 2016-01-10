#!/usr/bin/env bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd "$DIR"

if [ -n "$JAVA_HOME" ]; then
  $JAVA_HOME/bin/java -jar ./segrada-1.0-SNAPSHOT.jar "$@"
else
  java -jar ./segrada-1.0-SNAPSHOT.jar "$@"
fi
cd $OLDPWD