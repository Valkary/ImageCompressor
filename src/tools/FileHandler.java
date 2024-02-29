package tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Pepe Salcedo
 * This class verifies an image validity from the file path and returns a BufferedImage
 */
public class FileHandler {
    private static IOConsole console = null;
    private static FileHandler instance = null;

    /**
     * Private constructor to initialize a `FileHandler` instance with the specified console.
     *
     * @param console The input/output console used for displaying messages.
     */
    private FileHandler(IOConsole console) {
        this.console = console;
    }

    /**
     * Gets a unique instance of the `FileHandler` class.
     *
     * @return The unique instance of `FileHandler`.
     * @throws Exception If the instance is not found.
     */
    public static FileHandler getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Instance not found");
        }
        return instance;
    }

    /**
     * Gets a unique instance of the `FileHandler` class with the specified console.
     *
     * @param console The input/output console used for displaying messages.
     * @return The unique instance of `FileHandler`.
     */
    public static FileHandler getInstance(IOConsole console) {
        if (instance == null) {
            instance = new FileHandler(console);
        }
        return instance;
    }

    /**
     * This function verifies that a given path returns a valid BufferedImage with .bmp file format
     * @return Returns a verified BufferedImage
     */
    public BufferedImage getBufferedImage() {
        boolean verified = false;
        String path;
        BufferedImage image = null;

        do {
            path = console.getString("Relative path to file: ");

            if (!verifyMimeType(path)) {
                console.showInfo("==> Incorrect MIME type! The extension should be of type \".bmp\"");
                continue;
            } else if ((image = verifyPath(path)) == null) {
                console.showInfo("==> File path is incorrect!");
                continue;
            } else if (!verifyDimensions(image)) {
                console.showInfo("==> Dimensions cannot be less than 1x1 pixels");
                continue;
            }

            verified = true;
        } while (!verified);

        return image;
    }

    /**
     * This function verifies that a given path returns a valid BufferedImage with .bmp file format
     * @return Returns a verified BufferedImage
     */
    public BufferedImage getBufferedImage(String img_path) {
        boolean verified = false;
        BufferedImage image = null;

        do {
            if (!verifyMimeType(img_path)) {
                console.showInfo("==> Incorrect MIME type! The extension should be of type \".bmp\"");
                img_path = console.getString("Relative path to file: ");
                continue;
            } else if ((image = verifyPath(img_path)) == null) {
                console.showInfo("==> File path is incorrect!");
                img_path = console.getString("Relative path to file: ");
                continue;
            } else if (!verifyDimensions(image)) {
                console.showInfo("==> Dimensions cannot be less than 1x1 pixels");
                img_path = console.getString("Relative path to file: ");
                continue;
            }

            verified = true;
        } while (!verified);

        return image;
    }

    /**
     * This function verifies the image dimensions are valid
     * @param img BufferedImage
     * @return boolean
     */
    private boolean verifyDimensions(BufferedImage img) {
        return img.getWidth() > 0 && img.getHeight() > 0;
    }

    /**
     * Verifies the existence of an image file at the specified path and reads it into a `BufferedImage`.
     *
     * @param path The path to the image file.
     * @return The loaded `BufferedImage`, or `null` if an error occurs.
     */
    private BufferedImage verifyPath(String path) {
        try {
            File file = new File(path);
            return ImageIO.read(file);
        } catch (IOException err) {
            return null;
        }
    }

    /**
     * Verifies that the file extension corresponds to the BMP image format.
     *
     * @param path The path to the image file.
     * @return `true` if the file extension is BMP, `false` otherwise.
     */
    private boolean verifyMimeType(String path) {
        String[] splitPath = path.split("\\.");
        return splitPath[splitPath.length - 1].equalsIgnoreCase("bmp");
    }

    /**
     * Renders the provided `BufferedImage` to a file with the specified name and format.
     *
     * @param name   The base name of the output file.
     * @param format The desired image format (e.g., "bmp").
     * @param image  The `BufferedImage` to be saved.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void renderBufferedImage(String name, String format, BufferedImage image) throws IOException {
        File output = new File(name + "." + format);

        try {
            ImageIO.write(image, format, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
