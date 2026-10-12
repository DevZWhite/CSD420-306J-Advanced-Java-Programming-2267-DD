/**
 * Author: Zachary White
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 11 Programming Assignment - Gson JSON Example
 * Date: October 9, 2026
 *
 * Description:
 * Demonstrates the Gson library's data binding approach by converting a
 * Java object to a JSON string (serialization) and then back into a Java
 * object (deserialization) using the Gson class. It also shows how a
 * generic List of objects is read back using TypeToken. Unlike Jackson,
 * Gson reads and writes the fields directly through reflection, so the
 * Student class does not need getters or setters for the conversion to work.
 *
 * To compile and run, place the Gson JAR (gson-2.14.0.jar) in the same
 * folder, then:
 *   javac -cp "gson-2.14.0.jar:." GsonExample.java
 *   java  -cp "gson-2.14.0.jar:." GsonExample.java
 *
 * On Windows, use a semicolon (;) instead of a colon (:) in the classpath.
 * If you downloaded a different Gson version, change the JAR name to match.
 * Text blocks (""" ... """)     require Java 15 or newer.
 *
 * Source: Adapted from the Gson User Guide
 *   https://google.github.io/gson/UserGuide.html
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class GsonExample {

    // Simple POJO used to demonstrate data binding. Gson maps JSON
    // fields to these Java fields automatically based on their names.
    public static class Student {
        private String name;
        private String major;
        private int gradYear;
        private boolean fullTime;

        // Gson can build objects without a no-args constructor, but
        // providing one is the recommended practice
        public Student() {}

        public Student(String name, String major, int gradYear, boolean fullTime) {
            this.name = name;
            this.major = major;
            this.gradYear = gradYear;
            this.fullTime = fullTime;
        }

        // Getters are included for convenience in our own code. Gson
        // itself does not need them; it uses the private fields directly.
        public String getName() { return name; }
        public String getMajor() { return major; }
        public int getGradYear() { return gradYear; }
        public boolean isFullTime() { return fullTime; }

        @Override
        public String toString() {
            return "Student{name='" + name + "', major='" + major
                    + "', gradYear=" + gradYear + ", fullTime=" + fullTime + "}";
        }
    }

    public static void main(String[] args) {
        // GsonBuilder lets us configure settings before creating the Gson
        // object. Here we turn on pretty printing (the default is compact).
        // A Gson instance keeps no state, so one can be reused freely.
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // --- Serialization: Java object to JSON string ---
        Student student = new Student("Maria Lopez", "Computer Science", 2027, true);
        String json = gson.toJson(student);

        System.out.println("--- Serialization (Java -> JSON) ---");
        System.out.println(json);
        System.out.println();

        // --- Deserialization: JSON string to Java object ---
        String incomingJson = """
                {
                    "name": "Zachary White",
                    "major": "Software Development",
                    "gradYear": 2026,
                    "fullTime": true
                }
                """;

        Student parsedStudent = gson.fromJson(incomingJson, Student.class);

        System.out.println("--- Deserialization (JSON -> Java) ---");
        System.out.println(parsedStudent);
        System.out.println("Name field accessed directly: " + parsedStudent.getName());
        System.out.println();

        // --- Generic types: JSON array to List<Student> ---
        // Java erases generic types at runtime, so Gson needs a TypeToken
        // to know the list holds Student objects.
        String listJson = """
                [
                    {"name": "Aisha Khan", "major": "Cybersecurity", "gradYear": 2028, "fullTime": true},
                    {"name": "Tom Nguyen", "major": "Data Science", "gradYear": 2027, "fullTime": true}
                ]
                """;

        Type studentListType = new TypeToken<List<Student>>() {}.getType();
        List<Student> students = gson.fromJson(listJson, studentListType);

        System.out.println("--- Generic List (JSON array -> List<Student>) ---");
        for (Student s : students) {
            System.out.println(s);
        }
        System.out.println();

        // Quick verification test confirming the round trip worked
        boolean passed = parsedStudent.getName().equals("Zachary White")
                && parsedStudent.getMajor().equals("Software Development")
                && parsedStudent.getGradYear() == 2026
                && parsedStudent.isFullTime()
                && students.size() == 2
                && students.get(0).getName().equals("Aisha Khan");

        System.out.println("Round-trip test: " + (passed ? "PASSED" : "FAILED"));
    }
}
