import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * WordProcessorTest
 *
 * A small, self-contained test runner (no external test framework required)
 * that verifies WordProcessor behaves correctly:
 *  - duplicate words are removed
 *  - duplicate detection is case-insensitive
 *  - punctuation is stripped from words
 *  - ascending / descending ordering is correct
 *  - descending order is exactly the reverse of ascending order
 *  - an empty file produces no words
 *  - the actual collection_of_words.txt submitted with this program is
 *    processed correctly end-to-end
 *
 * Run with:  java WordProcessorTest
 */
public class WordProcessorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {
        testExtractUniqueWordsRemovesDuplicates();
        testExtractUniqueWordsIsCaseInsensitive();
        testExtractUniqueWordsStripsPunctuation();
        testExtractUniqueWordsKeepsApostrophes();
        testAscendingOrder();
        testDescendingOrder();
        testDescendingIsReverseOfAscending();
        testEmptyFileProducesNoWords();
        testActualCollectionFile();

        System.out.println();
        System.out.println("==============================");
        System.out.println("Tests passed: " + passed);
        System.out.println("Tests failed: " + failed);
        System.out.println("==============================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- test cases ----------

    private static void testExtractUniqueWordsRemovesDuplicates() throws IOException {
        Path file = writeTempFile("apple banana apple cherry banana apple");
        Set<String> result = WordProcessor.extractUniqueWords(file);
        check("extractUniqueWords removes duplicates",
                result.size() == 3 && result.containsAll(Set.of("apple", "banana", "cherry")),
                "expected {apple, banana, cherry}, got: " + result);
    }

    private static void testExtractUniqueWordsIsCaseInsensitive() throws IOException {
        Path file = writeTempFile("Apple apple APPLE ApPlE");
        Set<String> result = WordProcessor.extractUniqueWords(file);
        check("extractUniqueWords is case-insensitive",
                result.size() == 1 && result.contains("apple"),
                "expected single word 'apple', got: " + result);
    }

    private static void testExtractUniqueWordsStripsPunctuation() throws IOException {
        Path file = writeTempFile("dog, cat. fish! dog; cat: fish?");
        Set<String> result = WordProcessor.extractUniqueWords(file);
        check("extractUniqueWords strips punctuation",
                result.equals(Set.of("dog", "cat", "fish")),
                "expected {dog, cat, fish}, got: " + result);
    }

    private static void testExtractUniqueWordsKeepsApostrophes() throws IOException {
        Path file = writeTempFile("the dog's bone, the cat's toy");
        Set<String> result = WordProcessor.extractUniqueWords(file);
        check("extractUniqueWords keeps apostrophes intact",
                result.contains("dog's") && result.contains("cat's"),
                "expected \"dog's\" and \"cat's\" to be preserved, got: " + result);
    }

    private static void testAscendingOrder() {
        Set<String> words = new TreeSet<>(Set.of("banana", "apple", "cherry"));
        List<String> asc = WordProcessor.ascending(words);
        check("ascending() sorts words A-Z",
                asc.equals(List.of("apple", "banana", "cherry")),
                "got: " + asc);
    }

    private static void testDescendingOrder() {
        Set<String> words = new TreeSet<>(Set.of("banana", "apple", "cherry"));
        List<String> desc = WordProcessor.descending(words);
        check("descending() sorts words Z-A",
                desc.equals(List.of("cherry", "banana", "apple")),
                "got: " + desc);
    }

    private static void testDescendingIsReverseOfAscending() {
        Set<String> words = new TreeSet<>(Set.of("zebra", "apple", "mango", "kiwi"));
        List<String> asc = WordProcessor.ascending(words);
        List<String> desc = WordProcessor.descending(words);

        List<String> reversedAsc = new ArrayList<>(asc);
        Collections.reverse(reversedAsc);

        check("descending() is exactly the reverse of ascending()",
                desc.equals(reversedAsc),
                "asc: " + asc + ", desc: " + desc);
    }

    private static void testEmptyFileProducesNoWords() throws IOException {
        Path file = writeTempFile("");
        Set<String> result = WordProcessor.extractUniqueWords(file);
        check("empty file produces no words",
                result.isEmpty(),
                "expected empty set, got: " + result);
    }

    private static void testActualCollectionFile() throws IOException {
        Path file = Path.of("collection_of_words.txt");
        if (!Files.exists(file)) {
            System.out.println("[SKIP] actual collection_of_words.txt not found in working directory");
            return;
        }

        Set<String> result = WordProcessor.extractUniqueWords(file);
        check("actual file produces at least one unique word",
                !result.isEmpty(),
                "expected a non-empty set of words");

        List<String> asc = WordProcessor.ascending(result);
        check("actual file: ascending list is sorted correctly",
                isSorted(asc, true),
                "list not sorted ascending: " + asc);

        List<String> desc = WordProcessor.descending(result);
        check("actual file: descending list is sorted correctly",
                isSorted(desc, false),
                "list not sorted descending: " + desc);

        check("actual file: no duplicate words in result",
                new HashSet<>(asc).size() == asc.size(),
                "duplicates found in: " + asc);

        check("actual file: ascending and descending contain the same words",
                new HashSet<>(asc).equals(new HashSet<>(desc)),
                "asc and desc contain different word sets");
    }

    // ---------- helpers ----------

    private static boolean isSorted(List<String> list, boolean ascending) {
        for (int i = 0; i < list.size() - 1; i++) {
            int cmp = list.get(i).compareTo(list.get(i + 1));
            if (ascending && cmp > 0) return false;
            if (!ascending && cmp < 0) return false;
        }
        return true;
    }

    private static Path writeTempFile(String content) throws IOException {
        Path temp = Files.createTempFile("wordtest", ".txt");
        Files.writeString(temp, content);
        temp.toFile().deleteOnExit();
        return temp;
    }

    private static void check(String testName, boolean condition, String failureDetail) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + testName);
        } else {
            failed++;
            System.out.println("[FAIL] " + testName + " -> " + failureDetail);
        }
    }
}
