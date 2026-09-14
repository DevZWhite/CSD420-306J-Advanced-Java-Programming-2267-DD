import java.util.Arrays;
import java.util.Comparator;

/**
 * Author: Zachary White
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 6 Programming Assignment - Generic Bubble Sort
 * Date: 09, 10, 2026
 *
 * Description:
 * This class contains test/driver code that verifies BubbleSort.sort()
 * works correctly for both the Comparable and Comparator versions.
 *
 * It tests:
 * - Sorting an array of Integers (Comparable) in ascending order.
 * - Sorting an array of Strings (Comparable) alphabetically.
 * - Sorting an array of Strings using a Comparator to sort by length
 *   instead of alphabetically.
 * - Sorting an array of custom Employee objects using their natural
 *   Comparable ordering (by name).
 * - Sorting the same Employee objects using a Comparator to sort by
 *   salary instead.
 * - Edge cases: an empty array, a single-element array, and a null array.
 *
 * Each test prints the array before and after sorting, along with a
 * PASS/FAIL result determined by checking that the array is actually in
 * sorted order afterward.
 */
public class BubbleSortTest {

    public static void main(String[] args) {
        testIntegerSort();
        testStringComparableSort();
        testStringComparatorByLength();
        testEmployeeComparableSort();
        testEmployeeComparatorBySalary();
        testEdgeCases();
    }

    /**
     * Tests sorting Integers using the Comparable version of bubble sort.
     */
    private static void testIntegerSort() {
        System.out.println("Test 1: Sorting Integers (Comparable)");
        Integer[] numbers = {8, 3, 9, 1, 5, 2, 7};
        System.out.println("Before: " + Arrays.toString(numbers));

        BubbleSort.sort(numbers);

        System.out.println("After:  " + Arrays.toString(numbers));
        System.out.println(isSortedAscending(numbers) ? "PASS" : "FAIL");
        System.out.println();
    }

    /**
     * Tests sorting Strings alphabetically using the Comparable version
     * of bubble sort.
     */
    private static void testStringComparableSort() {
        System.out.println("Test 2: Sorting Strings alphabetically (Comparable)");
        String[] words = {"banana", "apple", "cherry", "date", "fig"};
        System.out.println("Before: " + Arrays.toString(words));

        BubbleSort.sort(words);

        System.out.println("After:  " + Arrays.toString(words));
        System.out.println(isSortedAscending(words) ? "PASS" : "FAIL");
        System.out.println();
    }

    /**
     * Tests sorting Strings by length (shortest to longest) using the
     * Comparator version of bubble sort, showing custom ordering logic.
     */
    private static void testStringComparatorByLength() {
        System.out.println("Test 3: Sorting Strings by length (Comparator)");
        String[] words = {"banana", "apple", "kiwi", "fig", "cherry"};
        System.out.println("Before: " + Arrays.toString(words));

        Comparator<String> byLength = new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        };

        BubbleSort.sort(words, byLength);

        System.out.println("After:  " + Arrays.toString(words));

        boolean pass = true;
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].length() > words[i + 1].length()) {
                pass = false;
                break;
            }
        }
        System.out.println(pass ? "PASS" : "FAIL");
        System.out.println();
    }

    /**
     * Tests sorting custom Employee objects by their natural ordering
     * (name), using the Comparable version of bubble sort.
     */
    private static void testEmployeeComparableSort() {
        System.out.println("Test 4: Sorting Employees by name (Comparable)");
        Employee[] employees = {
                new Employee("Zach", 55000),
                new Employee("Amy", 62000),
                new Employee("Mike", 48000),
                new Employee("Beth", 71000)
        };
        System.out.println("Before: " + Arrays.toString(employees));

        BubbleSort.sort(employees);

        System.out.println("After:  " + Arrays.toString(employees));

        boolean pass = true;
        for (int i = 0; i < employees.length - 1; i++) {
            if (employees[i].getName().compareTo(employees[i + 1].getName()) > 0) {
                pass = false;
                break;
            }
        }
        System.out.println(pass ? "PASS" : "FAIL");
        System.out.println();
    }

    /**
     * Tests sorting custom Employee objects by salary using the
     * Comparator version of bubble sort, showing that objects can be
     * sorted differently than their natural ordering.
     */
    private static void testEmployeeComparatorBySalary() {
        System.out.println("Test 5: Sorting Employees by salary (Comparator)");
        Employee[] employees = {
                new Employee("Zach", 55000),
                new Employee("Amy", 62000),
                new Employee("Mike", 48000),
                new Employee("Beth", 71000)
        };
        System.out.println("Before: " + Arrays.toString(employees));

        Comparator<Employee> bySalary = new Comparator<Employee>() {
            @Override
            public int compare(Employee a, Employee b) {
                return Double.compare(a.getSalary(), b.getSalary());
            }
        };

        BubbleSort.sort(employees, bySalary);

        System.out.println("After:  " + Arrays.toString(employees));

        boolean pass = true;
        for (int i = 0; i < employees.length - 1; i++) {
            if (employees[i].getSalary() > employees[i + 1].getSalary()) {
                pass = false;
                break;
            }
        }
        System.out.println(pass ? "PASS" : "FAIL");
        System.out.println();
    }

    /**
     * Tests edge cases: an empty array, a single-element array, and a
     * null array. None of these should throw an exception, and arrays
     * that already have 0 or 1 elements should remain unchanged.
     */
    private static void testEdgeCases() {
        System.out.println("Test 6: Edge cases (empty, single element, null)");

        Integer[] empty = {};
        BubbleSort.sort(empty);
        System.out.println("Empty array after sort: " + Arrays.toString(empty)
                + " -> " + (empty.length == 0 ? "PASS" : "FAIL"));

        Integer[] single = {42};
        BubbleSort.sort(single);
        System.out.println("Single-element array after sort: " + Arrays.toString(single)
                + " -> " + (single.length == 1 && single[0] == 42 ? "PASS" : "FAIL"));

        Integer[] nullArray = null;
        try {
            BubbleSort.sort(nullArray);
            System.out.println("Null array handled without exception -> PASS");
        } catch (Exception e) {
            System.out.println("Null array threw exception -> FAIL");
        }
        System.out.println();
    }

    /**
     * Helper method that checks whether a Comparable array is sorted in
     * ascending order. Used to verify test results.
     *
     * @param array the array to check
     * @param <T>   the type of elements in the array
     * @return true if the array is sorted in ascending order, false otherwise
     */
    private static <T extends Comparable<T>> boolean isSortedAscending(T[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i].compareTo(array[i + 1]) > 0) {
                return false;
            }
        }
        return true;
    }
}
