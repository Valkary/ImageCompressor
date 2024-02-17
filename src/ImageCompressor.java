import tools.FileHandler;
import tools.IOConsole;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageCompressor {
    BufferedImage image;
    IOConsole console;
    Color[][] pixels;
    int rows,cols, compression_value = 4;

    ImageCompressor() {
        console = new IOConsole();
        image = FileHandler.getBufferedImage(console);
        generatePixelData();
    }

    ImageCompressor(String img_path) {
        console = new IOConsole();
        image = FileHandler.getBufferedImage(console, img_path);
        generatePixelData();
    }

    public void compressImage() {
        List<String> colors = new ArrayList<>();
        int compressed_rows = (int)Math.ceil(((float)rows/compression_value));
        int compressed_cols = (int)Math.ceil(((float)cols/compression_value));
        int[][] compressed_pixels = new int[compressed_rows][compressed_cols];

        for (int row = 0; row < rows; row += compression_value) {
            int compressed_row = (int)Math.floor((double) row / compression_value);

            for (int col = 0; col < cols; col += compression_value) {
                int compressed_col = (int)Math.floor((double) col / compression_value);

                Color color = calculateRegionColorAverage(col, row);
                String hex_color = convertColorToHexString(color);

                if (!colors.contains(hex_color)) {
                    colors.add(hex_color);
                }

                int id = colors.indexOf(hex_color);
                compressed_pixels[compressed_row][compressed_col] = id;
            }
        }

        try {
            File myObj = new File("filename.txt");
            myObj.createNewFile();
            FileWriter myWriter = new FileWriter("filename.txt");
            myWriter.write(compressed_rows + " " + compressed_cols + "\n");
            myWriter.write("COLORS\n");
            String dictionary = colors.stream().map(Object::toString).collect(Collectors.joining(",")) + "\n";
            myWriter.write(dictionary);
            myWriter.write("PIXELS\n");
            StringBuilder pixels = new StringBuilder();

            for (int i = 0; i < compressed_pixels.length; i++) {
                for (int j = 0; j < compressed_pixels[i].length; j++) {
                    pixels.append(compressed_pixels[i][j]);
                    if (j < compressed_pixels[i].length - 1) {
                        pixels.append(",");
                    }
                }
                if (i < compressed_pixels.length - 1) {
                    pixels.append("\n");
                }
            }

            String pixelsStr = pixels.toString();

            myWriter.write(pixelsStr);
            myWriter.write("\nEOF\n");
            myWriter.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            System.out.println(e.getStackTrace());
        }
    }

    public String convertColorToHexString(Color color) {
      String hex_color = "";
      String red_hue = Integer.toHexString(color.getRed());
      String green_hue = Integer.toHexString(color.getGreen());
      String blue_hue = Integer.toHexString(color.getBlue());

      hex_color += red_hue.length() == 1 ? "0" + red_hue : red_hue;
      hex_color += green_hue.length() == 1 ? "0" + green_hue : green_hue;
      hex_color += blue_hue.length() == 1 ? "0" + blue_hue : blue_hue;

      return hex_color;
    };

    public void decompressImage() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("filename.txt"));

            enum STAGES {
                DIMENSIONS,
                COLORS,
                PIXELS,
                EOF
            };

            STAGES stage = STAGES.DIMENSIONS;
            int rows, cols = 0, curr_row = 0;
            List<Long> colors = new ArrayList<>();
            BufferedImage output = null;

            do {
                String line = reader.readLine();

                switch (line) {
                    case "EOF" -> stage = STAGES.EOF;
                    case "COLORS" -> stage = STAGES.COLORS;
                    case "PIXELS" -> stage = STAGES.PIXELS;
                    default -> {
                        switch (stage) {
                            case DIMENSIONS -> {
                                String[] dimensions = line.replace("\n", "").split(" ");
                                rows = Integer.parseInt(dimensions[0]);
                                cols = Integer.parseInt(dimensions[1]);
                                System.out.println(rows + "x" + cols);
                                output = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
                            }
                            case COLORS -> {
                                String[] color_strings = line.replace("\n", "").split(",");

                                for (String colorString : color_strings) {
                                    colors.add(Long.parseLong(colorString, 16));
                                }
                            }
                            case PIXELS -> {
                                String[] pixel_strings = line.replace("\n", "").split(",");

                                if (output == null) {
                                    throw new RuntimeException();
                                }

                                for (int col = 0; col < cols; col++) {
                                    output.setRGB(col, curr_row, Integer.parseInt(pixel_strings[col]));
                                }

                                curr_row++;
                            }
                        }
                    }
                }
            } while (!stage.equals(STAGES.EOF));

            renderImage("result", "bmp", output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void renderImage(String name, String format, BufferedImage image) {
        File output = new File(name + "." + format);

        try {
            ImageIO.write(image, format, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Color calculateRegionColorAverage(int x, int y) {
        long total_red = 0;
        long total_green = 0;
        long total_blue = 0;
        int iters = 0;

        for (int row = y; row < y + compression_value; row++) {
            for (int col = x; col < x + compression_value; col++) {
                if (row < rows && col < cols) {
                    Color color = pixels[row][col];

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
            int avgRed = (int)(total_red / iters);
            int avgGreen = (int)(total_green / iters);
            int avgBlue = (int)(total_blue / iters);

            System.out.println("RGB = (" + avgRed + ", " + avgGreen + ", " + avgBlue + ")");

            return new Color(avgRed, avgGreen, avgBlue);
        }
    }

    private void generatePixelData() {
        rows = image.getHeight();
        cols = image.getWidth();
        pixels = new Color[rows][cols];

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                pixels[y][x] = new Color(image.getRGB(x,y), false);
            }
        }
    }
}
