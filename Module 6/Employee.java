/**
 * Author: Zachary White
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 6 Programming Assignment - Generic Bubble Sort
 * Date: 09, 10, 2026
 *
 * Description:
 * A simple class representing an Employee, used to demonstrate sorting of
 * custom objects. This class implements Comparable<Employee> so its
 * natural ordering is by name (alphabetical). A separate Comparator will
 * be used elsewhere to sort Employees by salary instead, to show how the
 * two sorting approaches differ.
 */
public class Employee implements Comparable<Employee> {

    private String name;
    private double salary;

    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public double getSalary() {
        return salary;
    }

    /**
     * Defines the natural ordering of Employee objects: alphabetically
     * by name. This is what gets used when Employees are sorted with
     * the Comparable-based bubble sort method.
     */
    @Override
    public int compareTo(Employee other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name + " ($" + salary + ")";
    }
}
