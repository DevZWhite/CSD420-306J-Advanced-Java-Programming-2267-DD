/**
 * File: ZacharyThreeThreads.java
 * Author: Zachary White
 * Professor: Darrell Payne
 * Course: CSD 420 Advanced Java Programming
 * Assignment: Module 8 Programming Assignment - Multithreading
 * Date: September 15th 2026
 *
 * Description:
 * This JavaFX program demonstrates multithreading by running three threads
 * at once. Every thread creates 10,000 characters of its own type and adds
 * each one to a shared TextArea as soon as it is created:
 *   - Thread 1: lowercase letters (a-z)
 *   - Thread 2: digits (0-9)
 *   - Thread 3: special characters (! @ # $ % & *)
 *
 * Since all three threads work at the same time, their characters end up
 * mixed together in the TextArea (something like 4k$m9!c2&x) rather than
 * appearing in three separate blocks.
 *
 * A different approach is used to generate each type of character:
 *   - Letters : adds a random offset to the ASCII value of 'a' (java.util.Random)
 *   - Digits  : converts a random number with Character.forDigit() (ThreadLocalRandom)
 *   - Symbols : picks a random index from a char[] array (Math.random())
 *
 * Built-in tests print PASSED/FAILED results to the console.
 */

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ZacharyThreeThreads extends Application {

    // ----- Settings -----------------------------------------------------

    /** How many characters each thread must produce (assignment minimum is 10,000). */
    private static final int CHARS_PER_THREAD = 10_000;

    /**
     * Tiny pause after each character. Without it, one thread can finish
     * before the others get going, and the output would look grouped.
     * The pause lets the threads take turns so the mixing is easy to see.
     */
    private static final int DELAY_MS = 1;

    /** How often (in milliseconds) the live counter labels refresh. */
    private static final int REFRESH_MS = 50;

    /** The pool of special characters used by the third thread. */
    private static final char[] SYMBOLS = {'!', '@', '#', '$', '%', '&', '*'};

    // Random source used by the letter generator
    private static final Random LETTER_RNG = new Random();

    // ----- Shared state -------------------------------------------------

    private TextArea outputArea;      // shared display for all three threads
    private Label statusLabel;        // shows progress and the final test result

    // Controls
    private Button startButton;
    private Button clearButton;

    // Live counter labels (one per thread)
    private Label letterLabel;
    private Label digitLabel;
    private Label symbolLabel;

    // Refreshes the live counter labels while a run is in progress
    private Timeline refreshTimer;

    // Thread-safe counters so each thread can report how many chars it made
    private final AtomicInteger letterCount = new AtomicInteger();
    private final AtomicInteger digitCount = new AtomicInteger();
    private final AtomicInteger symbolCount = new AtomicInteger();

    // Running totals for the test report
    private int testsPassed = 0;
    private int testsRun = 0;
    private int startupTestsRun = 0;      // tests that run once at launch
    private int startupTestsPassed = 0;
    private int runNumber = 0;            // how many times Start has been pressed

    // ----- Character generators (one technique per type) ----------------

    /** Letters: start at 'a' and add a random offset from 0 to 25. */
    static char generateLetter() {
        return (char) ('a' + LETTER_RNG.nextInt(26));
    }

    /** Digits: turn a random number from 0 to 9 into its digit character. */
    static char generateDigit() {
        return Character.forDigit(ThreadLocalRandom.current().nextInt(10), 10);
    }

    /** Symbols: pick a random position in the SYMBOLS array. */
    static char generateSymbol() {
        return SYMBOLS[(int) (Math.random() * SYMBOLS.length)];
    }

    // ----- JavaFX setup -------------------------------------------------

    @Override
    public void start(Stage primaryStage) {
        // Header shows the author's name on the app itself
        Label header = new Label("Three Threads Demo  |  Zachary White");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Start launches a run; Clear wipes the output and counters
        startButton = new Button("Start");
        clearButton = new Button("Clear");
        startButton.setOnAction(e -> startRun());
        clearButton.setOnAction(e -> resetRun());

        HBox buttonBar = new HBox(10, startButton, clearButton);
        buttonBar.setAlignment(Pos.CENTER);

        VBox topBox = new VBox(8, header, buttonBar);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));

        outputArea = new TextArea();
        outputArea.setWrapText(true);
        outputArea.setEditable(false);

        // Live counters, one label per thread
        letterLabel = new Label();
        digitLabel = new Label();
        symbolLabel = new Label();
        updateCounterLabels();

        HBox counterBox = new HBox(25, letterLabel, digitLabel, symbolLabel);
        counterBox.setAlignment(Pos.CENTER);

        statusLabel = new Label("Ready. Press Start.");

        VBox bottomBox = new VBox(5, counterBox, statusLabel);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(outputArea);
        root.setBottom(bottomBox);

        // Timer that updates the counter labels about 20 times per second
        refreshTimer = new Timeline(
                new KeyFrame(Duration.millis(REFRESH_MS), e -> updateCounterLabels()));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("ZacharyThreeThreads - Zachary White");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Tests that can run before any threads start
        runGeneratorTests();
        runControlTests();

        // Remember the startup totals so each run's report can add to them
        startupTestsRun = testsRun;
        startupTestsPassed = testsPassed;
    }

    // ----- Button actions -----------------------------------------------

    /**
     * Start button: clears any old output, then launches the three
     * producer threads. Both buttons stay disabled until the run ends so
     * the user cannot start a second run or clear the text mid-run.
     */
    private void startRun() {
        runNumber++;
        resetRun();

        startButton.setDisable(true);
        clearButton.setDisable(true);
        statusLabel.setText("Run " + runNumber + ": generating characters...");
        refreshTimer.play();

        // Build the three threads. Each one gets its own generator method
        // and its own counter.
        final Thread letterThread = new Thread(
                new CharacterProducer(ZacharyThreeThreads::generateLetter, letterCount), "LetterThread");
        final Thread digitThread = new Thread(
                new CharacterProducer(ZacharyThreeThreads::generateDigit, digitCount), "DigitThread");
        final Thread symbolThread = new Thread(
                new CharacterProducer(ZacharyThreeThreads::generateSymbol, symbolCount), "SymbolThread");

        letterThread.start();
        digitThread.start();
        symbolThread.start();

        // A helper thread waits for all three producers to finish, then
        // schedules the output tests. Waiting here keeps the JavaFX thread free.
        Thread waiter = new Thread(() -> {
            try {
                letterThread.join();
                digitThread.join();
                symbolThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            // runLater goes to the back of the queue, so every character
            // append has already run by the time the tests execute.
            Platform.runLater(this::runOutputTests);
        }, "TestWaiterThread");
        waiter.setDaemon(true);
        waiter.start();
    }

    /**
     * Clear button (also called at the start of every run): empties the
     * TextArea and sets all three counters back to zero.
     */
    private void resetRun() {
        outputArea.clear();
        letterCount.set(0);
        digitCount.set(0);
        symbolCount.set(0);
        updateCounterLabels();
        statusLabel.setText("Ready. Press Start.");
    }

    /** Copies the current counter values into the live labels. */
    private void updateCounterLabels() {
        letterLabel.setText("Letters: " + letterCount.get());
        digitLabel.setText("Digits: " + digitCount.get());
        symbolLabel.setText("Symbols: " + symbolCount.get());
    }

    // ----- Producer task ------------------------------------------------

    /**
     * Task run by each thread. It repeatedly asks its generator for a
     * character and sends that character to the TextArea right away.
     */
    private class CharacterProducer implements Runnable {
        private final Supplier<Character> generator;
        private final AtomicInteger counter;

        CharacterProducer(Supplier<Character> generator, AtomicInteger counter) {
            this.generator = generator;
            this.counter = counter;
        }

        @Override
        public void run() {
            for (int i = 0; i < CHARS_PER_THREAD; i++) {
                char c = generator.get();

                // UI controls may only be changed on the JavaFX thread,
                // so hand the append over with Platform.runLater.
                Platform.runLater(() -> outputArea.appendText(String.valueOf(c)));
                counter.incrementAndGet();

                try {
                    Thread.sleep(DELAY_MS);   // let the other threads take a turn
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    // ----- Tests --------------------------------------------------------

    /** Prints one PASSED/FAILED line and updates the totals. */
    private void report(String description, boolean passed) {
        testsRun++;
        if (passed) {
            testsPassed++;
        }
        System.out.println("Test " + testsRun + " (" + description + "): "
                + (passed ? "PASSED" : "FAILED"));
    }

    /**
     * Tests 1-7: check each generator method by calling it many times.
     * These run once at launch, before any threads start.
     */
    private void runGeneratorTests() {
        System.out.println("--- Generator tests ---");

        // Sample each generator 10,000 times, remembering which values appeared
        boolean lettersInRange = true;
        boolean digitsInRange = true;
        boolean symbolsInRange = true;
        boolean[] letterSeen = new boolean[26];
        boolean[] digitSeen = new boolean[10];
        boolean[] symbolSeen = new boolean[SYMBOLS.length];

        for (int i = 0; i < 10_000; i++) {
            char l = generateLetter();
            if (l < 'a' || l > 'z') {
                lettersInRange = false;
            } else {
                letterSeen[l - 'a'] = true;
            }

            char d = generateDigit();
            if (d < '0' || d > '9') {
                digitsInRange = false;
            } else {
                digitSeen[d - '0'] = true;
            }

            char s = generateSymbol();
            int index = indexOfSymbol(s);
            if (index < 0) {
                symbolsInRange = false;
            } else {
                symbolSeen[index] = true;
            }
        }

        report("generateLetter only returns a-z", lettersInRange);
        report("generateLetter produced all 26 letters", allTrue(letterSeen));
        report("generateDigit only returns 0-9", digitsInRange);
        report("generateDigit produced all 10 digits", allTrue(digitSeen));
        report("generateSymbol only returns the allowed symbols", symbolsInRange);
        report("generateSymbol produced all " + SYMBOLS.length + " symbols", allTrue(symbolSeen));
        report("each thread is set to produce at least 10,000 chars", CHARS_PER_THREAD >= 10_000);
        System.out.println();
    }

    /**
     * Tests 8-9: check the buttons and reset logic. These run once at
     * launch. Test 9 fills the TextArea and counters with dummy data, calls
     * the same method the Clear button uses, and confirms everything is empty.
     */
    private void runControlTests() {
        System.out.println("--- Control tests ---");

        report("Start and Clear buttons are enabled before a run",
                !startButton.isDisabled() && !clearButton.isDisabled());

        // Put dummy data in place, then clear it the same way the button does
        outputArea.setText("dummy text");
        letterCount.set(7);
        digitCount.set(8);
        symbolCount.set(9);
        resetRun();

        report("Clear empties the TextArea and resets all counters and labels",
                outputArea.getText().isEmpty()
                        && letterCount.get() == 0
                        && digitCount.get() == 0
                        && symbolCount.get() == 0
                        && letterLabel.getText().equals("Letters: 0")
                        && digitLabel.getText().equals("Digits: 0")
                        && symbolLabel.getText().equals("Symbols: 0"));
        System.out.println();
    }

    /**
     * Tests 10-15: check the finished TextArea after all three threads
     * have completed. Runs on the JavaFX thread at the end of every run.
     */
    private void runOutputTests() {
        // The run is over: stop the timer, show the final numbers, and
        // let the user press Start or Clear again
        refreshTimer.stop();
        updateCounterLabels();
        startButton.setDisable(false);
        clearButton.setDisable(false);

        // Start from the launch totals so each run's report is self-contained
        testsRun = startupTestsRun;
        testsPassed = startupTestsPassed;

        System.out.println("--- Output tests (run " + runNumber + ") ---");

        String text = outputArea.getText();

        // Count each category and how often the category changes
        int letters = 0, digits = 0, symbols = 0, switches = 0;
        int previousType = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int type;
            if (c >= 'a' && c <= 'z') {
                type = 0;
                letters++;
            } else if (c >= '0' && c <= '9') {
                type = 1;
                digits++;
            } else {
                type = 2;
                symbols++;
            }
            if (previousType != -1 && type != previousType) {
                switches++;
            }
            previousType = type;
        }

        report("each thread's counter reached " + CHARS_PER_THREAD,
                letterCount.get() == CHARS_PER_THREAD
                        && digitCount.get() == CHARS_PER_THREAD
                        && symbolCount.get() == CHARS_PER_THREAD);
        report("TextArea holds " + (3 * CHARS_PER_THREAD) + " characters in total",
                text.length() == 3 * CHARS_PER_THREAD);
        report("TextArea has " + CHARS_PER_THREAD + " of each character type",
                letters == CHARS_PER_THREAD && digits == CHARS_PER_THREAD
                        && symbols == CHARS_PER_THREAD);
        report("output is mixed, not grouped (" + switches + " type changes)", switches > 100);
        report("live counter labels show the final totals",
                letterLabel.getText().equals("Letters: " + CHARS_PER_THREAD)
                        && digitLabel.getText().equals("Digits: " + CHARS_PER_THREAD)
                        && symbolLabel.getText().equals("Symbols: " + CHARS_PER_THREAD));
        report("Start and Clear buttons are enabled again after the run",
                !startButton.isDisabled() && !clearButton.isDisabled());

        System.out.println("Result: " + testsPassed + " of " + testsRun + " tests passed.");
        System.out.println();

        statusLabel.setText("Run " + runNumber + " done. " + testsPassed + " of " + testsRun
                + " tests passed (see console for details).");
    }

    // ----- Small helpers used by the tests ------------------------------

    /** Returns the position of c in SYMBOLS, or -1 if it is not there. */
    private static int indexOfSymbol(char c) {
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (SYMBOLS[i] == c) {
                return i;
            }
        }
        return -1;
    }

    /** True only if every element of the array is true. */
    private static boolean allTrue(boolean[] values) {
        for (boolean v : values) {
            if (!v) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}