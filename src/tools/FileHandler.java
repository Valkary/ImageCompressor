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
    /**
     * This function verifies that a given path returns a valid BufferedImage with .bmp file format
     * @param console Console helper
     * @return Returns a verified BufferedImage
     */
    public static BufferedImage getBufferedImage(IOConsole console) {
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

    public static BufferedImage getBufferedImage(IOConsole console, String img_path) {
        boolean verified = false;
        BufferedImage image = null;

        do {
            if (false) {
                console.showInfo("==> Incorrect MIME type! The extension should be of type \".bmp\"");
                continue;
            } else if ((image = verifyPath(img_path)) == null) {
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
     * This function verifies that a given path returns a valid BufferedImage with an array of accepted mime types
     * @param console Console helper
     * @return Returns a verified BufferedImage
     */
    public static BufferedImage getBufferedImage(IOConsole console, String[] mime_types) {
        boolean verified = false;
        String path;
        BufferedImage image = null;

        do {
            path = console.getString("Relative path to file: ");

            if (!verifyMimeType(path, mime_types)) {
                console.showInfo("==> Incorrect MIME type! The extension should be an accepted mime type");
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
     * This function verifies the image dimensions are valid
     * @param img BufferedImage
     * @return boolean
     */
    public static boolean verifyDimensions(BufferedImage img) {
        if (img.getWidth() <= 0 || img.getHeight() <= 0) {
            return false;
        }

        return true;
    }

    public static BufferedImage verifyPath(String path) {
        try {
            File file = new File(path);
            return ImageIO.read(file);
        } catch (IOException err) {
            return null;
        }
    }

    public static boolean verifyMimeType(String path) {
        String[] split_path = path.split("\\.");
        return split_path[split_path.length - 1].equals("bmp");
    }

    public static boolean verifyMimeType(String path, String[] mime_types) {
        String[] split_path = path.split("\\.");
        List<String> list = Arrays.asList(mime_types);

        return list.contains(split_path[split_path.length - 1]);
    }

    public static void writeLineToFile(FileWriter writer, String line) throws IOException {
        try {
            writer.write(line + "\n");
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static void renderBufferedImage(String name, String format, BufferedImage image) throws IOException {
        File output = new File(name + "." + format);

        try {
            ImageIO.write(image, format, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
