#!/bin/sh

dir=`dirname "$0"`
cd "$dir"

if [ -n "$JAVA_HOME" ]; then
	$JAVA_HOME/bin/java -Xdock:name="Segrada" -jar ./segrada-1.0-SNAPSHOT.jar $*
else
	java -Xdock:name="Segrada" -jar ./segrada-1.0-SNAPSHOT.jar $*
fi
cd $OLDPWD