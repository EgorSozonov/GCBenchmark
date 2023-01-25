#!/usr/bin/bash
gcc -O3 c/GCBenchmark.c -o c/gcBenchmark -lm
mvn package -f jvm/pom.xml
