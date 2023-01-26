package tech.sozonov.gcBenchmark;
import tech.sozonov.gcBenchmark.implementations.Naive;
import tech.sozonov.gcBenchmark.implementations.WithRegion;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;

public class GCBenchmark {


public static void main(String[] args) {
    if (args.length < 1 || args.length > 2) return;
    try {
        final int treeHeight = Integer.parseInt(args[0]);
        if (args.length == 2 && !args[1].equals("regions")) {
            System.out.println("The optional second argument must be either 'regions' or absent!");
            return;
        }
        System.out.println("Running on the JVM!");
        System.out.println();
        if (args.length == 1) {
            run(treeHeight, "GC 1st run", GCBenchmark::runWithNaive);

            run(treeHeight, "GC 2nd run", GCBenchmark::runWithNaive);

            run(treeHeight, "GC 3rd run", GCBenchmark::runWithNaive);
        } else {
            run(treeHeight, "Regions 1st run", GCBenchmark::runWithRegions);

            run(treeHeight, "Regions 2nd run", GCBenchmark::runWithRegions);

            run(treeHeight, "Regions 3rd run", GCBenchmark::runWithRegions);
        }

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
    System.out.println("Used time = " + (int)ChronoUnit.MILLIS.between(timeStart, timeEnd) + " ms");
    System.out.println();
}


private static int runWithNaive(int height, Instant tStart) {
    final var withNaive = new Naive(height);
    var runtime = Runtime.getRuntime();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Used memory = " + (memory / 1024L / 1024L) + " MB");
    System.out.println("Time for alloc = " + (int)ChronoUnit.MILLIS.between(tStart, Instant.now()) + " ms");
    return withNaive.processTree();
}

private static int runWithRegions(int height, Instant tStart) {
    final var withRegion = new WithRegion(height);
    var runtime = Runtime.getRuntime();
    long memory = runtime.totalMemory() - runtime.freeMemory();
    System.out.println("Used memory = " + (memory / 1024L / 1024L) + " MB");
    System.out.println("Time for alloc = " + (int)ChronoUnit.MILLIS.between(tStart, Instant.now()) + " ms");
    return withRegion.processTree();
}


}
