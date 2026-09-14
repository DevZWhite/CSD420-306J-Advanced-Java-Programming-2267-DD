/**
 * Author: Zachary White
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 6 Programming Assignment - Generic Bubble Sort
 * Date: 09, 10, 2026
 *
 * Description:
 * This class implements two generic bubble sort methods that can sort an
 * array of any object type.
 *
 * 1. sort(T[] array) - Sorts an array of objects that implement the
 *    Comparable interface. This means the objects themselves know how to
 *    compare to one another (for example, Integer, String, and any custom
 *    class that implements compareTo()).
 *
 * 2. sort(T[] array, Comparator<T> comparator) - Sorts an array of objects
 *    using an external Comparator object. This allows the caller to define
 *    custom sorting logic without changing the object's class, and also
 *    allows sorting of objects that do not implement Comparable at all.
 *
 * Both methods use the classic bubble sort algorithm: repeatedly step
 * through the array, compare adjacent elements, and swap them if they are
 * in the wrong order. This continues until a full pass is made with no
 * swaps, which means the array is sorted. An "optimized" flag (swapped)
 * is used so the algorithm can exit early if the array becomes sorted
 * before all passes are complete.
 */
public class BubbleSort {

    /**
     * Sorts an array of Comparable objects in ascending order using
     * bubble sort.
     *
     * How it works:
     * - The outer loop controls how many passes are made over the array.
     * - The inner loop compares each pair of adjacent elements.
     * - compareTo() is called on the elements themselves to determine
     *   their natural ordering. If element[j] is greater than
     *   element[j + 1], they are swapped.
     * - The "swapped" boolean tracks whether any swaps occurred during a
     *   pass. If no swaps occur, the array is already sorted and the
     *   method returns early instead of doing unnecessary passes.
     *
     * @param <T>   the type of elements in the array; must implement Comparable
     * @param array the array of elements to sort (sorted in place)
     */
    public static <T extends Comparable<T>> void sort(T[] array) {
        if (array == null || array.length < 2) {
            // Nothing to sort if the array is null, empty, or has 1 element.
            return;
        }

        int n = array.length;
        boolean swapped;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;

            // After each pass, the largest unsorted element "bubbles up"
            // to its correct position at the end, so we can shrink the
            // range we still need to check (n - 1 - i).
            for (int j = 0; j < n - 1 - i; j++) {
                if (array[j].compareTo(array[j + 1]) > 0) {
                    // Elements are out of order, swap them.
                    T temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    swapped = true;
                }
            }

            // If nothing was swapped during this pass, the array is
            // already sorted and we can stop early.
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * Sorts an array of objects in the order defined by the supplied
     * Comparator, using bubble sort.
     *
     * How it works:
     * - This method works the same way as the Comparable version above,
     *   except instead of calling compareTo() on the elements themselves,
     *   it calls comparator.compare(elementA, elementB).
     * - This is useful when the objects being sorted do not implement
     *   Comparable, or when you want to sort them in a different order
     *   than their natural ordering (for example, sorting Strings by
     *   length instead of alphabetically).
     *
     * @param <T>        the type of elements in the array
     * @param array      the array of elements to sort (sorted in place)
     * @param comparator the Comparator that defines how elements should
     *                   be ordered
     */
    public static <T> void sort(T[] array, java.util.Comparator<T> comparator) {
        if (array == null || array.length < 2 || comparator == null) {
            // Nothing to sort if the array is null/too small, or if no
            // comparator was provided.
            return;
        }

        int n = array.length;
        boolean swapped;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;

            for (int j = 0; j < n - 1 - i; j++) {
                if (comparator.compare(array[j], array[j + 1]) > 0) {
                    // Elements are out of order according to the
                    // comparator, swap them.
                    T temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    swapped = true;
                }
            }

            if (!swapped) {
                break;
            }
        }
    }
}
