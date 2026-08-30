import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * WriteRandomData.java
 *
 * Assignment:  Module Assignment - File I/O with Arrays
 * Author:      Zachary White
 * Date:        08/20/2026
 * Course:      CSD420
 *
 * Description:
 *   This program creates two arrays -- one holding five randomly
 *   generated integers and one holding five randomly generated
 *   double values.  Both arrays are written to a binary data file
 *   named "Zwhitedatafile.dat" using a DataOutputStream.
 *
 *   File handling requirements:
 *     - If the file Zwhitedatafile.dat does NOT already exist, it is
 *       created automatically the first time this program runs
 *       (FileOutputStream does this by default).
 *     - If the file DOES already exist, the new data is APPENDED to
 *       the end of the file rather than overwriting the previous
 *       contents.  This is accomplished by opening the
 *       FileOutputStream in "append" mode -- new
 *       FileOutputStream(fileName, true).
 *
 *   Every time this program is executed it writes one more "batch"
 *   of 5 ints followed by 5 doubles onto the end of the file, so the
 *   companion program (ReadRandomData.java) simply keeps reading
 *   int/double pairs until it reaches the end of the file.
 */
public class WriteRandomData {

    // Name of the data file (relative path, created in the current
    // working directory when the program is run).
    private static final String FILE_NAME = "Zwhitedatafile.dat";

    // How many integers / doubles are generated and stored per run.
    private static final int ARRAY_SIZE = 5;

    public static void main(String[] args) {

        // ----- Step 1: Create the arrays and fill them with random values -----
        int[] randomInts = new int[ARRAY_SIZE];
        double[] randomDoubles = new double[ARRAY_SIZE];

        Random generator = new Random();

        for (int i = 0; i < ARRAY_SIZE; i++) {
            // Random integers between 0 and 999
            randomInts[i] = generator.nextInt(1000);

            // Random doubles between 0.0 and 100.0, rounded to 2 decimals
            double value = generator.nextDouble() * 100.0;
            randomDoubles[i] = Math.round(value * 100.0) / 100.0;
        }

        // ----- Step 2: Write both arrays to the data file (append mode) -----
        // "true" as the second argument to FileOutputStream tells Java to
        // append to the file instead of overwriting it. If the file does
        // not exist yet, it will simply be created.
        try (FileOutputStream fos = new FileOutputStream(FILE_NAME, true);
             DataOutputStream dos = new DataOutputStream(fos)) {

            for (int i = 0; i < ARRAY_SIZE; i++) {
                dos.writeInt(randomInts[i]);
            }

            for (int i = 0; i < ARRAY_SIZE; i++) {
                dos.writeDouble(randomDoubles[i]);
            }

            System.out.println("Data successfully written/appended to " + FILE_NAME);

        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }

        // ----- Step 3: Display what was just generated/written, for verification -----
        System.out.println("\nInteger array written:");
        for (int value : randomInts) {
            System.out.print(value + "  ");
        }

        System.out.println("\n\nDouble array written:");
        for (double value : randomDoubles) {
            System.out.print(value + "  ");
        }
        System.out.println();
    }
}
