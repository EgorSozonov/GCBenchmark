namespace GCBenchmark {
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


public class WithRegions {
    public int height;
    public const int ELTS_IN_REGION = 600000;
    public const int SIZE_PAYLOAD = 4;
    public const int SIZE_REGION = ELTS_IN_REGION;
    List<Node[]> regions;
    int indCurrRegion;
    Node[] currRegion;
    int indFree;

    public int sum;

    public WithRegions(int _height, DateTime tStart) {
        height = _height;
        var numRegions = (int)(Math.Pow(2.0, (double)this.height) - 1) / ELTS_IN_REGION + 1;
        regions = new List<Node[]>(numRegions);
        for (int i = 0; i < numRegions; ++i) {
            regions.Add(new Node[SIZE_REGION]);
        }
        Console.WriteLine($"Time for region alloc = {(DateTime.Now - tStart).TotalMilliseconds} ms");

        indCurrRegion = 0;
        currRegion = regions[0];
        indFree = 0;

        createTree(1, 2, -1, -1);
        sum = 0;
    }

    public void createTree(int _a, int _b, int _c, int _d) {
        if (height <= 0) return;
        var stack = new Stack<Loc>(height);
        var wholeTree = createLeftTree(height, _a, _b, _c, _d, stack);
        while (stack.Count > 0) {
            var bottomElement = stack.Peek();
            if (bottomElement.arr[bottomElement.ind].right > -1 || stack.Count == height) {
                stack.Pop();
                while (stack.Count > 0) {
                    bottomElement = stack.Peek();
                    if (bottomElement.arr[bottomElement.ind].right == -1) break;
                    stack.Pop();
                }
            }
            if (stack.Count > 0) {
                bottomElement = stack.Peek();
                //var nd = bottomElement.arr[bottomElement.ind];
                int newH = height - stack.Count;
                ref Node nd = ref bottomElement.arr[bottomElement.ind];
                nd.right = createLeftTree(newH, _a, _b, _c, _d, stack);
            }
        }
    }

    public int createLeftTree(int height, int _a, int _b, int _c, int _d, Stack<Loc> stack) {
        if (height == 0) return -1;

        var wholeTree = allocateNode(_a, _b, _c, _d);
        Loc currTree = toLoc(wholeTree);
        stack.Push(currTree);
        for (int i = 1; i < height; ++i) {
            var newTree = allocateNode(_a, _b, _c, _d);
            currTree.arr[currTree.ind].left = newTree;
            currTree = toLoc(newTree);
            stack.Push(currTree);
        }
        return wholeTree;
    }


    public int processTree() {
        if (indFree == 0) {
            Console.WriteLine("Oh noes, the tree is null!");
            return -1;
        } else {
            var stack = new Stack<Loc>(height);
            processLeftTree(toLoc(0), stack);
            while (stack.Count > 0) {
                var bottomElem = stack.Pop();
                var indRight = bottomElem.arr[bottomElem.ind].right;
                if (indRight > -1) processLeftTree(toLoc(indRight), stack);
            }
        }
        return sum;
    }

    public void processLeftTree(Loc root, Stack<Loc> stack) {
        stack.Push(root);
        ref Node rootN = ref root.arr[root.ind];
        sum += rootN.a;
        sum += rootN.b;
        sum += rootN.c;
        sum += rootN.d;

        var currLeft = root.arr[root.ind].left;
        while (currLeft > -1) {
            var currNode = toLoc(currLeft);
            ref Node nd = ref currNode.arr[root.ind];
            sum += nd.a;
            sum += nd.b;
            sum += nd.c;
            sum += nd.d;
            stack.Push(currNode);
            currLeft = currNode.arr[currNode.ind].left;
        }
    }

    public int allocateNode(int _a, int _b, int _c, int _d) {
        if (indFree == SIZE_REGION) {
            ++indCurrRegion;
            indFree = 0;
            if (indCurrRegion == regions.Count) {
                regions.Add(new Node[SIZE_REGION]);
            }
            currRegion = regions[indCurrRegion];
        }
        //initNode(ref currRegion[indFree], _a, _b, _c, _d);
        ref Node nd = ref currRegion[indFree];
        nd.left = -1;
        nd.right = -1;
        nd.a = _a;
        nd.b = _b;
        nd.c = _c;
        nd.d = _d;
        ++indFree;

        return indCurrRegion * SIZE_REGION + indFree - 1;
    }

    public static void initNode(ref Node nd, int _a, int _b, int _c, int _d) {
        nd.left = -1;
        nd.right = -1;
        nd.a = _a;
        nd.b = _b;
        nd.c = _c;
        nd.d = _d;
    }

    public Loc toLoc(int ind) {
        var numRegion = ind / SIZE_REGION;
        var offset = ind % SIZE_REGION;
        return new Loc { arr = regions[numRegion], ind = offset };
    }

    public void updateLoc(Loc loc, int ind) {
        var numRegion = ind / SIZE_REGION;
        var offset = ind % SIZE_REGION;
        loc.arr = regions[numRegion];
        loc.ind = offset;
    }

    public struct Node {
        public int left;
        public int right;
        public int a;
        public int b;
        public int c;
        public int d;

        public void setLeft(int newVal) {
            left = newVal;
        }

        public void setRight(int newVal) {
            right = newVal;
        }
    }

    public sealed class Loc {
        public Node[] arr;
        public int ind;
    }
}

}
