package tech.sozonov.gcBenchmark;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GCBenchmark {

    public static void main(String[] args) {
        if (args.length == 0) return;
        try {
            final int treeHeight = Integer.parseInt(args[0]);
            run(treeHeight, "Naive GC", ::runWithNaive);
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
    }

    private static int runWithNaive(int height, Instant tStart) {
        
    }



}
