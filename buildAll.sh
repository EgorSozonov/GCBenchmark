#!/usr/bin/bash
gcc -O3 c/GCBenchmark.c -o c/gcBenchmark -lm

dotnet build clr

mvn package -f jvm/pom.xml

go build -o golang/_bin/gcBenchmark golang/gcBenchmark.go
