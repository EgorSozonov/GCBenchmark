namespace GCBenchmark {
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


class Program {
    static void Main(string[] args) {

        if (args.Length < 1 || args.Length > 2) {
            Console.WriteLine("There must be 1 or 2 arguments: the tree height (25 is a good starting value) plus, optionally, the word \"regions\"");
            return;
        }
        if (!int.TryParse(args[0], out int height)) {
            Console.WriteLine("Error parsing the argument: it must be a positive integer");
            return;
        }
        if (args.Length == 2 && args[1] != "regions") {
            Console.WriteLine("Error: second argument, if present, must be \"regions\"!");
            return;
        }
        Console.WriteLine("Running on the CLR!");
        Console.WriteLine();
        if (args.Length == 2) {
            run(height, "Regions, 1st run", runWithGoodStack);
            System.GC.Collect();
            Console.WriteLine();
            run(height, "Regions, 2nd run", runWithGoodStack);
        } else {
            run(height, "Naive GC 1st run", runNaive);
            System.GC.Collect();
            Console.WriteLine();
            run(height, "Naive GC 2nd run", runNaive);
        }

    }

    public static void run(int height, string designator, Func<int, DateTime, int> coreFun) {
        Console.WriteLine($"Processing tree with {designator}...");
        var timeStart = DateTime.Now;

        var result = coreFun(height, timeStart);

        var timeEnd = DateTime.Now;

        Console.WriteLine("Finished with result = " + result);
        Console.WriteLine("Used time = " + (int)(timeEnd - timeStart).TotalMilliseconds + " ms");
    }


    public static int runNaive(int height, DateTime tStart) {
        var naive = new Naive(height);

        var memory = GC.GetTotalMemory(false);
        Console.WriteLine($"Used memory = {memory / 1024L / 1024L} MB");
        Console.WriteLine($"Time for alloc = {(int)(DateTime.Now - tStart).TotalMilliseconds} ms");

        return naive.processTree();
    }


    public static int runWithRegions(int height, DateTime tStart) {
        var withRegion = new WithRegions(height, tStart);

        //var memory = GC.GetTotalMemory(false);
        //Console.WriteLine($"Used memory = {memory / 1024L / 1024L} MB");
        Console.WriteLine($"Time for alloc = {(DateTime.Now - tStart).TotalMilliseconds} ms");

        return withRegion.processTree();
    }

    public static int runWithSmartRegions(int height, DateTime tStart) {
        var withRegion = new WithSmartRegions(height, tStart);

        var memory = GC.GetTotalMemory(false);
        Console.WriteLine($"Used memory = {memory / 1024L / 1024L} MB");
        Console.WriteLine($"Time for alloc = {(DateTime.Now - tStart).TotalMilliseconds} ms");

        return withRegion.processTree();
    }

    public static int runWithGoodStack(int height, DateTime tStart) {
        var withRegion = new RegionsGoodStack(height, tStart);

        var memory = GC.GetTotalMemory(false);
        Console.WriteLine($"Used memory = {memory / 1024L / 1024L} MB");
        Console.WriteLine($"Time for alloc = {(DateTime.Now - tStart).TotalMilliseconds} ms");

        return withRegion.processTree();
    }
}


}
