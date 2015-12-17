#!/usr/bin/env bash

~/bin/launch4j/launch4j launch4j.xml

mkdir Segrada
chmod +x start.* start_mac.command
mv segrada-1.0-SNAPSHOT.jar lib/ src/ start* start_mac.command *.exe Segrada/

zip -9r Segrada.zip Segrada/
tar czvf Segrada.tgz Segrada/

rm -rf Segrada
