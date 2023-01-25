package main


import (
    "fmt"
    "os"
    "strconv"
    "time"
    "runtime"
)

const payloadSize = 4

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
    naive := NewNaive(height)

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

// ------------------------------ Naive GC ------------------------------

type Naive struct {
    sum int;
    theTree *Tree;
}

func NewNaive(height int) *Naive {
    result := new(Naive)
    result.sum = 0
    result.theTree = NewTree([]int{1, 2, -1, -1})
    return result
}

func NewTree(payload []int) *Tree {
    result := new(Tree)
    result.payload = payload
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

    newArr := make([]int, payloadSize)
    copy(newArr[:], payload)
    wholeTree := NewTree(newArr)
    currTree := wholeTree
    stack.Push(wholeTree)
    for i := 1; i < height; i++ {
        newArr = make([]int, payloadSize)
        copy(newArr[:], payload)
        newTree := NewTree(newArr)
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
    for i := 0; i < payloadSize; i++ {
        this.sum += currElem.payload[i]
    }
    for currElem.left != nil {
        currElem = currElem.left
        for i := 0; i < payloadSize; i++ {
            this.sum += currElem.payload[i]
        }
        stack.Push(currElem)
    }
}

type Tree struct {
    left *Tree;
    right *Tree;
    payload []int;
}

// ------------------------------ /Naive GC ------------------------------


// ------------------------------ Regions ------------------------------


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
