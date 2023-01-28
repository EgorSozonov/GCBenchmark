namespace GCBenchmark {
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


public sealed class Naive {
    public int height;
    public int sum = 0;
    public Tree theTree = null;

    public Naive(int _height) {
        height = _height;
        theTree = createTree(height, new[] { 1, 2, -1, -1 });
    }

    public static Tree createTree(int height, int[] payload) {
        if (height <= 0) return null;

        var stack = new ArrayStack(height);
        var wholeTree = createLeftTree(height, payload, stack);
        while (stack.isNotEmpty()) {
            var topElement = stack.peek();
            if (topElement.right != null || stack.length() == height) {
                stack.pop();
                while (stack.length() > 0 && stack.peek().right != null) stack.pop();
            }
            if (stack.isNotEmpty() && stack.length() < height) {
                topElement = stack.peek();
                topElement.right = createLeftTree(height - stack.length(), payload, stack);
            }
        }
        return wholeTree;
    }


    // Populate the tree. Allocates lots of objects for the GC to waste time on.
    public static Tree createLeftTree(int height, int[] payload, ArrayStack stack) {
        if (height == 0) return null;

        var wholeTree = new Tree { payload1 = payload[0], payload2 = payload[1], payload3 = payload[2], payload4 = payload[3] };
        var currTree = wholeTree;
        stack.push(wholeTree);
        for (int i = 1; i < height; ++i) {
            var newTree = new Tree { payload1 = payload[0], payload2 = payload[1], payload3 = payload[2], payload4 = payload[3] };
            currTree.left = newTree;
            currTree = newTree;
            stack.push(currTree);
        }
        return wholeTree;
    }


    public int processTree() {
        if (theTree == null) {
            Console.WriteLine("Oh blimey, why is the tree null!");
            return -1;
        } else {
            var stack = new ArrayStack(height);
            processLeftTree(theTree, stack);
            while (stack.isNotEmpty()) {
                var bottomElem = stack.pop().right;
                if (bottomElem != null) processLeftTree(bottomElem, stack);
            }
        }
        return sum;
    }


    public void processLeftTree(Tree tree, ArrayStack stack) {
        Tree currElem = tree;
        if (currElem != null) {
            stack.push(currElem);
            sum += currElem.payload1 + currElem.payload2 + currElem.payload3 + currElem.payload4;
            while (currElem?.left != null) {
                currElem = currElem.left;
                if (currElem != null) {
                    sum += currElem.payload1 + currElem.payload2 + currElem.payload3 + currElem.payload4;
                    stack.push(currElem);
                }
            }
        }
    }
}


public sealed class Tree {
    public Tree left = null;
    public Tree right = null;
    public int payload1;
    public int payload2;
    public int payload3;
    public int payload4;
}


public sealed class ArrayStack {
    private Tree[] content;
    private int ind;

    public ArrayStack(int height) {
        content = new Tree[height];
        ind = 0;
    }

    public void push(Tree newTree) {
        content[ind] = newTree;
        ind++;
    }

    public bool isNotEmpty() {
        return ind > 0;
    }

    public int length() {
        return ind;
    }

    public Tree pop() {
        if (ind == 0) return null;
        ind--;
        return content[ind];
    }

    public Tree peek() {
        if (ind == 0) return null;
        return content[ind - 1];
    }
}

}
