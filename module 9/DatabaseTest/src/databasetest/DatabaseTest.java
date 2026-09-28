/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package databasetest;

/**
 * Author: Zachary White
 * Course: CSD 420 Advanced Java Programming
 * Professor: Darrell Payne
 * Assignment: Module 9 Programming Assignment - MySQL connection test
 * Date: September 25 2026
 */

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTest {

    private static final String URL =
            "jdbc:mysql://localhost:3306/databasedb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "student1";
    private static final String PASSWORD = "pass";

    public static void main(String[] args) {
        System.out.println("============================");
        System.out.println("MySQL | JDBC Connection Test");
        System.out.println("============================");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // --- Connection status ---
            System.out.println("[1] Connection Status");
            System.out.println("    -> " + (conn.isValid(2) ? "Connected successfully" : "Connection invalid"));
            System.out.println();

            // --- Environment / version details ---
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("[2] Environment Details");
            System.out.println("    JDBC Driver Name:    " + meta.getDriverName());
            System.out.println("    JDBC Driver Version: " + meta.getDriverVersion());
            System.out.println("    Database Product:    " + meta.getDatabaseProductName());
            System.out.println("    Database Version:    " + meta.getDatabaseProductVersion());
            System.out.println("    JDBC URL:             " + meta.getURL());
            System.out.println("    Connected User:       " + meta.getUserName());
            System.out.println("    Java Runtime Version: " + System.getProperty("java.version"));
            System.out.println();

            // --- Current user / server version check ---
            System.out.println("[3] Server-Side Check");
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT CURRENT_USER() AS user, VERSION() AS version, DATABASE() AS db")) {
                if (rs.next()) {
                    System.out.println("    Current User: " + rs.getString("user"));
                    System.out.println("    MySQL Version: " + rs.getString("version"));
                    System.out.println("    Active Database: " + rs.getString("db"));
                }
            }
            System.out.println();

            // --- Data readout, formatted as a table ---
            System.out.println("[4] Table Contents: address_test");
            String header = String.format("    %-4s %-12s %-12s %-22s %-12s %-6s %-8s",
                    "ID", "LASTNAME", "FIRSTNAME", "STREET", "CITY", "STATE", "ZIP");
            System.out.println(header);
            System.out.println("    " + "-".repeat(header.length() - 4));

            int rowCount = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM address_test ORDER BY ID")) {
                while (rs.next()) {
                    System.out.printf("    %-4d %-12s %-12s %-22s %-12s %-6s %-8s%n",
                            rs.getInt("ID"),
                            rs.getString("LASTNAME"),
                            rs.getString("FIRSTNAME"),
                            rs.getString("STREET"),
                            rs.getString("CITY"),
                            rs.getString("STATE"),
                            rs.getString("ZIP"));
                    rowCount++;
                }
            }
            System.out.println("    (" + rowCount + " row" + (rowCount == 1 ? "" : "s") + " returned)");

            System.out.println("\n=================================================");
            System.out.println(" Setup verified: connection, metadata, and data");
            System.out.println(" retrieval all completed without errors.");
            System.out.println("=================================================");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}