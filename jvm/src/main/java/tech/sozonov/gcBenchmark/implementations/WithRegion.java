package tech.sozonov.gcBenchmark.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class WithRegion {


public static final int eltsInRegion = 200000;
public static final int sizeRegion = 6*eltsInRegion;
public static final int sizePayload = 4;

protected List<int[]> regions;
protected int currRegion;
protected int indFree;
protected int sum;
protected int height;

public WithRegion(int height) {
    this.height = height;
    this.regions = new ArrayList<>();
    int numRegions = ((int)(Math.pow(2.0, (double)height) - 1.0))/eltsInRegion + 1;
    System.out.println("Number of regions = " + numRegions + ", size of one region = " + sizeRegion + " 32-bit elements");
    for (int i = 0; i < numRegions; i++) {
        regions.add(new int[sizeRegion]);
    }
    currRegion = 0;
    indFree = 0;
    createTree(new int[] {1, 2, -1, -1});
}

protected static class Loc {
    public int[] arr;
    public int ind;
    public Loc(int[] _arr, int _ind) {
        arr = _arr;
        ind = _ind;
    }
}
protected void createTree(int[] payload) {
    if (this.height <= 0) return;
    var stack = new Stack<Loc>();
    final int wholeTree = createLeftTree(height, payload, stack);
    while (!stack.isEmpty()) {
        var topElem = stack.peek();
        if (topElem.arr[topElem.ind + 1] > -1 || stack.size() == height) {
            stack.pop();
            while (!stack.isEmpty()) {
                topElem = stack.peek();
                if (topElem.arr[topElem.ind + 1] == -1) break;
                stack.pop();
            }
        }
        if (!stack.isEmpty()) {
            topElem = stack.peek();
            topElem.arr[topElem.ind + 1] = createLeftTree(height - stack.size(), payload, stack);
        }
    }
}

protected int createLeftTree(int height, int[] payload, Stack<Loc> stack) {
    if (height == 0) return -1;
    final int wholeTree = allocateNode(payload);
    var currTree = toLoc(wholeTree);
    stack.push(currTree);
    for (int i = 1; i < height; i++) {
        final int newTree = allocateNode(payload);
        currTree.arr[currTree.ind] = newTree;
        currTree = toLoc(newTree);
        stack.push(currTree);
    }
    return wholeTree;
}

public int processTree() {
    if (indFree == 0) {
        System.out.println("Blimey, the tree is null or something!");
        return -1;
    }
    var stack = new Stack<Loc>();
    processLeftTree(toLoc(0), stack);
    while (!stack.isEmpty()) {
        var topElem = stack.pop();
        int indRight = topElem.arr[topElem.ind + 1];
        if (indRight > -1) processLeftTree(toLoc(indRight), stack);
    }
    return this.sum;
}

protected void processLeftTree(Loc root, Stack<Loc> stack) {
    stack.push(root);
    for (int i = (root.ind + 2); i <= (root.ind + sizePayload + 1); i++) {
        sum += root.arr[i];
    }
    var currLeft = root.arr[root.ind];
    while (currLeft > -1) {
        var currNode = toLoc(currLeft);
        for (int i = (currNode.ind + 2); i <= (currNode.ind + sizePayload + 1); i++) {
            sum += currNode.arr[i];
        }
        stack.push(currNode);
        currLeft = currNode.arr[currNode.ind];
    }
}

protected Loc toLoc(int ind) {
    final int numRegion = ind/sizeRegion;
    final int offset = ind % sizeRegion;
    return new Loc(regions.get(numRegion), offset);
}

protected int allocateNode(int[] payload) {
    if (indFree == sizeRegion) {
        ++currRegion;
        indFree = 0;
        if (currRegion == regions.size()) {
            regions.add(new int[sizeRegion]);
        }
    }
    final var region = regions.get(currRegion);
    final int result = currRegion*sizeRegion + indFree;
    region[indFree] = -1;
    region[indFree + 1] = -1;
    indFree += 2;
    for (int pl : payload) {
        region[indFree] = pl;
        indFree++;
    }
    return result;
}

public Loc getValue(int ind) throws Exception {
    if (ind < 0) throw new Exception("Region index must be non-negative, not " + ind);
    final int numRegion = ind/sizeRegion;
    int offset = ind % sizeRegion;
    if (numRegion >= regions.size()) throw new Exception("Nonexistent region " + numRegion);
    return new Loc(regions.get(numRegion), offset);
}

public void setValue(int ind, int newValue) {
    if (ind < 0) return;
    final int numRegion = ind / sizeRegion;
    final int offset = ind % sizeRegion;
    if (numRegion >= regions.size()) return;
    regions.get(numRegion)[offset] = newValue;
}


}
