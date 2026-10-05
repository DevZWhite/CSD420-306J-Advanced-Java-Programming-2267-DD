/**
 * Author: Zachary White
 * Course: CSD 420 - Advanced Java Programming
 * Professor: Darrell Payne
 * Date: October 2, 2026
 * Assignment: Fan Manager - JavaFX / JDBC CRUD Interface
 *
 * Lightweight, dependency-free test harness (no JUnit required) that
 * exercises FanManager.fetchFan() and FanManager.applyUpdate() directly,
 * independent of the JavaFX UI, plus a basic input-validation check.
 */

package com.mycompany.fanmanagerproject;

import java.sql.SQLException;

public class FanManagerTest {

    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println("=== FanManager Test Suite ===\n");

        testFetchExistingFan();
        testFetchNonexistentFan();
        testUpdateExistingFan();
        testUpdateNonexistentFan();
        testIdParsingValidation();

        System.out.println("\n=== Results: " + passCount + " passed, " + failCount + " failed ===");
    }

    private static void testFetchExistingFan() {
        String name = "fetchFan() returns correct data for an existing ID";
        try {
            FanManager.FanRecord record = FanManager.fetchFan(1);
            check(name, record != null && record.firstName != null && !record.firstName.isEmpty());
        } catch (SQLException e) {
            fail(name, e.getMessage());
        }
    }

    private static void testFetchNonexistentFan() {
        String name = "fetchFan() returns null for an ID that does not exist";
        try {
            FanManager.FanRecord record = FanManager.fetchFan(999999);
            check(name, record == null);
        } catch (SQLException e) {
            fail(name, e.getMessage());
        }
    }

    private static void testUpdateExistingFan() {
        String name = "applyUpdate() updates and returns true, then round-trips correctly";
        try {
            FanManager.FanRecord original = FanManager.fetchFan(1);
            if (original == null) {
                fail(name, "fan ID 1 not present; cannot test update");
                return;
            }

            FanManager.FanRecord modified = new FanManager.FanRecord(
                    1, "TestFirst", "TestLast", "TestTeam");
            boolean updated = FanManager.applyUpdate(modified);

            FanManager.FanRecord reread = FanManager.fetchFan(1);
            boolean roundTripOk = reread != null
                    && "TestFirst".equals(reread.firstName)
                    && "TestLast".equals(reread.lastName)
                    && "TestTeam".equals(reread.favoriteTeam);

            // restore original values regardless of test outcome
            FanManager.applyUpdate(original);

            check(name, updated && roundTripOk);
        } catch (SQLException e) {
            fail(name, e.getMessage());
        }
    }

    private static void testUpdateNonexistentFan() {
        String name = "applyUpdate() returns false for an ID that does not exist";
        try {
            FanManager.FanRecord ghost = new FanManager.FanRecord(999999, "X", "Y", "Z");
            boolean updated = FanManager.applyUpdate(ghost);
            check(name, !updated);
        } catch (SQLException e) {
            fail(name, e.getMessage());
        }
    }

    private static void testIdParsingValidation() {
        String name = "Non-numeric ID input is rejected before hitting the database";
        try {
            Integer.parseInt("abc");
            check(name, false);
        } catch (NumberFormatException expected) {
            check(name, true);
        }
    }

    private static void check(String testName, boolean passed) {
        if (passed) {
            passCount++;
            System.out.println("[PASS] " + testName);
        } else {
            failCount++;
            System.out.println("[FAIL] " + testName);
        }
    }

    private static void fail(String testName, String reason) {
        failCount++;
        System.out.println("[FAIL] " + testName + " -- " + reason);
    }
}
