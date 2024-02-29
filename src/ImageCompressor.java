import tools.FileHandler;
import tools.IOConsole;

/**
 * @author Pepe Salcedo
 * The `ImageCompressor` class handles image compression and decompression.
 * It uses the `Decompressor` and `Compressor` instances for these operations.
 */
public class ImageCompressor {
    private static final IOConsole console = new IOConsole();
    private static final FileHandler fileHandler = FileHandler.getInstance(console);
    private static final Decompressor decompressor = Decompressor.getInstance(console, fileHandler);
    private static final Compressor compressor = Compressor.getInstance(console, fileHandler);

    /**
     * Shows a menu on the screen with compression and decompression options
     */
    public static void menu() {
        int opt;

        do {
            console.showInfo("Welcome to your trusty 'Pepressed' image compression program!");
            console.showInfo("Please select an option: ");
            console.showInfo("[1] Compress an image to a file");
            console.showInfo("[2] Decompress a file into an image");
            console.showInfo("[3] Exit program");
            opt = console.getInt("Enter your selection: ", "Enter a valid integer");

            switch (opt) {
                case 1 -> {
                    int compressionFactor = console.getInt("Enter a compression factor: ", "Enter a valid integer");
                    String filePath = console.getString("Enter the path to the file you want to compress: ");
                    String outputFileName = console.getString("Enter the name you want on the output file: ");

                    compressImage(compressionFactor, filePath, outputFileName);
                }
                case 2 -> {
                    String filePath = console.getString("Enter the path to the file you want to decompress: ");
                    String outputFileName = console.getString("Enter the name you want on the output image: ");

                    decompressImage(filePath, outputFileName);
                }
                case 3 -> {
                    console.showInfo("Thank your for using the program!");
                }
                default -> console.showInfo("Select a valid option!");
            }
        } while (opt != 3);
    }

    /**
     * Decompresses an image from the specified compressed file path.
     *
     * @param compressedFilePath The path to the compressed image file.
     * @param outputName         The name of the decompressed output file.
     */
    public static void decompressImage(String compressedFilePath, String outputName) {
        if (decompressor.decompressImage(compressedFilePath, outputName)) {
            console.showInfo("==> Image correctly decompressed at the given path!");
        } else {
            console.showInfo("==> Error decompressing image. Try again later.");
        }
    }

    /**
     * Compresses an image with the given compression factor.
     *
     * @param compressionFactor The compression factor (e.g., quality level).
     * @param filePath          The path to the original image file.
     * @param outputFileName    The name of the compressed output file.
     */
    public static void compressImage(int compressionFactor, String filePath, String outputFileName) {
        if (compressor.compressImage(compressionFactor, filePath, outputFileName)) {
            console.showInfo("==> Image correctly compressed at the given path!");
        } else {
            console.showInfo("==> Error compressing image. Try again later.");
        }
    }
}
