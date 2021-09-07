package cyder.genesis;

import cyder.utilities.ImageUtil;

import java.awt.image.BufferedImage;

public class OpenSimplexNoiseTest
{
    private static final int WIDTH = 256;
    private static final int HEIGHT = 256;
    private static final double FEATURE_SIZE = 24;

    public static void main(String[] args) {

        OpenSimplexNoise noise = new OpenSimplexNoise(290);
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, 0.0);
                int rgb = 0x010101 * (int)((value + 1) * 127.5);
                image.setRGB(x, y, rgb);
            }
        }
        ImageUtil.drawBufferedImage(image);
    }
}
