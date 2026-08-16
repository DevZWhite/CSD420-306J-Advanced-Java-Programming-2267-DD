/**
 * Author: Zachary White
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 1 Programming Assignment - Random Card Display
 * Date: 17 August 2026
 *
 * Description:
 * A JavaFX application that shows four randomly chosen playing cards
 * pulled from a 52-card deck. Clicking the "New Hand" button below the
 * cards swaps in four newly chosen cards. All event handling and the
 * random draw logic are implemented with lambda expressions.
 */

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.List;
import java.util.Random;

public class CardDisplay extends Application {

    // Total cards in a standard deck, and how many are shown at once
    private static final int DECK_SIZE = 52;
    private static final int HAND_SIZE = 4;

    // Pixel dimensions used to render each card image
    private static final double CARD_WIDTH = 120;
    private static final double CARD_HEIGHT = 180;

    // Folder (relative to the working directory) that holds 1.png .. 52.png
    private static final String CARD_FOLDER = "cards/";

    // Reused ImageView slots; refresh only swaps the images, not the layout
    private final ImageView[] handSlots = new ImageView[HAND_SIZE];

    private final Random rng = new Random();

    @Override
    public void start(Stage stage) {
        Label heading = new Label("Your Hand");
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox handBox = new HBox(15);
        handBox.setAlignment(Pos.CENTER);
        handBox.setPadding(new Insets(20));

        // Build the four reusable ImageView placeholders via lambda expression
        IntStream.range(0, HAND_SIZE).forEach(i -> {
            ImageView slot = new ImageView();
            slot.setFitWidth(CARD_WIDTH);
            slot.setFitHeight(CARD_HEIGHT);
            slot.setPreserveRatio(true);
            handSlots[i] = slot;
            handBox.getChildren().add(slot);
        });

        Button newHandButton = new Button("New Hand");
        newHandButton.setPrefWidth(130);
        // Lambda expression wired to the button's click event
        newHandButton.setOnAction(e -> dealNewHand());

        VBox layout = new VBox(15, heading, handBox, newHandButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));

        // Show an initial hand as soon as the window opens
        dealNewHand();

        Scene scene = new Scene(layout, 620, 320);
        stage.setTitle("Random Card Display");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Picks four distinct card numbers between 1 and 52 (inclusive) and
     * loads the matching images into the on-screen slots. Distinct values
     * are guaranteed by drawing from a shuffled stream of the full deck.
     */
    private void dealNewHand() {
        List<Integer> chosen = IntStream.rangeClosed(1, DECK_SIZE)
                .boxed()
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    java.util.Collections.shuffle(list, rng);
                    return list.subList(0, HAND_SIZE);
                }));

        // Lambda expression applies each chosen card number to its slot
        IntStream.range(0, HAND_SIZE).forEach(i -> {
            int cardNumber = chosen.get(i);
            Image cardImage = new Image("file:" + CARD_FOLDER + cardNumber + ".png");
            handSlots[i].setImage(cardImage);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}