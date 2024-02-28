import tools.FileHandler;
import tools.IOConsole;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Compressor {
    private static Compressor instance = null;
    private final IOConsole console;
    private int MAX_WIDTH = Integer.MAX_VALUE;
    private int MAX_HEIGHT = Integer.MAX_VALUE;

    public static Compressor getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("No console assigned");
        }
        return instance;
    }

    public static Compressor getInstance(IOConsole console) {
        if (instance == null) {
            instance = new Compressor(console);
        }
        return instance;
    }

    private Compressor(IOConsole console) {
        this.console = console;
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
        try {
            verifyCompressionFactor(compressionFactor);
            BufferedImage image = FileHandler.getBufferedImage(console, file_path);

            int rows = image.getHeight();
            int cols = image.getWidth();

            Color[][] pixelBuffer = generatePixelData(image);
            writeToBinaryFile(pixelBuffer, compressionFactor, rows, cols, outputFileName);

            return true;
        } catch (Exception e) {
            console.showInfo(String.valueOf(e));
            return false;
        }
    }

    private void verifyCompressionFactor(int compressionFactor) throws Exception {
            if (compressionFactor <= 1) {
                throw new Exception("Compression factor must be an integer larger than 1");
            }
    }

    private void writeToBinaryFile(Color[][] pixelBuffer, int compressionFactor, int rows, int cols, String outputFileName) {
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
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Color calculateRegionColorAverage(int x, int y, int rows, int cols, Color[][] pixelBuffer, int compressionFactor) {
        long total_red = 0;
        long total_green = 0;
        long total_blue = 0;
        int iters = 0;

        for (int row = y; row < y + compressionFactor; row++) {
            for (int col = x; col < x + compressionFactor; col++) {
                if (row < rows && col < cols) {
                    Color color = pixelBuffer[row][col];

                    total_red += color.getRed();
                    total_green += color.getGreen();
                    total_blue += color.getBlue();

                    iters++;
                }
            }
        }

        if (iters == 0) {
            return Color.RED;
        } else {
            int avgRed = (int) (total_red / iters);
            int avgGreen = (int) (total_green / iters);
            int avgBlue = (int) (total_blue / iters);

            return new Color(avgRed, avgGreen, avgBlue);
        }
    }

    private Color[][] generatePixelData(BufferedImage image) {
        int rows = image.getHeight();
        int cols = image.getWidth();
        Color[][] pixel_buffer = new Color[rows][cols];

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                pixel_buffer[y][x] = new Color(image.getRGB(x, y), false);
            }
        }

        return pixel_buffer;
    }
}
