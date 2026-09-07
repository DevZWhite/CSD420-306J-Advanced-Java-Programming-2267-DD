import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * WordProcessor
 *
 * Reads words from a text file (referenced internally, not via command-line
 * arguments), removes duplicates, and displays the resulting unique words
 * first in ascending alphabetical order and then in descending alphabetical
 * order.
 *
 * Notes on word extraction rules:
 *  - Words are split on any run of characters that are not letters or an
 *    apostrophe (so punctuation such as commas, periods, and colons are
 *    stripped, but contractions like "dog's" are kept intact).
 *  - Duplicate detection is case-insensitive: "Dog", "dog", and "DOG" are
 *    all treated as the same word and only stored once (lower-cased).
 */
public class WordProcessor {

    /** Name of the file that contains the source words. Referenced internally. */
    private static final String WORD_FILE_NAME = "collection_of_words.txt";

    /** Splits on anything that is not a letter or apostrophe. */
    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("[^A-Za-z']+");

    /**
     * Reads the given file and returns the set of unique, lower-cased words
     * it contains, with punctuation stripped and blank tokens discarded.
     */
    public static Set<String> extractUniqueWords(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);

        return lines.stream()
                .flatMap(line -> WORD_SPLIT_PATTERN.splitAsStream(line))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /** Returns the given words as a new list sorted in ascending order. */
    public static List<String> ascending(Set<String> words) {
        List<String> result = new ArrayList<>(words);
        Collections.sort(result);
        return result;
    }

    /** Returns the given words as a new list sorted in descending order. */
    public static List<String> descending(Set<String> words) {
        List<String> result = new ArrayList<>(words);
        result.sort(Collections.reverseOrder());
        return result;
    }

    public static void main(String[] args) {
        // The file is referenced directly here; no command-line argument is needed.
        Path filePath = Path.of(WORD_FILE_NAME);

        try {
            Set<String> uniqueWords = extractUniqueWords(filePath);

            System.out.println("Read \"" + WORD_FILE_NAME + "\" successfully.");
            System.out.println("Total unique words found: " + uniqueWords.size());

            System.out.println();
            System.out.println("--- Unique words in ASCENDING order ---");
            ascending(uniqueWords).forEach(System.out::println);

            System.out.println();
            System.out.println("--- Unique words in DESCENDING order ---");
            descending(uniqueWords).forEach(System.out::println);

        } catch (IOException e) {
            System.err.println("Could not read file \"" + WORD_FILE_NAME + "\": " + e.getMessage());
        }
    }
}
