namespace GCBenchmark {
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


class Program {
    static void Main(string[] args) {
        if (args.Length != 1) {
            Console.WriteLine("There must be exactly 1 argument: the tree height (25 is a good starting value)");
            return;
        }
        if (!int.TryParse(args[0], out int height)) {
            Console.WriteLine("Error parsing the argument: it must be a positive integer");
            return;
        }
        run(height, "Regions and a good stack", runWithGoodStack);
    }

    public static void run(int height, string designator, Func<int, DateTime, int> coreFun) {
        Console.WriteLine($"Processing tree with {designator}...");
        var timeStart = DateTime.Now;

        var result = coreFun(height, timeStart);

        var timeEnd = DateTime.Now;

        Console.WriteLine("Finished with result = " + result);
        Console.WriteLine("Used time = " + (timeEnd - timeStart).TotalMilliseconds + " ms");
    }


    public static int runNaive(int height, DateTime tStart) {
        var naive = new Naive(height);

        var memory = GC.GetTotalMemory(false);
        Console.WriteLine($"Used memory = {memory / 1024L / 1024L} MB");
        Console.WriteLine($"Time for alloc = {(DateTime.Now - tStart).TotalMilliseconds} ms");

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
