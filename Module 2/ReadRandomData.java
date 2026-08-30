import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * ReadRandomData.java
 *
 * Assignment:  Module Assignment - File I/O with Arrays
 * Author:      Zachary White
 * Date:        08/20/2026
 * Course:      CSD420
 *
 * Description:
 *   This program opens "Zwhitedatafile.dat" (created/appended to by
 *   WriteRandomData.java) and reads the data back out with a
 *   DataInputStream, displaying it to the console.
 *
 *   Because WriteRandomData.java appends a new "batch" of 5 ints
 *   followed by 5 doubles every time it is run, this reader loops,
 *   pulling one batch at a time out of the file, until it reaches
 *   the end of the file (EOFException), at which point it stops
 *   reading and reports how many batches (program runs) were found.
 */
public class ReadRandomData {

    private static final String FILE_NAME = "Zwhitedatafile.dat";
    private static final int ARRAY_SIZE = 5;

    public static void main(String[] args) {

        int batchNumber = 0;

        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             DataInputStream dis = new DataInputStream(fis)) {

            System.out.println("Reading data from " + FILE_NAME + "...\n");

            // Keep reading batches of (5 ints + 5 doubles) until the
            // end of the file is reached.
            while (true) {

                int[] intData = new int[ARRAY_SIZE];
                double[] doubleData = new double[ARRAY_SIZE];

                // readInt()/readDouble() throw EOFException when there
                // is no more data left to read, which is how we know
                // to stop the loop.
                for (int i = 0; i < ARRAY_SIZE; i++) {
                    intData[i] = dis.readInt();
                }

                for (int i = 0; i < ARRAY_SIZE; i++) {
                    doubleData[i] = dis.readDouble();
                }

                batchNumber++;
                System.out.println("----- Batch " + batchNumber + " -----");

                System.out.print("Integers: ");
                for (int value : intData) {
                    System.out.print(value + "  ");
                }

                System.out.print("\nDoubles:  ");
                for (double value : doubleData) {
                    System.out.print(value + "  ");
                }
                System.out.println("\n");
            }

        } catch (EOFException eof) {
            // Normal, expected way for this loop to end -- there is no
            // more data left in the file.
            if (batchNumber == 0) {
                System.out.println("The file exists but contains no complete data batches.");
            } else {
                System.out.println("End of file reached. Total batches read: " + batchNumber);
            }
        } catch (IOException e) {
            System.out.println("Could not find or read " + FILE_NAME
                    + ". Please run WriteRandomData first.");
            e.printStackTrace();
        }
    }
}
