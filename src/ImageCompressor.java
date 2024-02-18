import tools.FileHandler;
import tools.IOConsole;
import tools.Tuple;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ImageCompressor {
    private static final IOConsole console = new IOConsole();

    public static void compressImage(int compression_factor, String file_path, String output_file_name) {
        BufferedImage image = FileHandler.getBufferedImage(console, file_path);
        int rows = image.getHeight();
        int cols = image.getWidth();
        Color[][] pixel_buffer = generatePixelData(image);
        List<String> colors = new ArrayList<>();
        int compressed_rows = (int)Math.ceil(((float)rows/compression_factor));
        int compressed_cols = (int)Math.ceil(((float)cols/compression_factor));
        int[][] compressed_pixels = new int[compressed_rows][compressed_cols];

        for (int row = 0; row < rows; row += compression_factor) {
            int compressed_row = (int)Math.floor((double) row / compression_factor);

            for (int col = 0; col < cols; col += compression_factor) {
                int compressed_col = (int)Math.floor((double) col / compression_factor);

                Color color = calculateRegionColorAverage(col, row, rows, cols, pixel_buffer, compression_factor);
                String hex_color = convertColorToHexString(color);

                if (!colors.contains(hex_color)) {
                    colors.add(hex_color);
                }

                int id = colors.indexOf(hex_color);
                compressed_pixels[compressed_row][compressed_col] = id;
            }
        }

        try {
            File myObj = new File(output_file_name + ".txt");
            myObj.createNewFile();
            FileWriter myWriter = new FileWriter(output_file_name + ".txt");

            FileHandler.writeLineToFile(myWriter, "COMPRESSION");
            FileHandler.writeLineToFile(myWriter, "" + compression_factor);
            FileHandler.writeLineToFile(myWriter, "DIMENSIONS");
            FileHandler.writeLineToFile(myWriter, compressed_rows + " " + compressed_cols);
            FileHandler.writeLineToFile(myWriter, "COLORS");
            FileHandler.writeLineToFile(myWriter, colors.stream().map(Object::toString).collect(Collectors.joining(",")));
            FileHandler.writeLineToFile(myWriter, "PIXELS");
            FileHandler.writeLineToFile(myWriter, buildPixelMatrixString(compressed_pixels));
            FileHandler.writeLineToFile(myWriter, "EOF");

            myWriter.close();
        } catch (Exception e) {
            console.showInfo("==> Error writing to compressed file");
            console.showInfo(Arrays.toString(e.getStackTrace()));
        }
    }

    private static String buildPixelMatrixString(int[][] compressed_pixels) {
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

        return pixels.toString();
    }

    public static String convertColorToHexString(Color color) {
      String hex_color = "";
      String red_hue = Integer.toHexString(color.getRed());
      String green_hue = Integer.toHexString(color.getGreen());
      String blue_hue = Integer.toHexString(color.getBlue());

      hex_color += red_hue.length() == 1 ? "0" + red_hue : red_hue;
      hex_color += green_hue.length() == 1 ? "0" + green_hue : green_hue;
      hex_color += blue_hue.length() == 1 ? "0" + blue_hue : blue_hue;

      return hex_color;
    };

    public static void decompressImage(String file_path, String img_name) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file_path));

            enum STAGES {
                COMPRESSION_FACTOR,
                DIMENSIONS,
                COLORS,
                PIXELS,
                EOF
            };

            STAGES stage = STAGES.COMPRESSION_FACTOR;
            int rows = 0, cols = 0, curr_row = 0, compression_factor = 1;

            List<Color> colors_buffer = new ArrayList<>();
            Color[][] compressed_pixels_buffer = null;

            do {
                String line = reader.readLine();

                switch (line) {
                    case "EOF" -> stage = STAGES.EOF;
                    case "COLORS" -> stage = STAGES.COLORS;
                    case "PIXELS" -> stage = STAGES.PIXELS;
                    case "COMPRESSION" -> stage = STAGES.COMPRESSION_FACTOR;
                    case "DIMENSIONS" -> stage = STAGES.DIMENSIONS;
                    default -> {
                        switch (stage) {
                            case COMPRESSION_FACTOR -> {
                                String compression_string = line.replace("\n", "");
                                compression_factor = Integer.parseInt(compression_string);
                            }
                            case DIMENSIONS -> {
                                String[] dimensions = line.replace("\n", "").split(" ");
                                rows = Integer.parseInt(dimensions[0]);
                                cols = Integer.parseInt(dimensions[1]);
                                compressed_pixels_buffer = new Color[rows][cols];
                            }
                            case COLORS -> {
                                String[] color_strings = line.replace("\n", "").split(",");

                                for (String colorString : color_strings) {
                                    Color curr_color = new Color(Integer.parseInt(colorString, 16));
                                    colors_buffer.add(curr_color);
                                }
                            }
                            case PIXELS -> {
                                generatePixelDataFromCompressedFile(line, compressed_pixels_buffer, colors_buffer, curr_row, cols);
                                curr_row++;
                            }
                        }
                    }
                }
            } while (!stage.equals(STAGES.EOF));

            BufferedImage output = buildDecompressedImage(compressed_pixels_buffer, cols, rows, compression_factor);
            try {
                FileHandler.renderBufferedImage(img_name, "bmp", output);
            } catch (IOException e) {
                console.showInfo("==> Error rendering BufferedImage to file");
                console.showInfo(String.valueOf(e));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Color calculateRegionColorAverage(int x, int y, int rows, int cols, Color[][] pixel_buffer, int compression_factor) {
        long total_red = 0;
        long total_green = 0;
        long total_blue = 0;
        int iters = 0;

        for (int row = y; row < y + compression_factor; row++) {
            for (int col = x; col < x + compression_factor; col++) {
                if (row < rows && col < cols) {
                    Color color = pixel_buffer[row][col];

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

            return new Color(avgRed, avgGreen, avgBlue);
        }
    }

    private static Color[][] generatePixelData(BufferedImage image) {
        int rows = image.getHeight();
        int cols = image.getWidth();
        Color[][] pixel_buffer = new Color[rows][cols];

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                pixel_buffer[y][x] = new Color(image.getRGB(x,y), false);
            }
        }

        return pixel_buffer;
    }

    private static void generatePixelDataFromCompressedFile(String row_pixels, Color[][] pixels, List<Color> colors, int curr_row, int total_cols) {
        String[] pixel_strings = row_pixels.replace("\n", "").split(",");

        for (int col = 0; col < total_cols; col++) {
            pixels[curr_row][col] = colors.get(Integer.parseInt(pixel_strings[col]));
        }
    };

    private static BufferedImage buildDecompressedImage(Color[][] pixel_buffer, int cols, int rows, int compression_factor) {
        BufferedImage image = new BufferedImage(cols * compression_factor, rows * compression_factor, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < rows * compression_factor - compression_factor; y++) {
            for (int x = 0; x < cols * compression_factor - compression_factor; x++) {
                Tuple<Integer, Integer> p = new Tuple<>(x,y);
                Color intermediate_color = bilinearInterpolation(p, compression_factor, pixel_buffer);
                image.setRGB(x,y,intermediate_color.getRGB());
            }
        }

        return image;
    }

    private static Color bilinearInterpolation(Tuple<Integer, Integer> p, int compression_factor, Color[][] colors) {
        int x = p.x / compression_factor;
        int y = p.y / compression_factor;

        Color q11 = colors[y][x]; // bottom left
        Color q12 = colors[y + 1][x]; // top left
        Color q21 = colors[y][x + 1]; // bottom right
        Color q22 = colors[y + 1][x + 1]; // top right

        float x1 = x * compression_factor;
        float x2 = x1 + compression_factor;
        float y1 = y * compression_factor;
        float y2 = y1 + compression_factor;

        float x_diff = (p.x - x1) / (x2 - x1);
        float y_diff = (p.y - y1) / (y2 - y1);

        Color interpolatedColor = new Color(
                interpolate(q11.getRed(), q12.getRed(), q21.getRed(), q22.getRed(), x_diff, y_diff),
                interpolate(q11.getGreen(), q12.getGreen(), q21.getGreen(), q22.getGreen(), x_diff, y_diff),
                interpolate(q11.getBlue(), q12.getBlue(), q21.getBlue(), q22.getBlue(), x_diff, y_diff)
        );

        return interpolatedColor;
    }

    private static int interpolate(int q11, int q12, int q21, int q22, float x_diff, float y_diff) {
        // Bilinear interpolation formula
        int b1 = (int) (q11 * (1 - x_diff) * (1 - y_diff));
        int b2 = (int) (q21 * x_diff * (1 - y_diff));
        int b3 = (int) (q12 * (1 - x_diff) * y_diff);
        int b4 = (int) (q22 * x_diff * y_diff);

        return b1 + b2 + b3 + b4;
    }

}
