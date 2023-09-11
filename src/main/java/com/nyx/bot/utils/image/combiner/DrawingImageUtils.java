package com.nyx.bot.utils.image.combiner;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DrawingImageUtils {
    /**
     * 旋转图片
     *
     * @param image  图片
     * @param rotate 旋转的角度
     * @return 处理后的图片
     */
    public static BufferedImage rotateImage(BufferedImage image, double rotate) {
        double width = image.getWidth();
        double height = image.getHeight();
        int type = image.getType();

        BufferedImage result = new BufferedImage((int) width, (int) height, type);
        Graphics2D g2 = result.createGraphics();
        g2.rotate(Math.toRadians(rotate), width / 2, height / 2);
        g2.drawImage(image, null, 0, 0);
        return result;
    }

    /**
     * 二值化图像
     *
     * @param image 图像
     * @return 处理后的图像
     */
    public static BufferedImage addBlackWhiteFilter(BufferedImage image) {
        for (int h = 0; h < image.getHeight(); h++) {
            for (int w = 0; w < image.getWidth(); w++) {
                int gray = getGray(image, w, h);
                if (gray < 127) {
                    image.setRGB(w, h, 0xFFFFFFFF);
                } else {
                    image.setRGB(w, h, 0xFF000000);
                }
            }
        }
        return image;
    }

    /**
     * 镜像图像
     *
     * @param image 图像
     * @return 处理后的图像
     */
    public static BufferedImage mirrorImage(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int h = 0; h < image.getHeight(); h++) {
            for (int w = 0; w < image.getWidth(); w++) {
                newImage.setRGB(image.getWidth() - w - 1, h, image.getRGB(w, h));
            }
        }
        return newImage;
    }


    /**
     * 灰度化图像
     *
     * @param image 图像
     * @return 处理后的图像
     */
    public static BufferedImage grayImage(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int h = 0; h < image.getHeight(); h++) {
            for (int w = 0; w < image.getWidth(); w++) {
                int gray = getGray(image, w, h);
                newImage.setRGB(w, h, gray << 16 | gray << 8 | gray);
            }
        }
        return newImage;
    }


    /**
     * 取RGB平均值
     *
     * @param image 图像
     * @param x     像素的 X 坐标，从中获取默认 RGB 颜色模型和 sRGB 颜色空间中的像素
     * @param y     从中获取默认 RGB 颜色模型和 sRGB 颜色空间中的像素的像素的 Y 坐标
     * @return RGB平均值
     */
    static int getGray(BufferedImage image, int x, int y) {
        int r = image.getRGB(x, y) >> 16 & 0xFF;
        int g = image.getRGB(x, y) >> 8 & 0xFF;
        int b = image.getRGB(x, y) & 0xFF;
        return (r + g + b) / 3;
    }
}
