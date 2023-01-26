package tech.sozonov.gcBenchmark.implementations;
import java.util.ArrayList;
import java.util.List;

public final class WithRegion {


public static final int eltsInRegion = 200000;
public static final int sizeRegion = 6*eltsInRegion;
public static final int sizePayload = 4;

private final List<int[]> regions;
private int currRegion;
private int indFree;
private int sum;
private final int height;

public WithRegion(int height) {
    this.height = height;
    this.regions = new ArrayList<>();
    int numRegions = ((int)(Math.pow(2.0, height) - 1.0))/eltsInRegion + 1;
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
private void createTree(int[] payload) {
    if (this.height <= 0) return;
    var stack = new ArrayStack(this.height);
    final int wholeTree = createLeftTree(height, payload, stack);
    while (stack.isNotEmpty()) {
        var topElem = stack.peek();
        if (topElem.arr[topElem.ind + 1] > -1 || stack.length() == height) {
            stack.pop();
            while (stack.isNotEmpty()) {
                topElem = stack.peek();
                if (topElem.arr[topElem.ind + 1] == -1) break;
                stack.pop();
            }
        }
        if (stack.isNotEmpty()) {
            topElem = stack.peek();
            topElem.arr[topElem.ind + 1] = createLeftTree(height - stack.length(), payload, stack);
        }
    }
}

private int createLeftTree(int height, int[] payload, ArrayStack stack) {
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
    var stack = new ArrayStack(this.height);
    processLeftTree(toLoc(0), stack);
    while (stack.isNotEmpty()) {
        var topElem = stack.pop();
        int indRight = topElem.arr[topElem.ind + 1];
        if (indRight > -1) processLeftTree(toLoc(indRight), stack);
    }
    return this.sum;
}

private void processLeftTree(Loc root, ArrayStack stack) {
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

private Loc toLoc(int ind) {
    final int numRegion = ind/sizeRegion;
    final int offset = ind % sizeRegion;
    return new Loc(regions.get(numRegion), offset);
}

private int allocateNode(int[] payload) {
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

private static final class ArrayStack {
    private final Loc[] content;
    private int ind;

    public ArrayStack(int height) {
        content = new Loc[height];
        ind = 0;
    }

    public void push(Loc newTree) {
        content[ind] = newTree;
        ind++;
    }

    public Loc pop() {
        if (ind == 0) return null;
        ind--;
        return content[ind];
    }

    public Loc peek() {
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
