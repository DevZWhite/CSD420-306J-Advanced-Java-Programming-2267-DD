/**
 * Author: Zachary White
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 4 Programming Assignment - LinkedList Traversal Timing
 * Date: August 27, 2026
 *
 * Description:
 * Test program that stores a large number of integers in a LinkedList and
 * compares the time it takes to traverse the list two different ways:
 *   1. Using an Iterator (calling next() repeatedly)
 *   2. Using the get(index) method in a standard indexed for-loop
 *
 * The program runs this comparison twice: once with 50,000 integers and
 * once with 500,000 integers, so the two approaches can be compared both
 * within a single run and across the two list sizes.
 */
import java.util.Iterator;
import java.util.LinkedList;

public class LinkedListTimingTest {

    public static void main(String[] args) {
        // Run the timing comparison for both required list sizes
        runTimingComparison(50_000);
        runTimingComparison(500_000);

        // Run a small correctness check separate from the timing runs
        verifyTraversalCorrectness();

        /*
         * DISCUSSION OF MY RESULTS BUILDING THIS APPLICATION
         * ---------------------------------------------------------------
         * A LinkedList in Java is implemented as a doubly linked list, so
         * there is no way to jump directly to a given position the way an
         * array (or ArrayList backed by an array) can. Each call to
         * get(index) has to start at whichever end of the list is closer
         * (head or tail) and step through node-by-node until it reaches
         * the requested index. That means a single get(index) call runs
         * in O(n) time in the most worst case.
         *
         * When get(index) is called inside a for-loop that counts from 0
         * up to size - 1, that O(n) cost is paid on every single
         * iteration, which makes the entire traversal loop O(n^2) overall.
         * An Iterator, on the other hand, keeps a direct reference to the
         * current node and simply follows the "next" pointer to move
         * forward, so each call to next() is O(1) and a full traversal
         * with an iterator is O(n) overall.
         *
         * In practice this shows up as the iterator traversal finishing
         * almost instantly for both list sizes (a few milliseconds or
         * less), while the get(index) traversal is noticeably slower even
         * at 50,000 elements and becomes dramatically slower at 500,000
         * elements. Because the get(index) approach is O(n^2), increasing
         * the list size by a factor of 10 (50,000 -> 500,000) does not
         * just increase its running time by a factor of 10 - it increases
         * it by roughly a factor of 100, since both the number of
         * iterations and the average cost per get() call grow with n.
         * The iterator's running time, by contrast, grows only linearly,
         * so it takes roughly 10 times as long at 500,000 elements as it
         * did at 50,000, not 100 times as long.
         *
         * The overall conclusion is that get(index) should be avoided when
         * repeatedly traversing a LinkedList. An Iterator (or an enhanced
         * for-loop, which uses an iterator internally) should be used
         * instead whenever every element needs to be visited in order.
         * The get(index) method is only efficient on a LinkedList when a
         * single, occasional lookup is needed rather than a full scan.
         * (Exact millisecond values will vary depending on the machine and
         * JVM running the program, but the relative pattern described
         * above - iterator far outperforming get(index), with the gap
         * widening sharply as list size grows - is consistent and is what
         * this program's console output demonstrates.)
         */
    }

    /**
     * Builds a LinkedList of the given size filled with sequential integers,
     * then times how long it takes to traverse that list using an Iterator
     * versus using the get(index) method, and prints the results.
     *
     * @param size the number of integers to store in the LinkedList
     */
    private static void runTimingComparison(int size) {
        LinkedList<Integer> numbers = buildList(size);

        System.out.println("===== List size: " + size + " =====");

        long iteratorTimeMs = timeIteratorTraversal(numbers);
        System.out.println("Iterator traversal time: " + iteratorTimeMs + " ms");

        long getIndexTimeMs = timeGetIndexTraversal(numbers);
        System.out.println("get(index) traversal time: " + getIndexTimeMs + " ms");

        System.out.println();
    }

    /**
     * Creates a LinkedList<Integer> containing 'size' sequential values,
     * starting at 0.
     */
    private static LinkedList<Integer> buildList(int size) {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    /**
     * Traverses the given list from start to finish using an Iterator
     * and returns the elapsed time in milliseconds.
     */
    private static long timeIteratorTraversal(LinkedList<Integer> list) {
        long startTime = System.nanoTime();

        long runningTotal = 0; // forces the JIT to actually use each value
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            runningTotal += it.next();
        }

        long endTime = System.nanoTime();
        // runningTotal is not printed, but referencing it below keeps the
        // compiler from optimizing the loop away entirely
        if (runningTotal < 0) {
            System.out.println("unreachable");
        }
        return (endTime - startTime) / 1_000_000;
    }

    /**
     * Traverses the given list from start to finish using get(index) in
     * a standard indexed for-loop and returns the elapsed time in
     * milliseconds.
     */
    private static long timeGetIndexTraversal(LinkedList<Integer> list) {
        long startTime = System.nanoTime();

        long runningTotal = 0;
        for (int i = 0; i < list.size(); i++) {
            runningTotal += list.get(i);
        }

        long endTime = System.nanoTime();
        if (runningTotal < 0) {
            System.out.println("unreachable");
        }
        return (endTime - startTime) / 1_000_000;
    }

    /**
     * Small correctness check, separate from the timing runs above, that
     * confirms both traversal approaches actually visit every element in
     * the correct order and produce the expected sum. Uses a much smaller
     * list so the get(index) portion completes quickly.
     */
    private static void verifyTraversalCorrectness() {
        System.out.println("===== Correctness check =====");

        int testSize = 1_000;
        LinkedList<Integer> testList = buildList(testSize);
        long expectedSum = (long) (testSize - 1) * testSize / 2; // 0 + 1 + ... + (testSize - 1)

        long iteratorSum = 0;
        for (int value : testList) {
            iteratorSum += value;
        }

        long getIndexSum = 0;
        for (int i = 0; i < testList.size(); i++) {
            getIndexSum += testList.get(i);
        }

        boolean iteratorPassed = iteratorSum == expectedSum;
        boolean getIndexPassed = getIndexSum == expectedSum;

        System.out.println("Expected sum for " + testSize + " sequential values: " + expectedSum);
        System.out.println("Sum via iterator:   " + iteratorSum + " -> " + (iteratorPassed ? "PASS" : "FAIL"));
        System.out.println("Sum via get(index): " + getIndexSum + " -> " + (getIndexPassed ? "PASS" : "FAIL"));
    }
}