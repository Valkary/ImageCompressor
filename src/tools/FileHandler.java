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
    public static boolean verifyDimensions(BufferedImage img) {
        return img.getWidth() > 0 && img.getHeight() > 0;
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

    public static void renderBufferedImage(String name, String format, BufferedImage image) throws IOException {
        File output = new File(name + "." + format);

        try {
            ImageIO.write(image, format, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
