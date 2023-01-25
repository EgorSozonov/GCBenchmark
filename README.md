# GCBenchmark

Just a simple benchmark to compare the characteristics of various GCs under GC pressure.

It consists of binary tree implementations in 4 different languages of 4 different runtime platforms: 

- C (native platform)

- C# (the Common Language Runtime)

- Java (the Java Virtual Machi	ne)

- Golang (the Google Go runtime)



The "buildAll.sh" is included for convenience, it builds all 4 projects.

## C

Build with

    gcc -O3 c/GCBenchmark.c -o c/gcBenchmark -lm
    
Run with

    c/gcBenchmark 27
    

## C#

Build with

    dotnet build clr

Run with

    clr/_bin/gcBenchmark 27


## Java

Build with

    mvn package -f jvm/pom.xml

Run with

    java -Xmx8000m -jar jvm/_bin/gcBenchmark.jar 27

where "27" is the height of the tree (at height = 27 it takes up about 7.4 GB of memory) and the Xmx setting should be adjusted as needed.


## Golang

Build with

    go build -o golang/_bin/gcBenchmark golang/gcBenchmark.go
    
Run with

    golang/_bin/gcBenchmark 25



