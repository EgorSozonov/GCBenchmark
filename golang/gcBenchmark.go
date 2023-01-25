package main


import (
    "fmt"
    "os"
    "strconv"
    "time"
    "runtime"
)


func main() {

    argsWithProg := os.Args
    if (len(argsWithProg) != 3) {
        fmt.Println("There must be exactly 2 arguments: the tree height (25 is a good starting value) then optionally 'regions'")
        return
    }

    height, err := strconv.Atoi(argsWithProg[1])
    if err != nil || height < 1 {
        fmt.Println("The tree height must be a positive integer (25 is a good starting value)")
        return
    }

    if argsWithProg[2] != "" && argsWithProg[2] != "regions" {
        fmt.Println("The second argument, if present, must be 'regions', not " + argsWithProg[2])
        return
    }

    designator := "Naive GC"
    coreFun := runNaive
    if argsWithProg[2] == "regions" {
        designator = "Regions"
    }
    run(height, designator, coreFun)

}

func run(height int, designator string, coreFun func(int, time.Time) int) {
    fmt.Println("Processing tree with ", designator, "...")
    timeStart := time.Now()
    result := coreFun(height, timeStart)
    timeEnd := time.Now()
    fmt.Println("Finished with result = ", result)
    fmt.Println("Used time = ", (timeEnd.Sub(timeStart).Seconds()), " s")
}

func runNaive(height int, tStart time.Time) int {
    naive := Naive.new(height)

    var m runtime.MemStats
    runtime.ReadMemStats(&m)
    // For info on each, see: https://golang.org/pkg/runtime/#MemStats
    fmt.Println("Used memory = ", bToMb(m.HeapAlloc), " MB")

    fmt.Println("Time for alloc = ", time.Now().Sub(tStart).Seconds(), " s")
    return naive.ProcessTree()
}

func bToMb(b uint64) uint64 {
    return b / 1024 / 1024
}


type Naive struct {
    sum int;
    theTree *Tree;
}

func (this *Naive) ProcessTree() int {
    return -1
}

type Tree struct {
    left *Tree;
    right *Tree;
    payload []int;
}
