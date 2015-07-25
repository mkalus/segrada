#!/usr/bin/env bash

~/bin/launch4j/launch4j launch4j.xml

mkdir Segrada
chmod +x start.*
mv segrada-1.0-SNAPSHOT.jar lib/ src/ start.* *.exe Segrada/

zip -9r Segrada.zip Segrada/
#tar czvf Segrada.tar.gz Segrada/

rm -rf Segrada