package tech.sozonov.gcBenchmark.implementations;
import java.util.Stack;

public class Naive {


private final Tree theTree;
public static final int payloadSize = 4;
public int sum;

public Naive(int height) {
    theTree = createTree(height, new int[] {1, 2, -1, -1});
}


private static Tree createTree(int height, int[] payload) {
    if (height <= 0) return null;
    var stack = new Stack<Tree>();
    var wholeTree = createLeftTree(height, payload, stack);
    while (!stack.isEmpty()) {
        var topElem = stack.peek();
        if (topElem.right != null || stack.size() == height) {
            stack.pop();
            while (!stack.isEmpty() && stack.peek().right != null) stack.pop();
        }
        if (!stack.isEmpty() && stack.size() < height) {
            topElem = stack.peek();
            topElem.right = createLeftTree( height - stack.size(), payload, stack);
        }
    }
    return wholeTree;
}


private static Tree createLeftTree(int height, int[] payload, Stack<Tree> stack) {
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
    final var stack = new Stack<Tree>();
    processLeftTree(theTree, stack);
    while (!stack.isEmpty()) {
        var topElem = stack.pop().right;
        if (topElem != null) processLeftTree(topElem, stack);
    }
    return this.sum;
}


protected void processLeftTree(Tree tree, Stack<Tree> stack) {
    Tree currElem = tree;
    if (currElem == null) return;
    stack.push(currElem);
    for (int i = 0; i < payloadSize; i++) {
        sum += currElem.payload[i];
    }
    while (currElem.left != null) {
        currElem = currElem.left;
        for (int i = 0; i < payloadSize; i++) {
            sum += currElem.payload[i];
        }
        stack.push(currElem);
    }
}


private static class Tree {
    public Tree left;
    public Tree right;
    public int[] payload;

    public Tree(Tree _left, Tree _right, int[] _payload) {
        left = _left;
        right = _right;
        payload = _payload;
    }
}


}
