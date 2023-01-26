#include <stdio.h>
#include <stdlib.h>
#include <time.h> 
#include <math.h> 


#define push(sp, n) (*((sp)++) = (n))
#define pop(sp) (*--(sp))
#define peek(sp) (*((sp)-1))
#define stackSize(sp, stack) ((sp)-(stack))

const int SIZE_REGION = 200000;
const int SIZE_PAYLOAD = 4;

static struct Tree** regions;
static int currRegion = 0;
static int indFree = 0;
static struct Tree** stack;
static struct Tree** sp;


struct Tree {
    struct Tree* left;
    struct Tree* right;
    int vals[4];
};


//--------------------- Create --------------------------


struct Tree* allocateNode() {
    if (indFree == SIZE_REGION) {
        ++currRegion;
        indFree = 0;
    }
    struct Tree* region = regions[currRegion];
    struct Tree* result = &region[indFree];
    region[indFree++] = (struct Tree) { .left = NULL, .right = NULL, .vals = { 1, 2, -1, -1 } };
    return result;
}


struct Tree* createLeftTree(int height) {
    struct Tree* wholeTree = allocateNode();
    struct Tree* currTree = wholeTree;
    push(sp, wholeTree);
    for (int i = 1; i < height; ++i) {
        struct Tree* newTree = allocateNode();
        currTree->left = newTree;
        currTree = newTree;
        push(sp, newTree);
    }
    return wholeTree;
}


struct Tree* createTree(const int height) {
    // The stack
    stack = malloc(height * height * sizeof(struct Tree*));
    sp = stack;
    struct Tree* wholeTree = createLeftTree(height);
    
    while (stackSize(sp, stack) > 0) {        
        struct Tree* bottomElement = peek(sp);
        if (stackSize(sp, stack) == height || bottomElement->right != NULL) {
            pop(sp);
            while (stackSize(sp, stack) > 0) {
                bottomElement = peek(sp);
                if (bottomElement->right == NULL) break;
                pop(sp);
            }
        }
        if (stackSize(sp, stack) > 0) {
            bottomElement = peek(sp);
            bottomElement->right = createLeftTree(height - stackSize(sp, stack));
        }
    }
    return wholeTree;    
}


//--------------------- Process --------------------------


void processLeftTree(struct Tree* root, int* sum) {
    push(sp, root);
    for (int i = 0; i < SIZE_PAYLOAD; ++i) {
        (*sum) += root->vals[i];
    }
    //printf("sum at pt 0 is %d\n", sum);
    struct Tree* currNode = root->left;
    while (currNode != NULL) {
        for (int i = 0; i < SIZE_PAYLOAD; ++i) {
            (*sum) += root->vals[i];
        }
        //printf("sum at pt 1 is %d\n", sum);
        push(sp, currNode);
        currNode = currNode->left;
    }
}

int processTree(struct Tree* root) {
    if (root == NULL) {
        printf("Tree is NULL!\n");
        return -1;
    }

    int sum = 0;
    processLeftTree(root, &sum);
    //printf("sum after first left tree is %d\n", sum);
    while (stackSize(sp, stack) > 0) {
        struct Tree* bottomElem = pop(sp);
        if (bottomElem->right != NULL) processLeftTree(bottomElem->right, &sum);
    }
    return sum;
}


//--------------------- Run --------------------------


int main(int argc, char** argv) {
    if (argc != 2) {
        printf("This benchmark needs exactly 1 arguments: the tree height (25 is a good start)\n");
        return -1;
    }
    int height;
    if (sscanf (argv[1], "%i", &height) != 1 || height <= 0) {
        printf("Error - argument must be a positive integer\n");
        return -1;
    }

    int numRegions = (pow(2, height) - 1)/SIZE_REGION + 1;
    printf("# of regions: %d\n", numRegions);
    regions = (struct Tree**)malloc(numRegions * sizeof(struct Tree*));
    for (int i = 0; i < numRegions; ++i) {
        regions[i] = (struct Tree*)malloc(SIZE_REGION * sizeof(struct Tree));
    }

    //regions[0][0] = (struct Tree) { .left = NULL, .right = NULL, .vals = {1, 2, -1, -1} };

    printf("Processing tree with Regions...\n");
    clock_t timeStart = clock();

    struct Tree* theTree = createTree(height);
    clock_t timeAlloc = clock();
    int sum = processTree(theTree);

    clock_t timeEnd = clock();
    printf("The sum is %d\n", sum);
    double diffTimeAlloc = (double)(timeAlloc - timeStart) / CLOCKS_PER_SEC;
    double diffTime = (double)(timeEnd - timeStart) / CLOCKS_PER_SEC;
    printf("Alloc time: %f s\n", diffTimeAlloc);
    printf("Total used time: %f s\n", diffTime);
    return 0;
}

