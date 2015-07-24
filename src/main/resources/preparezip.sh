#!/usr/bin/env bash

mkdir Segrada
chmod +x start.*
mv segrada-1.0-SNAPSHOT.jar lib/ src/ start.* Segrada/

zip -9r Segrada.zip Segrada/
tar czvf Segrada.tar.gz Segrada/

rm -rf Segrada classes generated-* maven-* surefire* test*