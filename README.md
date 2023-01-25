# GCBenchmark
Just a simple benchmark to compare characteristics of various GCs
It consists of binary tree implementations in 4 different languages of 4 different runtime platforms: 

- C (native platform)

- C# (the Common Language Runtime)

- Java (the Java Virtual Machine)

- Golang (the Google Go runtime)


## C

Build with

Run with

## C#

Build with

Run with


## Java

Build with

    mvn package

Run with

    java -Xmx8000m -jar jvm/_bin/gcBenchmark.jar 27

where "27" is the height of the tree (at height = 27 it takes up about 7.4 GB of memory) and the Xmx setting should be adjusted as needed.


## Golang

Build with

Run with

