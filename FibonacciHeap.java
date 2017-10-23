package fibPack;

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over non-negative integers.
 */
public class FibonacciHeap {

	/*
	 * FiBonacciHeap parameters: mMin- Point's to the node with the minimum key
	 * mTreeCount- Total number of trees in the heap mMarkedCout- Total number
	 * of marked nodes (as defined in class) mSize- Total number of nodes
	 * totalLinks- Counter of the total links conducted upon the heap totalCuts-
	 * Counter of the total cuts conducted upon the heap
	 */
	private static int totalLinks = 0;
	private static int totalCuts = 0;

	private HeapNode mMin;
	private int mTreesCount;
	private int mMarkedCount;
	private int mSize;

	// tester
	public static void main(String[] args) {
		FibonacciHeap fibo = new FibonacciHeap();
		int[] arr = { 5, 8, 2, 14, 1, 3, 11 };
		fibo.arrayToHeap(arr);
		System.out.println(fibo.mTreesCount);
		System.out.println(fibo.potential());
		for (int i = 1; i <= 1000; i++) {
			fibo.insert(i);
		}
		System.out.println(fibo.potential());

		/*
		 * fibo.displayHeap(); printLines();
		 * System.out.println(fibo.findMin().getmKey()); printLines();
		 * //fibo.displayHeap(); fibo.deleteMin(); fibo.displayHeap();
		 * printLines(); fibo.insert(77); fibo.insert(88); fibo.insert(2);
		 * fibo.insert(450); fibo.insert(365); HeapNode min = fibo.insert(1);
		 * fibo.displayHeap(); printLines(); fibo.delete(min);
		 * fibo.displayHeap(); printLines(); System.out.println(fibo.mSize);
		 */
	}

	public static void printLines() {
		for (int i = 0; i <= 20; i++) {
			System.out.print("-");
		}
		System.out.println("");
	}

	public void displayHeap() {
		//System.out.println("Roots\tChildren");
		HeapNode start = this.mMin;
		start.print(0);
	}

	// Constructor- with arguments
	public FibonacciHeap() {
		this.mMin = null;
		this.mTreesCount = 0;
		this.mMarkedCount = 0;
		this.mSize = 0;
	}

	// Clear the heap- set mMin to null and set to zero all counters
	private void clear() {
		this.mMin = null;
		this.mTreesCount = 0;
		this.mMarkedCount = 0;
		this.mSize = 0;
	}

	/**
	 * public boolean empty()
	 *
	 * precondition: none
	 * 
	 * The method returns true if and only if the heap is empty.
	 * 
	 */
	public boolean empty() {
		return (this.mMin == null);
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and
	 * inserts it into the heap.
	 */
	public HeapNode insert(int key) {
		// Create an Heap and meld it with the current Heap
		FibonacciHeap lNewHeap = new FibonacciHeap();
		lNewHeap.mMin = new HeapNode(key);
		lNewHeap.mSize = 1;
		lNewHeap.mTreesCount = 1;

		this.meld(lNewHeap);

		// return the Heap that was inserted
		return lNewHeap.mMin;

	}

	/**
	 * public void deleteMin()
	 *
	 * Delete the node containing the minimum key.
	 *
	 */
	public void deleteMin() {
		if (this.empty())
			return;

		// Reduce the size of total nodes
		this.mSize--;
		HeapNode lMinNode = this.mMin;

		// Check whether heap contains more than one node
		if (this.mMin.getmNext() == this.mMin)
			this.mMin = null;
		// Else, by-pass the current minimum
		else {
			this.mMin.getmPrev().setmNext(this.mMin.getmNext());
			this.mMin.getmNext().setmPrev(this.mMin.getmPrev());
			// Set temporary minimum
			this.mMin = this.mMin.getmNext();
		}

		// Count number of marked direct children of the mMin tree
		// Set counter to zero
		int lMarkedChildCount = 0;
		if (lMinNode.getmChild() != null) {
			// Continue to add until no more children exist
			HeapNode lCurrNode = lMinNode.getmChild();
			do {
				// Set parent to null
				lCurrNode.setmParent(null);
				// If child is marked, add +1 to counter and remove the mark
				// from
				// the child- as it will be a root
				if (lCurrNode.getmMark()) {
					lMarkedChildCount++;
					lCurrNode.setmMark(false);
				}
				lCurrNode = lCurrNode.getmNext();
			} while (lCurrNode != lMinNode.getmChild());
		}

		// Create a heap of mMin children and meld them to main heap
		FibonacciHeap lNewHeap = new FibonacciHeap();
		lNewHeap.mMin = lMinNode.getmChild();
		lNewHeap.mTreesCount = lMinNode.getmRank() - 1;
		lNewHeap.mMarkedCount = (-1) * lMarkedChildCount;
		this.meld(lNewHeap);

		// Exit if heap contained only one node (mMin)
		if (this.empty())
			return;
		// Consolidate- i.e. Successive-Linking
		this.consolidate();
	}

	private void consolidate() {
		// Create an array of HeapNodes by toBuckets
		HeapNode[] buckets = this.toBuckets();
		// Retrieve from buckets while melding all nodes to one heap
		this.fromBuckets(buckets);

	}

	// Return an array of nodes, at most one node of each rank
	private HeapNode[] toBuckets() {
		HeapNode[] buckets = new HeapNode[42];
		// Initialize the array
		for (int i = 0; i < buckets.length; i++) {
			buckets[i] = null;
		}

		// Start the operation from the mMin node
		HeapNode lStartNode = this.mMin;
		// Cut the link to the previous node
		lStartNode.getmPrev().setmNext(null);
		// Iterate until the disconnected link
		while (lStartNode != null) {
			HeapNode lCurrNode = lStartNode;
			lStartNode = lStartNode.getmNext();

			// Cut the current node from the main heap
			lCurrNode.setmNext(lCurrNode);
			lCurrNode.setmPrev(lCurrNode);

			// Apply successive linking of the node
			while (buckets[lCurrNode.getmRank()] != null) {
				// Link between equal ranked nodes
				lCurrNode = link(lCurrNode, buckets[lCurrNode.getmRank()]);
				buckets[lCurrNode.getmRank() - 1] = null;
			}
			// Place linked node into the appropriate place in the bucket
			buckets[lCurrNode.getmRank()] = lCurrNode;
		}
		return buckets;
	}

	private void fromBuckets(HeapNode[] aBuckets) {
		this.mMin = null;
		this.mTreesCount = 0;
		// Iterate on the bucket list
		for (int i = 0; i < aBuckets.length; i++) {
			// Retrieve each existing node from the array
			if (aBuckets[i] != null) {
				// Create a temporary heap with the node and meld into main heap
				FibonacciHeap lNewHeap = new FibonacciHeap();
				lNewHeap.mMin = aBuckets[i];
				lNewHeap.mTreesCount = 1;
				this.meld(lNewHeap);
			}
		}
	}

	private static HeapNode link(HeapNode aFirstNode, HeapNode aSecNode) {
		// Choose the parent and the child
		// As seen in class, child is the node with the larger key
		HeapNode lChildNode = aFirstNode.getmKey() > aSecNode.getmKey() ? aFirstNode : aSecNode;
		HeapNode lParentNode = aFirstNode.getmKey() > aSecNode.getmKey() ? aSecNode : aFirstNode;

		// Link both nodes
		HeapNode lChildOfParentNode = lParentNode.getmChild();
		lChildNode.setmParent(lParentNode);
		lParentNode.setmChild(lChildNode);
		lParentNode.setmRank(lParentNode.getmRank() + 1);

		// Test is parent node contains children
		if (lChildOfParentNode != null) {
			// Link new child node to existing children nodes list
			HeapNode lNextChild = lChildOfParentNode.getmNext();
			lChildOfParentNode.setmNext(lChildNode);
			lChildOfParentNode.getmNext().setmPrev(lChildOfParentNode);
			lChildNode.setmNext(lNextChild);
			lChildNode.getmNext().setmPrev(lChildNode);
		}

		totalLinks++;
		return lParentNode;
	}

	/**
	 * public HeapNode findMin()
	 *
	 * Return the node of the heap whose key is minimal.
	 *
	 */
	public HeapNode findMin() {
		return this.mMin;
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2) {

		if (heap2 == null)
			return;

		if (this.empty() || heap2.empty()) {
			// If current heap is empty, choose heap2 mMin as current mMin
			if (this.empty())
				this.mMin = heap2.mMin;
			// Sum all counters
			this.mSize += heap2.mSize;
			this.mTreesCount += heap2.mTreesCount;
			this.mMarkedCount += heap2.mMarkedCount;
			return;
		}

		// Link heap2 to current heap
		HeapNode lNextHeap = this.mMin.getmNext();
		this.mMin.setmNext(heap2.mMin.getmNext());
		this.mMin.getmNext().setmPrev(this.mMin);
		heap2.mMin.setmNext(lNextHeap);
		heap2.mMin.getmNext().setmPrev(heap2.mMin);
		// Change mMin if required
		if (heap2.mMin.getmKey() < this.mMin.getmKey())
			this.mMin = heap2.mMin;
		// Sum all counters
		this.mSize += heap2.mSize;
		this.mTreesCount += heap2.mTreesCount;
		this.mMarkedCount += heap2.mMarkedCount;

	}

	/**
	 * public int size()
	 *
	 * Return the number of elements in the heap
	 * 
	 */
	public int size() {
		return this.mSize;
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return a counters array, where the value of the i-th entry is the number
	 * of trees of order i in the heap.
	 * 
	 */
	public int[] countersRep() {
		int[] arr = new int[42];
		if (!this.empty()) {
			// Iterate upon the heap's root nodes
			HeapNode lStartNode = this.mMin;
			do {
				// Add 1 to total number of nodes of same rank
				arr[lStartNode.getmRank()] += 1;
				lStartNode = lStartNode.getmNext();
			} while (lStartNode != this.mMin);
		}
		return arr;
	}

	/**
	 * public void arrayToHeap()
	 *
	 * Insert the array to the heap. Delete previous elements in the heap.
	 * 
	 */
	public void arrayToHeap(int[] array) {
		// Delete previous elements in the heap
		this.clear();
		// Iterate and insert the items
		for (int i = 0; i < array.length; i++)
			this.insert(array[i]);
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap.
	 *
	 */
	public void delete(HeapNode x) {
		// Change key to minimum possible
		this.changeKey(x, Integer.MIN_VALUE);
		// Delete minimum- i.e. changed key
		this.deleteMin();
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * The function decreases the key of the node x by delta. The structure of
	 * the heap should be updated to reflect this change (for example, the
	 * cascading cuts procedure should be applied if needed).
	 */
	public void decreaseKey(HeapNode x, int delta) {
		// Call to changeKey with decreased key of x
		this.changeKey(x, x.getmKey() - delta);
	}

	// Function that changes the key of a node and calls cascading cuts, if
	// needed
	private void changeKey(HeapNode aNode, int aNewKey) {
		aNode.setmKey(aNewKey);
		HeapNode lParentNode = aNode.getmParent();
		// If a parent node exists and the heap rule was violated- do cascading
		// cuts
		if (lParentNode != null && aNode.getmKey() < lParentNode.getmKey()) {
			cascadingCut(aNode, lParentNode);
		}

		// If node is new minimum- set it as such
		if (aNode.getmKey() < this.mMin.getmKey())
			this.mMin = aNode;

	}

	private void cut(HeapNode aChild, HeapNode aParent) {
		// Disconnect aChild from aParent
		aChild.setmParent(null);

		// If aChild was marked, unmark it (as it will be a root node)
		// Decrease marked counter by 1
		if (aChild.getmMark()) {
			aChild.mMarked = false;
			this.mMarkedCount--;

		}
		// Decrease total rank of aParent by 1
		aParent.mRank--;

		// Unlink aChild from aParent's children list
		if (aChild.getmNext() == aChild)
			aParent.setmChild(null);
		else {
			aParent.setmChild(aChild.getmNext());
			aChild.getmPrev().setmNext(aChild.getmNext());
			aChild.getmNext().setmPrev(aChild.getmPrev());
			aChild.setmNext(aChild);
			aChild.setmPrev(aChild);
		}

		// Create a temporary heap with aChild as a single node
		// Meld temporary heap with main heap
		FibonacciHeap lNewHeap = new FibonacciHeap();
		lNewHeap.mMin = aChild;
		lNewHeap.mTreesCount = 1;
		this.meld(lNewHeap);

		totalCuts++;
	}

	private void cascadingCut(HeapNode aChild, HeapNode aParent) {
		// Cut aChild from aParent
		this.cut(aChild, aParent);
		// Check if a aParent has a parent
		if (aParent.getmParent() != null) {
			// Set aParent as marked, if it was not
			if (!aParent.mMarked) {
				aParent.mMarked = true;
				this.mMarkedCount++;
			} else
				// Continue cascadingCut, as aParent was marked
				cascadingCut(aParent, aParent.getmParent());
		}
	}

	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is:
	 * Potential = #trees + 2*#marked The potential equals to the number of
	 * trees in the heap plus twice the number of marked nodes in the heap.
	 */
	public int potential() {
		return 2 * (this.mMarkedCount) + this.mTreesCount;
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made
	 * during the run-time of the program. A link operation is the operation
	 * which gets as input two trees of the same rank, and generates a tree of
	 * rank bigger by one, by hanging the tree which has larger value in its
	 * root on the tree which has smaller value in its root.
	 */
	public static int totalLinks() {
		return totalLinks;
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made
	 * during the run-time of the program. A cut operation is the operation
	 * which diconnects a subtree from its parent (during decreaseKey/delete
	 * methods).
	 */
	public static int totalCuts() {
		return totalCuts;
	}

	/**
	 * public class HeapNode
	 * 
	 * If you wish to implement classes other than FibonacciHeap (for example
	 * HeapNode), do it in this file, not in another file
	 * 
	 */
	public class HeapNode {

		/*
		 * HeapNode Parameters: mKey- will contain the key of the node mRank-
		 * will contain the rank of the node (total children of a node) mMarked-
		 * True if the node is marked, else False mChild- Direct child to the
		 * heap mNext- Next node in the doubly-linked list mPrev- Previous node
		 * in the doubly-linked list mParent- Proint's to Parent node
		 */
		private int mKey;
		private int mRank;
		private boolean mMarked;
		private HeapNode mChild;
		private HeapNode mNext;
		private HeapNode mPrev;
		private HeapNode mParent;

		// Initialize the heap node with the given key
		public HeapNode(int aKey) {
			this.mKey = aKey;
			this.mMarked = false;
			this.mRank = 0;
			this.mChild = null;
			this.mParent = null;
			this.mNext = this;
			this.mPrev = this;
		}

		public void printHeap() {
			HeapNode pt = this;
			printHeap(pt);
			// Print root
/*			System.out.print(this.mKey + " \t");
			// First level
			int index = 0;
			while(pt.mChild != null) {
				if (index >= 1) {
					System.out.print("    \u21C5\u21C6 ");
				} else {
					System.out.print("\u21C6 ");
				}
				HeapNode levelPt = pt.mChild;
				if (levelPt != null) {
					System.out.print(levelPt.mKey);
				}
				HeapNode levelPtIt = levelPt.mNext;
				while (levelPtIt != levelPt) {
					System.out.print(" \u21C6 " + levelPtIt.mKey);
					levelPtIt = levelPtIt.mNext;
				}
				pt = pt.mChild;
				System.out.println("");
				System.out.print("\t");
				index++;
			}
			System.out.println("");*/

		}
		
		public void printHeap(HeapNode ptr) {
			if (ptr == null) {
				return;
			}
			HeapNode level = ptr;
			HeapNode nextlevel = level.mNext;
			do {
				System.out.println(level.mKey);
			} while (level != nextlevel);
			printHeap(ptr.mChild);
		}
		
		private void print(int level) {
            HeapNode curr = this;
            do {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    sb.append("  ");
                }
                sb.append(curr.mKey);
                System.out.println(sb.toString());
                if (curr.mChild != null) {
                    curr.mChild.print(level + 1);
                }
                curr = curr.mNext;
            } while (curr != this);
        }

		/*
		 * Getters and Setters
		 */

		public int getmKey() {
			return mKey;
		}

		public void setmKey(int mKey) {
			this.mKey = mKey;
		}

		public int getmRank() {
			return mRank;
		}

		public void setmRank(int mRank) {
			this.mRank = mRank;
		}

		public boolean getmMark() {
			return mMarked;
		}

		public void setmMark(boolean mMark) {
			this.mMarked = mMark;
		}

		public HeapNode getmChild() {
			return mChild;
		}

		public void setmChild(HeapNode mChild) {
			this.mChild = mChild;
		}

		public HeapNode getmNext() {
			return mNext;
		}

		public void setmNext(HeapNode mNext) {
			this.mNext = mNext;
		}

		public HeapNode getmPrev() {
			return mPrev;
		}

		public void setmPrev(HeapNode mPrev) {
			this.mPrev = mPrev;
		}

		public HeapNode getmParent() {
			return mParent;
		}

		public void setmParent(HeapNode mParent) {
			this.mParent = mParent;
		}

	}
}
