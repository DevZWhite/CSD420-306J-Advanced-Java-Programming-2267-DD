/**
 * Author: Zachary White
 * Course: CSD 420 - Advanced Java Programming
 * Professor: Darrell Payne
 * Date: October 11, 2026
 * Assignment: Fan Manager - JavaFX / JDBC CRUD Interface
 *
 * A JavaFX front end for the "fans" table in databasedb. The application
 * only ever SELECTs and UPDATEs rows -- it never creates or drops the
 * table, since the grading environment supplies it already populated.
 *
 * Table "fans": ID (int, PK), firstname (varchar25),
 * lastname (varchar25), favoriteteam (varchar25)
 */


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
 
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
 
public class FanManager extends Application {
 
    // Stylesheet embedded in the source so the program has no external file
    // dependencies and runs as a single self-contained class.
    private static final String STYLE_SHEET = """
            /* Fan Manager - stylesheet
               Author: Zachary White
               Course: CSD 420 */

            .root {
                -fx-background-color: transparent;
            }

            .content-area {
                -fx-alignment: center;
                -fx-background-color: linear-gradient(to bottom right, #1f2937, #0f172a);
                -fx-background-radius: 0 0 14 14;
            }

            /* ---- Custom title bar ---- */

            .title-bar {
                -fx-background-color: #111827;
                -fx-background-radius: 14 14 0 0;
                -fx-border-color: transparent transparent #1f2937 transparent;
                -fx-border-width: 0 0 1 0;
            }

            .title-bar-icon {
                -fx-font-size: 14px;
            }

            .title-bar-text {
                -fx-text-fill: #e5e7eb;
                -fx-font-size: 12px;
                -fx-font-weight: 600;
                -fx-letter-spacing: 0.5px;
            }

            .title-bar-button {
                -fx-background-color: transparent;
                -fx-text-fill: #9ca3af;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 5;
                -fx-min-width: 30px;
                -fx-min-height: 26px;
                -fx-padding: 0;
                -fx-cursor: hand;
            }

            .title-bar-button:hover {
                -fx-background-color: #374151;
                -fx-text-fill: #f9fafb;
            }

            .title-bar-close:hover {
                -fx-background-color: #dc2626;
                -fx-text-fill: white;
            }

            /* ---- Content ---- */

            .title-label {
                -fx-text-fill: #ffffff;
                -fx-font-size: 23px;
                -fx-font-weight: bold;
                -fx-padding: 0 0 6 0;
            }

            .form-label {
                -fx-text-fill: #9ca3af;
                -fx-font-size: 12px;
                -fx-font-weight: 600;
            }

            .text-field {
                -fx-alignment: center;
                -fx-background-color: #151e2d;
                -fx-text-fill: #f9fafb;
                -fx-prompt-text-fill: #6b7280;
                -fx-background-radius: 8;
                -fx-border-color: #2d3748;
                -fx-border-radius: 8;
                -fx-border-width: 1.2;
                -fx-padding: 9 12 9 12;
                -fx-font-size: 13px;
            }

            .text-field:focused {
                -fx-alignment: center;
                -fx-border-color: #3b82f6;
                -fx-border-width: 1.6;
                -fx-background-color: #17233a;
            }

            .button {
                -fx-background-radius: 8;
                -fx-font-size: 13px;
                -fx-font-weight: 600;
                -fx-padding: 10 24 10 24;
                -fx-cursor: hand;
            }

            .display-button {
                -fx-background-color: #2d3748;
                -fx-text-fill: #f1f5f9;
            }

            .display-button:hover {
                -fx-background-color: #3a4a61;
            }

            .update-button {
                -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);
                -fx-text-fill: white;
            }

            .update-button:hover {
                -fx-background-color: linear-gradient(to right, #2f6fe0, #1d4ed8);
            }

            .status-label {
                -fx-font-size: 12px;
                -fx-padding: 6 0 0 0;
            }

            .status-success {
                -fx-text-fill: #34d399;
            }

            .status-error {
                -fx-text-fill: #f87171;
            }
            """;

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
        applyStyles(scene);
 
        primaryStage.setTitle("Fan Manager");
        try {
            primaryStage.getIcons().add(buildIcon());
        } catch (Exception ignored) {
            // Icon is cosmetic -- the app still runs normally without it
        }
        primaryStage.setScene(scene);
        primaryStage.show();
 
        // Run the built-in tests once the UI is up so results print
        // to the console for verification
        runTests();
    }
 
    /**
     * Applies the embedded stylesheet. It is written to a temporary file and
     * loaded by URI; if that is not possible a data: URI is used instead.
     * Styling is cosmetic, so a failure here never stops the application.
     */
    private void applyStyles(Scene scene) {
        try {
            Path cssFile = Files.createTempFile("fanmanager", ".css");
            Files.write(cssFile, STYLE_SHEET.getBytes(StandardCharsets.UTF_8));
            cssFile.toFile().deleteOnExit();
            scene.getStylesheets().add(cssFile.toUri().toString());
        } catch (IOException | RuntimeException e) {
            try {
                String encoded = Base64.getEncoder()
                        .encodeToString(STYLE_SHEET.getBytes(StandardCharsets.UTF_8));
                scene.getStylesheets().add("data:text/css;base64," + encoded);
            } catch (RuntimeException ignored) {
                // fall back to the default JavaFX look
            }
        }
    }

    /** Draws the application icon in code so no image file is needed. */
    private Image buildIcon() {
        Canvas canvas = new Canvas(128, 128);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.web("#2563eb"));
        g.fillRoundRect(4, 4, 120, 120, 34, 34);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("System", FontWeight.BOLD, 76));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText("F", 64, 66);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, new WritableImage(128, 128));
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
 


