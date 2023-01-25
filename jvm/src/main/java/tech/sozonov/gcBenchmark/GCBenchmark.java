package tech.sozonov.gcBenchmark;
import tech.sozonov.gcBenchmark.implementations.Naive;
import tech.sozonov.gcBenchmark.implementations.WithRegion;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;

public class GCBenchmark {


public static void main(String[] args) {
    if (args.length == 0) return;
    try {
        final int treeHeight = Integer.parseInt(args[0]);
        run(treeHeight, "Reg 1", GCBenchmark::runWithRegions);


        var timeStart = Instant.now();
        Runtime.getRuntime().gc();
        var timeEnd = Instant.now();
        System.out.println("Full GC time = " + ChronoUnit.SECONDS.between(timeStart, timeEnd) + " s");

        run(treeHeight, "Reg 2", GCBenchmark::runWithRegions);

        timeStart = Instant.now();
        Runtime.getRuntime().gc();
        timeEnd = Instant.now();
        System.out.println("Full GC time = " + ChronoUnit.SECONDS.between(timeStart, timeEnd) + " s");

        run(treeHeight, "Reg 3", GCBenchmark::runWithRegions);


    } catch (Exception e) {
        System.out.println("Input error, you should input a tree height like 25");
    }
}


private static void run(int height, String designator, BiFunction<Integer, Instant, Integer> coreFun) {
    System.out.println("Processing tree with " + designator + "...");
    var timeStart = Instant.now();
    int result = coreFun.apply(height, timeStart);
    var timeEnd = Instant.now();
    System.out.println("Finished with result = " + result);
    System.out.println("Used time = " + ChronoUnit.SECONDS.between(timeStart, timeEnd) + " s");
    System.out.println();
}


private static int runWithNaive(int height, Instant tStart) {
    final var withNaive = new Naive(height);
    var runtime = Runtime.getRuntime();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Used memory = " + (memory / 1024L / 1024L) + " MB");
    System.out.println("Time for alloc = " + ChronoUnit.SECONDS.between(tStart, Instant.now()) + " s");
    return withNaive.processTree();
}

private static int runWithRegions(int height, Instant tStart) {
    final var withRegion = new WithRegion(height);
    var runtime = Runtime.getRuntime();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Used memory = " + (memory / 1024L / 1024L) + " MB");
    System.out.println("Time for alloc = " + ChronoUnit.SECONDS.between(tStart, Instant.now()) + " s");
    return withRegion.processTree();
}


}
