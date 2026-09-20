/**
 * File: Colors_in_JavaFX.java
 * Author: Zachary White
 * Professor: Darrell Payne
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 7 Programming Assignment - JavaFX CSS
 * Date: September 15th 2026
 */

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Colors_in_JavaFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Circles 1 and 2 are styled with a CSS class (reusable on many nodes)
        Circle circle1 = new Circle(30);
        circle1.getStyleClass().add("plainStyle");   // class selector: .plainStyle

        Circle circle2 = new Circle(30);
        circle2.getStyleClass().add("plainStyle");   // same class reused

        // Circles 3 and 4 are styled with a CSS ID (unique to one node)
        Circle circle3 = new Circle(30);
        circle3.setId("redStyle");                   // ID selector: #redStyle

        Circle circle4 = new Circle(30);
        circle4.setId("greenStyle");                 // ID selector: #greenStyle

        // The first circle sits inside a tall StackPane that uses the
        // .border class, which draws the thick black box from the sample image
        StackPane borderBox = new StackPane(circle1);
        borderBox.getStyleClass().add("border");     // class selector: .border
        borderBox.setPrefSize(72, 245);
        borderBox.setMinSize(72, 245);

        // HBox places the boxed circle and the other three side by side.
        // CENTER alignment keeps every circle at the same vertical middle.
        HBox root = new HBox(6, borderBox, circle2, circle3, circle4);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        // Create the scene and attach the external stylesheet.
        // mystyle.css must be in the same folder/package as this class.
        Scene scene = new Scene(root, 300, 270);
        scene.getStylesheets().add(
                getClass().getResource("mystyle.css").toExternalForm());

        primaryStage.setTitle("Colors In JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Run the checks after the scene is shown so CSS has been applied
        runTests(circle1, circle2, circle3, circle4, borderBox, scene);
    }

    /**
     * Test code: confirms the stylesheet loaded, that each node has the
     * expected style class or ID, and that the CSS produced the look shown
     * in the assignment sample. Results print to the console.
     */
    private void runTests(Circle c1, Circle c2, Circle c3, Circle c4,
                          StackPane box, Scene scene) {
        System.out.println("--- Running tests ---");

        // Test 1: the stylesheet was actually found and attached
        boolean t1 = !scene.getStylesheets().isEmpty();
        System.out.println("Test 1 (mystyle.css loaded): " + (t1 ? "PASSED" : "FAILED"));

        // Tests 2-3: the first two circles use the class
        boolean t2 = c1.getStyleClass().contains("plainStyle");
        System.out.println("Test 2 (circle1 uses .plainStyle): " + (t2 ? "PASSED" : "FAILED"));

        boolean t3 = c2.getStyleClass().contains("plainStyle");
        System.out.println("Test 3 (circle2 uses .plainStyle): " + (t3 ? "PASSED" : "FAILED"));

        // Tests 4-5: the last two circles use IDs
        boolean t4 = "redStyle".equals(c3.getId());
        System.out.println("Test 4 (circle3 has #redStyle): " + (t4 ? "PASSED" : "FAILED"));

        boolean t5 = "greenStyle".equals(c4.getId());
        System.out.println("Test 5 (circle4 has #greenStyle): " + (t5 ? "PASSED" : "FAILED"));

        // Test 6: CSS actually applied the fill colors (checks the real result)
        boolean t6 = c1.getFill().toString().equals("0xffffffff")
                && c3.getFill().toString().equals("0xff0000ff")
                && c4.getFill().toString().equals("0x008000ff");
        System.out.println("Test 6 (CSS fill colors applied): " + (t6 ? "PASSED" : "FAILED"));

        // Test 7: the box around circle 1 uses the .border class and holds circle 1
        boolean t7 = box.getStyleClass().contains("border")
                && box.getChildren().contains(c1);
        System.out.println("Test 7 (circle1 is inside the .border box): " + (t7 ? "PASSED" : "FAILED"));

    }

    public static void main(String[] args) {
        launch(args);
    }
}