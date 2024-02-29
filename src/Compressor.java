import tools.FileHandler;
import tools.IOConsole;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The `Compressor` class handles image compression.
 * It utilizes instances of `IOConsole` and `FileHandler` for these operations.
 *
 * @author Pepe Salcedo
 */
public class Compressor {
    private static Compressor instance = null;
    private final FileHandler fileHandler;
    private final IOConsole console;

    /**
     * Private constructor to initialize a `Compressor` instance with the specified console and file handler.
     *
     * @param console      The input/output console used for displaying messages.
     * @param fileHandler  The file handler for managing file-related operations.
     */
    private Compressor(IOConsole console, FileHandler fileHandler) {
        this.console = console;
        this.fileHandler = fileHandler;
    }

    /**
     * Gets a unique instance of the `Compressor` class.
     *
     * @return The unique instance of `Compressor`.
     * @throws Exception If no console has been assigned.
     */
    public static Compressor getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("No console assigned");
        }
        return instance;
    }

    /**
     * Gets a unique instance of the `Compressor` class with the specified console and file handler.
     *
     * @param console      The input/output console used for displaying messages.
     * @param fileHandler  The file handler for managing file-related operations.
     * @return The unique instance of `Compressor`.
     */
    public static Compressor getInstance(IOConsole console, FileHandler fileHandler) {
        if (instance == null) {
            instance = new Compressor(console, fileHandler);
        }
        return instance;
    }

    /**
     * This function verifies that the provided image is valid, reads the pixel data by regions and averages them
     * and writes a color dictionary and to a compressed file of specified name.
     *
     * @param compressionFactor the value by which the image will be divided by (must be larger than 1)
     * @param file_path         the path to the file you want to compress
     * @param outputFileName    the name of the output file
     */
    public boolean compressImage(int compressionFactor, String file_path, String outputFileName) {
        console.showInfo("==> Starting compression...");
        try {
            console.showInfo("==> Verifying image data...");
            verifyCompressionFactor(compressionFactor);
            BufferedImage image = fileHandler.getBufferedImage(file_path);

            int rows = image.getHeight();
            int cols = image.getWidth();

            console.showInfo("==> Generating pixel data...");
            Color[][] pixelBuffer = generatePixelData(image);

            console.showInfo("==> Writing image to compressed file...");
            writeToBinaryFile(pixelBuffer, compressionFactor, rows, cols, outputFileName);

            return true;
        } catch (Exception e) {
            console.showInfo(String.valueOf(e));
            return false;
        }
    }

    /**
     * This function verifies that the compression factor is larger than 1
     * @param compressionFactor the integer to check
     * @throws Exception
     */
    private void verifyCompressionFactor(int compressionFactor) throws Exception {
            if (compressionFactor <= 1) {
                throw new Exception("Compression factor must be an integer larger than 1");
            }
    }

    /**
     * Writes the pixel buffer to a binary file with the specified compression factor, rows, columns, and output file name.
     *
     * @param pixelBuffer      The pixel buffer containing color information.
     * @param compressionFactor The compression factor (e.g., quality level).
     * @param rows             The number of rows in the image.
     * @param cols             The number of columns in the image.
     * @param outputFileName   The name of the output binary file.
     * @throws IOException If an I/O error occurs during writing.
     */
    private void writeToBinaryFile(Color[][] pixelBuffer, int compressionFactor, int rows, int cols, String outputFileName) throws IOException {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(outputFileName));

            dataOutputStream.write(compressionFactor);
            dataOutputStream.writeInt(cols / compressionFactor);

            for (int row = 0; row < rows; row += compressionFactor) {
                for (int col = 0; col < cols; col += compressionFactor) {
                    Color color = calculateRegionColorAverage(col, row, rows, cols, pixelBuffer, compressionFactor);

                    dataOutputStream.write(new byte[]{
                            (byte) color.getRed(),
                            (byte) color.getGreen(),
                            (byte) color.getBlue()
                    });
                }
            }

            dataOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the average color for a region specified by the given coordinates.
     *
     * @param x                The starting x-coordinate of the region.
     * @param y                The starting y-coordinate of the region.
     * @param rows             The total number of rows in the image.
     * @param cols             The total number of columns in the image.
     * @param pixelBuffer      The pixel buffer containing color information.
     * @param compressionFactor The compression factor (e.g., quality level).
     * @return The average color for the specified region.
     */
    private Color calculateRegionColorAverage(int x, int y, int rows, int cols, Color[][] pixelBuffer, int compressionFactor) {
        long totalRed = 0;
        long totalGreen = 0;
        long totalBlue = 0;
        int iters = 0;

        for (int row = y; row < y + compressionFactor; row++) {
            for (int col = x; col < x + compressionFactor; col++) {
                if (row < rows && col < cols) {
                    Color color = pixelBuffer[row][col];

                    totalRed += color.getRed();
                    totalGreen += color.getGreen();
                    totalBlue += color.getBlue();

                    iters++;
                }
            }
        }

        if (iters == 0) {
            return Color.RED;
        } else {
            int avgRed = (int) (totalRed / iters);
            int avgGreen = (int) (totalGreen / iters);
            int avgBlue = (int) (totalBlue / iters);

            return new Color(avgRed, avgGreen, avgBlue);
        }
    }

    /**
     * Generates a pixel buffer from the given `BufferedImage`.
     * Each pixel in the buffer corresponds to a color in the image.
     *
     * @param image The input image from which to generate the pixel data.
     * @return A 2D array of `Color` objects representing the pixel data.
     */
    private Color[][] generatePixelData(BufferedImage image) {
        int rows = image.getHeight();
        int cols = image.getWidth();
        Color[][] pixelBuffer = new Color[rows][cols];

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                pixelBuffer[y][x] = new Color(image.getRGB(x, y), false);
            }
        }

        return pixelBuffer;
    }
}
