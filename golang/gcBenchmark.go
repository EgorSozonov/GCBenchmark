package main


import (
    "fmt"
    "os"
    "strconv"
    "time"
    "runtime"
    "math"
)

const payloadSize = 4

func main() {
    argsWithProg := os.Args
    if (len(argsWithProg) < 2 || len(argsWithProg) > 3) {
        fmt.Println("There must be 1 or 2 arguments: the tree height (25 is a good starting value) then optionally 'regions'")
        return
    }

    height, err := strconv.Atoi(argsWithProg[1])
    if err != nil || height < 1 {
        fmt.Println("The tree height must be a positive integer (25 is a good starting value)")
        return
    }

    fmt.Println("Rob Pike give me wind, I'm sailing on the Golang ship!")
    fmt.Println()

    runRegions := false
    if len(argsWithProg) == 3 {
        if argsWithProg[2] != "regions" {
            fmt.Println("The second argument, if present, must be 'regions', not " + argsWithProg[2])
            return
        }
        runRegions = true
    }

    designator := "Naive GC"
    coreFun := runNaive
    if runRegions {
        designator = "Regions"
    }
    run(height, designator + " 1st run", coreFun)
    fmt.Println()
    run(height, designator + " 2nd run", coreFun)

    fmt.Println()
    var m runtime.MemStats
    runtime.ReadMemStats(&m)

    fmt.Println("Used memory = ", bToMb(m.HeapAlloc), " MB")
    fmt.Println("GC cycles = ", m.NumGC, ", count of freed objects = ", m.Frees)

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
    naive := NewNaive(height)

    var m runtime.MemStats
    runtime.ReadMemStats(&m)
    // For info on other memory statistics, see: https://golang.org/pkg/runtime/#MemStats
    fmt.Println("Used memory = ", bToMb(m.HeapAlloc), " MB")
    fmt.Println("GC cycles = ", m.NumGC, ", count of freed objects = ", m.Frees)

    fmt.Println("Time for alloc = ", time.Now().Sub(tStart).Seconds(), " s")
    return naive.ProcessTree()
}

func bToMb(b uint64) uint64 {
    return b / 1024 / 1024
}


// ------------------------------ Naive GC ------------------------------

type Naive struct {
    sum int;
    theTree *Tree;
}

func NewNaive(height int) *Naive {
    result := new(Naive)
    result.sum = 0
    result.theTree = createTree(height, []int{1, 2, -1, -1})
    return result
}

func NewTree(payload []int) *Tree {
    result := new(Tree)
    result.payload1 = payload[0]
    result.payload2 = payload[1]
    result.payload3 = payload[2]
    result.payload4 = payload[3]
    return result
}

func createTree(height int, payload []int) *Tree {
    if height <= 0 { return nil }
    stack := new(Stack[Tree])
    wholeTree := createLeftTree(height, payload, stack)
    for stack.IsNotEmpty() {
        topElem := stack.Peek()
        if topElem.right != nil || stack.Len() == height {
            stack.Pop()
            for stack.IsNotEmpty() && stack.Peek().right != nil {
                stack.Pop()
            }
        }
        if stack.IsNotEmpty() && stack.Len() < height {
            topElem = stack.Peek()
            topElem.right = createLeftTree(height - stack.Len(), payload, stack)
        }
    }
    return wholeTree
}

func createLeftTree(height int, payload []int, stack *Stack[Tree]) *Tree {
    if height == 0 { return nil }

    wholeTree := NewTree(payload)
    currTree := wholeTree
    stack.Push(wholeTree)
    for i := 1; i < height; i++ {

        newTree := NewTree(payload)
        currTree.left = newTree
        currTree = newTree
        stack.Push(currTree)
    }
    return wholeTree
}

func (this *Naive) ProcessTree() int {
    if this.theTree == nil {
        fmt.Println("Blimey, the tree doth equal null!")
        return -1
    }
    stack := new(Stack[Tree])
    this.processLeftTree(this.theTree, stack)
    for stack.IsNotEmpty() {
        topElem := stack.Pop().right
        if topElem != nil {
            this.processLeftTree(topElem, stack)
        }
    }
    return this.sum
}

func (this *Naive) processLeftTree(tree *Tree, stack *Stack[Tree]) {
    currElem := tree
    if currElem == nil { return }
    stack.Push(currElem)
    this.sum += (currElem.payload1 + currElem.payload2 + currElem.payload3 + currElem.payload4)

    for currElem.left != nil {
        currElem = currElem.left
        this.sum += (currElem.payload1 + currElem.payload2 + currElem.payload3 + currElem.payload4)
        stack.Push(currElem)
    }
}

type Tree struct {
    left *Tree;
    right *Tree;
    payload1 int;
    payload2 int;
    payload3 int;
    payload4 int;
}

// ------------------------------ /Naive GC ------------------------------


// ------------------------------ Regions ------------------------------

const eltsInRegion = 200000
const sizeRegion = 6*eltsInRegion
const sizePayload = 4

type WithRegion struct {
    regions [][sizeRegion]int;
    currRegion int;
    indFree int;
    sum int;
    height int;
}

func NewWithRegion(height int) *WithRegion {
    result := new(WithRegion)
    result.height = height
    numRegions := int((math.Pow(2.0, float64(height)) - 1)/eltsInRegion + 1)
    result.regions = make([][sizeRegion]int, numRegions)
    result.currRegion = 0
    result.indFree = 0
    result.createTree([]int {1, 2, -1, -1})
    return result
}

func (this *WithRegion) createTree(payload []int) {
    if this.height <= 0 { return }
    stack := new(Stack[Loc])
    this.createLeftTree(this.height, payload, stack)
    for stack.IsNotEmpty() {
        topElem := stack.Peek()
        if topElem.arr[topElem.ind + 1] > -1 || stack.Len() == this.height {
            stack.Pop()
            for stack.IsNotEmpty() {
                topElem = stack.Peek()
                if (topElem.arr[topElem.ind + 1] == -1) { break }
                stack.Pop()
            }
        }
        if (stack.IsNotEmpty()) {
            topElem = stack.Peek()
            topElem.arr[topElem.ind + 1] = this.createLeftTree(this.height - stack.Len(), payload, stack)
        }
    }
}

func (this *WithRegion) createLeftTree(height int, payload []int, stack *Stack[Loc]) int {
    if height == 0 { return -1 }
    wholeTree := this.allocateNode(payload)
    currTree := this.toLoc(wholeTree)
    stack.Push(&currTree)
    for i := 1; i < height; i++ {
        newTree := this.allocateNode(payload)
        currTree.arr[currTree.ind] = newTree
        currTree = this.toLoc(newTree)
        stack.Push(&currTree)
    }
    return wholeTree
}

func (this *WithRegion) ProcessTree() int {
    if this.indFree == 0 {
        fmt.Println("Blimey, the tree is null or something!")
        return -1
    }
    stack := new(Stack[Loc])
    this.processLeftTree(this.toLoc(0), stack)
    for stack.IsNotEmpty() {
        topElem := stack.Pop()
        indRight := topElem.arr[topElem.ind + 1]
        if indRight > -1 {
            this.processLeftTree(this.toLoc(indRight), stack)
        }
    }
    return this.sum
}

func (this *WithRegion) processLeftTree(root Loc, stack *Stack[Loc]) {
    stack.Push(&root)
    for i := root.ind + 2; i <= (root.ind + sizePayload + 1); i++ {
        this.sum += root.arr[i]
    }
    currLeft := root.arr[root.ind]
    for currLeft > -1 {
        currNode := this.toLoc(currLeft)
        for i := currNode.ind + 2; i <= (currNode.ind + sizePayload + 1); i++ {
            this.sum += currNode.arr[i]
        }
        stack.Push(&currNode)
        currLeft = currNode.arr[currNode.ind]
    }
}

func (this *WithRegion) allocateNode(payload []int) int {
    if this.indFree == sizeRegion {
        this.currRegion++
        this.indFree = 0
        this.regions[this.currRegion] = [sizeRegion]int{}
    }
    region := this.regions[this.currRegion]
    result := this.currRegion*sizeRegion + this.indFree
    region[this.indFree] = -1
    region[this.indFree + 1] = -1
    this.indFree += 2
    for pl := range payload {
        region[this.indFree] = pl
        this.indFree++
    }
    return result
}

func (this *WithRegion) toLoc(ind int) Loc {
    numRegion := ind/sizeRegion
    offset := ind % sizeRegion
    return Loc{ arr: this.regions[numRegion], ind: offset }
}

type Loc struct {
    arr [sizeRegion]int;
    ind int;
}

// ------------------------------ /Regions ------------------------------


// ------------------------------ Stack ------------------------------

type Stack[T any] []*T;

func (s *Stack[T]) IsNotEmpty() bool {
    return len(*s) > 0
}

func (s *Stack[T]) Push(item *T) {
    *s = append(*s, item)
}

func (s *Stack[T]) Pop() *T {
    if s.IsNotEmpty() {
        index := len(*s) - 1
        element := (*s)[index]
        *s = (*s)[:index]
        return element
    } else {
        return nil
    }
}

func (s *Stack[T]) Len() int {
    return len(*s)
}

func (s *Stack[T]) Peek() *T {
    if s.IsNotEmpty() {
        index := len(*s) - 1
        element := (*s)[index]
        return element
    } else {
        return nil
    }
}

// ------------------------------ /Stack ------------------------------

