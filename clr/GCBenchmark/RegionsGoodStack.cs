namespace GCBenchmark {
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


public class RegionsGoodStack {
    public int height;
    public const int ELTS_IN_REGION = 600000;
    public const int SIZE_PAYLOAD = 4;
    public const int SIZE_REGION = ELTS_IN_REGION;
    List<Node[]> regions;
    int indCurrRegion;
    Node[] currRegion;
    int indFree;

    public int sum;

    public RegionsGoodStack(int _height, DateTime tStart) {
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
        var stack = new SpineStack(height + 1);
        var wholeTree = createLeftTree(height, _a, _b, _c, _d, stack);
        while (stack.count > 0) {
            var bottomElement = stack.peek(out int bottomInd);
            if (bottomElement[bottomInd].right > -1 || stack.count == height) {
                stack.throwAway();
                while (stack.count > 0) {
                    bottomElement = stack.peek(out int bottomInd1);
                    if (bottomElement[bottomInd1].right == -1) break;
                    stack.throwAway();
                }
            }
            if (stack.count > 0) {
                bottomElement = stack.peek(out int bottomInd1);
                //var nd = bottomElement.arr[bottomElement.ind];
                int newH = height - stack.count;
                ref Node nd = ref bottomElement[bottomInd1];
                nd.right = createLeftTree(newH, _a, _b, _c, _d, stack);
            }
        }
    }

    public int createLeftTree(int height, int _a, int _b, int _c, int _d, SpineStack stack) {
        if (height == 0) return -1;

        var wholeTree = allocateNode(_a, _b, _c, _d);
        Node[] currArr = regions[wholeTree / SIZE_REGION];
        int currInd = wholeTree % SIZE_REGION;
        stack.push(currArr, currInd);
        for (int i = 1; i < height; ++i) {
            var newTree = allocateNode(_a, _b, _c, _d);
            currArr[currInd].left = newTree;

            currArr = regions[newTree / SIZE_REGION];
            currInd = newTree % SIZE_REGION;
            stack.push(currArr, currInd);
        }
        return wholeTree;
    }


    public int processTree() {
        if (indFree == 0) {
            Console.WriteLine("Oh noes, the tree is null!");
            return -1;
        } else {
            var stack = new SpineStack(height);
            processLeftTree(regions[0], 0, stack);
            while (stack.count > 0) {
                var bottomElem = stack.pop(out int bottomInd);
                var indRight = bottomElem[bottomInd].right;
                var arr = regions[indRight/SIZE_REGION];
                if (indRight > -1) processLeftTree(arr, indRight%SIZE_REGION, stack);
            }
        }
        return sum;
    }

    public void processLeftTree(Node[] arr, int ind, SpineStack stack) {
        stack.push(arr, ind);
        ref Node rootN = ref arr[ind];
        sum += rootN.a;
        sum += rootN.b;
        sum += rootN.c;
        sum += rootN.d;

        var currLeft = arr[ind].left;
        while (currLeft > -1) {
            var currNode = toLoc(currLeft);
            ref Node nd = ref currNode.arr[ind];
            sum += nd.a;
            sum += nd.b;
            sum += nd.c;
            sum += nd.d;
            stack.push(currNode.arr, currNode.ind);
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

    public struct Loc {
        public Node[] arr;
        public int ind;
    }

    public class SpineStack {
        protected Loc[] arr;
        public int count;
        public SpineStack(int length) {
            arr = new Loc[length];
        }

        public void push(Node[] _arr, int _ind) {
            ref Loc ptr = ref arr[count];
            ptr.arr = _arr;
            ptr.ind = _ind;
            ++count;
        }

        public Node[] peek(out int _ind) {
            int c = count - 1;
            _ind = arr[c].ind;
            return arr[c].arr;
        }

        public Node[] pop(out int _ind) {
            --count;
            _ind = arr[count].ind;
            return arr[count].arr;
        }

        public void throwAway() {
            --count;
        }
    }
}


}
