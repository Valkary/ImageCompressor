import tools.IOConsole;

public class ImageCompressor {
    private static final IOConsole console = new IOConsole();
    private static final Decompressor decompressor = Decompressor.getInstance(console);
    private static final Compressor compressor = Compressor.getInstance(console);

    public static void decompressImage(String compressedFilePath, String outputName) {
        if (decompressor.decompressImage(compressedFilePath, outputName)) {
            console.showInfo("==> Image correctly decompressed at the given path!");
        } else {
            console.showInfo("==> Error decompressing image. Try again later.");
        }
    }

    public static void compressImage(int compressionFactor, String filePath, String outputFileName) {
        if (compressor.compressImage(compressionFactor, filePath, outputFileName)) {
            console.showInfo("==> Image correctly compressed at the given path!");
        } else {
            console.showInfo("==> Error compressing image. Try again later.");
        }
    }
}
