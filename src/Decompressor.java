import tools.FileHandler;
import tools.IOConsole;
import tools.Tuple;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Decompressor {
    private static Decompressor instance = null;
    private static IOConsole console;

    private Decompressor(IOConsole console) {
        this.console = console;
    }

    public static Decompressor getInstance(IOConsole console) {
        if (instance == null) {
            instance = new Decompressor(console);
        }

        return instance;
    }

    public static Decompressor getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("No console assigned");
        }

        return instance;
    }

    public boolean decompressImage(String compressedFilePath, String outputName) {
        console.showInfo("==> Starting file decompression...");
        try {
            console.showInfo("==> Calculating image data...");
            DataInputStream inputStream = new DataInputStream(new FileInputStream(compressedFilePath));
            int compressionFactor = inputStream.read();
            int numPixels = inputStream.available() / 3;
            int numCols = inputStream.readInt();
            int numRows = numPixels / numCols;

            console.showInfo("==> Building pixel buffer...");
            Color[][] pixelBuffer = generatePixelBuffer(inputStream, numRows, numCols, numPixels);

            console.showInfo("==> Building image by using bilinear interpolations...");
            BufferedImage outputImg = buildDecompressedImage(pixelBuffer, numCols, numRows, compressionFactor);

            console.showInfo("==> Rendering image to specified file...");
            FileHandler.renderBufferedImage(outputName, "bmp", outputImg);

            console.showInfo("==> Image correctly decompressed!");
            return true;
        } catch (FileNotFoundException e) {
            console.showInfo("==> The provided file path was not found!");
            console.showInfo(String.valueOf(e));
            return false;
        } catch (IOException e) {
            console.showInfo(String.valueOf(e));
            return false;
        }
    }

    private Color[][] generatePixelBuffer(DataInputStream inputStream, int numRows, int numCols, int numPixels) throws IOException {
        Color[][] pixelBuffer = new Color[numRows][numCols];

        try {
            for (int i = 0; i < numPixels - 1; i++) {
                int red = Math.max(inputStream.read(), 0);
                int green = Math.max(inputStream.read(), 0);
                int blue = Math.max(inputStream.read(), 0);

                int calculatedCol = i % numCols;
                int calculatedRow = i / numCols;

                pixelBuffer[calculatedRow][calculatedCol] = new Color(red, green, blue);
            }

            return pixelBuffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage buildDecompressedImage(Color[][] pixelBuffer, int cols, int rows, int compressionFactor) {
        BufferedImage image = new BufferedImage(cols * compressionFactor - compressionFactor, rows * compressionFactor - compressionFactor, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < rows * compressionFactor - compressionFactor; y++) {
            for (int x = 0; x < cols * compressionFactor - compressionFactor; x++) {
                Tuple<Integer, Integer> p = new Tuple<>(x, y);
                Color intermediateColor = bilinearInterpolation(p, compressionFactor, pixelBuffer);
                image.setRGB(x, y, intermediateColor.getRGB());
            }
        }

        return image;
    }

    private Color bilinearInterpolation(Tuple<Integer, Integer> p, int compressionFactor, Color[][] colors) {
        int x = p.x / compressionFactor;
        int y = p.y / compressionFactor;

        Color q11 = colors[y][x]; // bottom left
        Color q12 = colors[y + 1][x]; // top left
        Color q21 = colors[y][x + 1]; // bottom right
        Color q22 = colors[y + 1][x + 1]; // top right

        float x1 = x * compressionFactor;
        float x2 = x1 + compressionFactor;
        float y1 = y * compressionFactor;
        float y2 = y1 + compressionFactor;

        float x_diff = (p.x - x1) / (x2 - x1);
        float y_diff = (p.y - y1) / (y2 - y1);

        return new Color(
                interpolate(q11.getRed(), q12.getRed(), q21.getRed(), q22.getRed(), x_diff, y_diff),
                interpolate(q11.getGreen(), q12.getGreen(), q21.getGreen(), q22.getGreen(), x_diff, y_diff),
                interpolate(q11.getBlue(), q12.getBlue(), q21.getBlue(), q22.getBlue(), x_diff, y_diff)
        );
    }

    private int interpolate(int q11, int q12, int q21, int q22, float xDiff, float yDiff) {
        // Bilinear interpolation formula
        int b1 = (int) (q11 * (1 - xDiff) * (1 - yDiff));
        int b2 = (int) (q21 * xDiff * (1 - yDiff));
        int b3 = (int) (q12 * (1 - xDiff) * yDiff);
        int b4 = (int) (q22 * xDiff * yDiff);

        return b1 + b2 + b3 + b4;
    }
}
