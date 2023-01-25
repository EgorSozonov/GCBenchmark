#!/usr/bin/bash
c/gcBenchmark 27

clr/_bin/gcBenchmark 27

java -Xmx8000m -jar jvm/_bin/gcBenchmark.jar 27

golang/_bin/gcBenchmark 27

golang/_bin/gcBenchmark 27 regions
