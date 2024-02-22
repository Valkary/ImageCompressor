public class Main {
    public static void main(String[] args) {
        ImageCompressor.compressImage(4, "src/images/iu.png", "kchau");
        ImageCompressor.decompressImage("kchau.txt", "final");
    }
}
