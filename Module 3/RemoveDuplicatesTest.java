/**
 * Author: Zachary White
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 3 Programming Assignment - Generic Remove Duplicates
 * Date: August 30, 2026
 *
 * Description:
 * Test program that demonstrates a generic static method for removing
 * duplicate values from an ArrayList. The method builds a new ArrayList
 * that contains every distinct value from the original list, keeping the
 * values in the same order they first appeared.
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class RemoveDuplicatesTest {

    // Total number of random values generated for the original list
    private static final int TOTAL_VALUES = 50;
    // Lower bound (inclusive) for the random values
    private static final int LOWER_BOUND = 1;
    // Upper bound (inclusive) for the random values
    private static final int UPPER_BOUND = 20;

    /**
     * Generic static method that builds and returns a new ArrayList
     * containing the distinct values from the ArrayList passed in.
     *
     * The type parameter <E> is declared before the return type, which
     * scopes it to this method only. That lets the method work with an
     * ArrayList holding any reference type while the compiler still
     * enforces type safety on both the input and the returned list.
     *
     * Rather than handing the whole list to a Set constructor, this
     * version walks the list one element at a time. A HashSet is kept
     * on the side purely to track which values have already been seen
     * (HashSet lookups run in constant time), while a separate ArrayList
     * collects the actual output in first-seen order. An element is only
     * appended to the result the first time it shows up.
     *
     * @param list the original ArrayList, possibly containing duplicates
     * @param <E>  the element type of the list
     * @return a new ArrayList with duplicates removed, order preserved
     */
    public static <E> ArrayList<E> removeDuplicates(ArrayList<E> list) {
        HashSet<E> seenValues = new HashSet<>();
        ArrayList<E> distinctValues = new ArrayList<>();

        for (E value : list) {
            if (seenValues.add(value)) {
                // seenValues.add() returns true only the first time
                // a given value is added, so this is our "new value" check
                distinctValues.add(value);
            }
        }
        return distinctValues;
    }

    /**
     * Small helper method just to keep main() from repeating the same
     * three print statements twice.
     */
    private static void displayList(String label, ArrayList<Integer> values) {
        System.out.println(label + " (" + values.size() + " values):");
        System.out.println(values);
        System.out.println();
    }

    public static void main(String[] args) {
        ArrayList<Integer> originalList = new ArrayList<>();

        // Fill the original list with 50 random integers, 1 through 20
        for (int i = 0; i < TOTAL_VALUES; i++) {
            int randomValue = ThreadLocalRandom.current().nextInt(LOWER_BOUND, UPPER_BOUND + 1);
            originalList.add(randomValue);
        }

        // Use the generic method to get a duplicate-free version of the list
        ArrayList<Integer> noDuplicatesList = removeDuplicates(originalList);

        displayList("Original list", originalList);
        displayList("List after removeDuplicates()", noDuplicatesList);
    }
}