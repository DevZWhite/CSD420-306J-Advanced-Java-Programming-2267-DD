/**
 * Author: Zachary White
 * Course: CSD 420 - Advanced Java Programming
 * Professor: Darrell Payne
 * Date: October 2, 2026
 * Assignment: Fan Manager - JavaFX / JDBC CRUD Interface
 *
 * A JavaFX front end for the "fans" table in databasedb. The application
 * only ever SELECTs and UPDATEs rows -- it never creates or drops the
 * table, since the grading environment supplies it already populated.
 *
 * Table "fans": ID (int, PK), firstname (varchar25),
 * lastname (varchar25), favoriteteam (varchar25)
 */
package com.mycompany.fanmanagerproject;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
public class FanManager extends Application {
 
    // JDBC connection details matching the assignment requirements
    private static final String URL =
            "jdbc:mysql://localhost:3306/databasedb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "student1";
    private static final String PASSWORD = "pass";
 
    // Input fields shared between the Display and Update handlers
    private final TextField idField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField favoriteTeamField = new TextField();
 
    // Status message shown below the buttons after each action
    private final Label statusLabel = new Label(" ");
 
    // Tracks mouse-press offset so the custom title bar can drag the window
    private double dragOffsetX;
    private double dragOffsetY;
 
    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UNDECORATED);
 
        HBox titleBar = buildTitleBar(primaryStage);
 
        Label titleLabel = new Label("Fan Manager");
        titleLabel.getStyleClass().add("title-label");
 
        // Form layout: labels on the left, text fields on the right
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(10, 0, 10, 0));
 
        Label idLabel = new Label("ID:");
        Label firstLabel = new Label("First Name:");
        Label lastLabel = new Label("Last Name:");
        Label teamLabel = new Label("Favorite Team:");
        for (Label l : new Label[]{idLabel, firstLabel, lastLabel, teamLabel}) {
            l.getStyleClass().add("form-label");
        }
 
        form.add(idLabel, 0, 0);
        form.add(idField, 1, 0);
        form.add(firstLabel, 0, 1);
        form.add(firstNameField, 1, 1);
        form.add(lastLabel, 0, 2);
        form.add(lastNameField, 1, 2);
        form.add(teamLabel, 0, 3);
        form.add(favoriteTeamField, 1, 3);
 
        // Buttons wired to their handlers using lambdas
        Button displayButton = new Button("Display");
        Button updateButton = new Button("Update");
        displayButton.getStyleClass().add("display-button");
        updateButton.getStyleClass().add("update-button");
        displayButton.setOnAction(e -> displayRecord());
        updateButton.setOnAction(e -> updateRecord());
 
        HBox buttonRow = new HBox(15, displayButton, updateButton);
        buttonRow.setAlignment(Pos.CENTER);
 
        statusLabel.getStyleClass().add("status-label");
 
        VBox content = new VBox(18, titleLabel, form, buttonRow, statusLabel);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("content-area");
 
        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        root.setCenter(content);
        root.getStyleClass().add("root");
 
        Scene scene = new Scene(root, 360, 480);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/fanmanager.css").toExternalForm());
 
        primaryStage.setTitle("Fan Manager");
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/fanmanager-icon.png")));
        } catch (Exception ignored) {
            // Icon is optional -- app still runs fine without it if the file is missing
        }
        primaryStage.setScene(scene);
        primaryStage.show();
 
        // Run the built-in tests once the UI is up so results print
        // to the console for verification
        runTests();
    }
 
    /**
     * Builds a custom dark title bar to replace the OS-native one (the
     * window uses StageStyle.UNDECORATED). Includes the app name/icon,
     * a drag handle so the window can still be moved, and minimize/close
     * buttons since the OS no longer provides them.
     */
    private HBox buildTitleBar(Stage stage) {
        Label appIcon = new Label("\u2B50");
        appIcon.getStyleClass().add("title-bar-icon");
 
        Label appName = new Label("Fan Manager");
        appName.getStyleClass().add("title-bar-text");
 
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
 
        Button minimizeButton = new Button("\u2013");
        minimizeButton.getStyleClass().add("title-bar-button");
        minimizeButton.setOnAction(e -> stage.setIconified(true));
 
        Button closeButton = new Button("\u2715");
        closeButton.getStyleClass().addAll("title-bar-button", "title-bar-close");
        closeButton.setOnAction(e -> stage.close());
 
        HBox titleBar = new HBox(8, appIcon, appName, spacer, minimizeButton, closeButton);
        titleBar.getStyleClass().add("title-bar");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(8, 10, 8, 14));
 
        // Make the bar draggable since the window no longer has a native one
        titleBar.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        titleBar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });
 
        return titleBar;
    }
 
    /**
     * Looks up the record whose ID matches the ID field and fills the
     * other fields with the values from the database. UI-facing wrapper
     * around the static, independently testable fetchFan() method.
     */
    private void displayRecord() {
        int id;
        try {
            id = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            setStatus("ID must be a number.", false);
            return;
        }
 
        try {
            FanRecord record = fetchFan(id);
            if (record != null) {
                firstNameField.setText(record.firstName);
                lastNameField.setText(record.lastName);
                favoriteTeamField.setText(record.favoriteTeam);
                setStatus("Record displayed for ID " + id + ".", true);
            } else {
                firstNameField.clear();
                lastNameField.clear();
                favoriteTeamField.clear();
                setStatus("No record found for ID " + id + ".", false);
            }
        } catch (SQLException e) {
            setStatus("Database error: " + e.getMessage(), false);
        }
    }
 
    /** Updates the status label's text and applies the matching CSS class. */
    private void setStatus(String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add(isSuccess ? "status-success" : "status-error");
    }
 
    /**
     * Applies the current values in all four fields to the matching
     * row in the database. UI-facing wrapper around the static,
     * independently testable applyUpdate() method.
     */
    private void updateRecord() {
        int id;
        try {
            id = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            setStatus("ID must be a number.", false);
            return;
        }
 
        FanRecord record = new FanRecord(id, firstNameField.getText().trim(),
                lastNameField.getText().trim(), favoriteTeamField.getText().trim());
 
        try {
            boolean updated = applyUpdate(record);
            setStatus(updated
                    ? "Record updated for ID " + id + "."
                    : "No record found for ID " + id + ".", updated);
        } catch (SQLException e) {
            setStatus("Database error: " + e.getMessage(), false);
        }
    }
 
    /**
     * Simple immutable holder for one row of the fans table, used to pass
     * data between the UI and the static DB-access methods below.
     */
    static class FanRecord {
        final int id;
        final String firstName;
        final String lastName;
        final String favoriteTeam;
 
        FanRecord(int id, String firstName, String lastName, String favoriteTeam) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.favoriteTeam = favoriteTeam;
        }
    }
 
    /**
     * Fetches the fan with the given ID. Static and UI-independent so it
     * can be called directly from test code. Returns null if no row
     * matches the given ID.
     */
    static FanRecord fetchFan(int id) throws SQLException {
        String sql = "SELECT firstname, lastname, favoriteteam FROM fans WHERE ID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new FanRecord(id, rs.getString("firstname"),
                            rs.getString("lastname"), rs.getString("favoriteteam"));
                }
                return null;
            }
        }
    }
 
    /**
     * Writes the given record's values to the matching row in the fans
     * table. Static and UI-independent so it can be called directly from
     * test code. Returns true if a row was actually changed.
     */
    static boolean applyUpdate(FanRecord record) throws SQLException {
        String sql = "UPDATE fans SET firstname = ?, lastname = ?, "
                + "favoriteteam = ? WHERE ID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, record.firstName);
            stmt.setString(2, record.lastName);
            stmt.setString(3, record.favoriteTeam);
            stmt.setInt(4, record.id);
            return stmt.executeUpdate() > 0;
        }
    }
 
    /**
     * Basic startup tests that verify a Connection can be established
     * and that the fans table is reachable. Results print to the
     * console so they show up alongside compile output.
     */
    private void runTests() {
        System.out.println("--- Running startup tests ---");
 
        // Test 1: connection can be established with the required credentials
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Test 1 (Connection to databasedb): "
                    + (conn.isValid(2) ? "PASSED" : "FAILED"));
 
            // Test 2: fans table exists and can be counted
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) AS n FROM fans");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Test 2 (fans table reachable): PASSED");
                    System.out.println("  Row count: " + rs.getInt("n"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Startup tests FAILED: " + e.getMessage());
        }
 
        System.out.println();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}
 


