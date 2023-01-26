package tech.sozonov.gcBenchmark.implementations;
import java.util.Stack;

public class Naive {


private final Tree theTree;
public static final int payloadSize = 4;
public int sum;
public final int height;

public Naive(int height) {
    theTree = createTree(height, new int[] {1, 2, -1, -1});
    this.height = height;
}


private static Tree createTree(int height, int[] payload) {
    if (height <= 0) return null;
    var stack = new ArrayStack(height);
    var wholeTree = createLeftTree(height, payload, stack);
    while (stack.isNotEmpty()) {
        var topElem = stack.peek();
        if (topElem.right != null || stack.length() == height) {
            stack.pop();
            while (stack.isNotEmpty() && stack.peek().right != null) stack.pop();
        }
        if (stack.isNotEmpty() && stack.length() < height) {
            topElem = stack.peek();
            topElem.right = createLeftTree( height - stack.length(), payload, stack);
        }
    }
    return wholeTree;
}


private static Tree createLeftTree(int height, int[] payload, ArrayStack stack) {
    if (height == 0) return null;

    var newArr = payload.clone();
    final Tree wholeTree = new Tree(null, null, newArr);
    var currTree = wholeTree;
    stack.push(wholeTree);
    for (int i = 1; i < height; i++) {
        newArr = payload.clone();
        final Tree newTree = new Tree(null, null, newArr);
        currTree.left = newTree;
        currTree = newTree;
        stack.push(currTree);
    }
    return wholeTree;
}


public int processTree() {
    if (theTree == null) {
        System.out.println("Blimey, the tree doth equal null!");
        return -1;
    }
    final var stack = new ArrayStack(height);
    processLeftTree(theTree, stack);
    while (stack.isNotEmpty()) {
        var topElem = stack.pop().right;
        if (topElem != null) processLeftTree(topElem, stack);
    }
    return this.sum;
}


protected void processLeftTree(Tree tree, ArrayStack stack) {
    Tree currElem = tree;
    if (currElem == null) return;
    stack.push(currElem);
    sum += currElem.payload1 + currElem.payload2 + currElem.payload3 + currElem.payload4;
    while (currElem.left != null) {
        currElem = currElem.left;
        sum += currElem.payload1 + currElem.payload2 + currElem.payload3 + currElem.payload4;
        stack.push(currElem);
    }
}


private static class Tree {
    public Tree left;
    public Tree right;
    public int payload1;
    public int payload2;
    public int payload3;
    public int payload4;

    public Tree(Tree _left, Tree _right, int[] _payload) {
        left = _left;
        right = _right;
        payload1 = _payload[0];
        payload2 = _payload[1];
        payload3 = _payload[2];
        payload4 = _payload[3];
    }
}

protected static final class ArrayStack {
    private final Tree[] content;
    private int ind;

    public ArrayStack(int height) {
        content = new Tree[height];
        ind = 0;
    }

    public void push(Tree newTree) {
        content[ind] = newTree;
        ind++;
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

    public boolean isNotEmpty() {
        return ind > 0;
    }

    public int length() {
        return ind;
    }
}


}
