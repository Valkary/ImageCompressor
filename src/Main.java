public class Main {
    public static void main(String[] args) {
        ImageCompressor.compressImage(4, "src/images/test1.bmp", "kchau");
        ImageCompressor.decompressImage("kchau.txt", "final");
    }
}
