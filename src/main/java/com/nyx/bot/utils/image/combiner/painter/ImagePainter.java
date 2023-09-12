package com.nyx.bot.utils.image.combiner.painter;


import com.nyx.bot.utils.image.combiner.element.CombineElement;
import com.nyx.bot.utils.image.combiner.element.ImageElement;
import com.nyx.bot.utils.image.combiner.enums.Direction;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePainter implements IPainter {
    public ImagePainter() {
    }

    public void draw(Graphics2D g, CombineElement element, int canvasWidth) throws Exception {
        ImageElement imageElement = (ImageElement) element;
        BufferedImage image = imageElement.getImage();
        int width = 0;
        int height = 0;
        switch (imageElement.getZoomMode()) {
            case Origin -> {
                width = image.getWidth();
                height = image.getHeight();
            }
            case Width -> {
                width = imageElement.getWidth();
                height = image.getHeight() * width / image.getWidth();
            }
            case Height -> {
                height = imageElement.getHeight();
                width = image.getWidth() * height / image.getHeight();
            }
            case WidthHeight -> {
                height = imageElement.getHeight();
                width = imageElement.getWidth();
            }
        }

        if (imageElement.getRoundCorner() != null) {
            image = this.makeRoundCorner(image, width, height, imageElement.getRoundCorner());
        }

        if (imageElement.getBlur() != null) {
            image = this.makeBlur(image, imageElement.getBlur());
        }

        if (imageElement.isCenter()) {
            int centerX = (canvasWidth - width) / 2;
            imageElement.setX(centerX);
        } else if (imageElement.getDirection() == Direction.RightLeft) {
            imageElement.setX(imageElement.getX() - width);
        } else if (imageElement.getDirection() == Direction.CenterLeftRight) {
            imageElement.setX(imageElement.getX() - width / 2);
        }

        if (imageElement.getRotate() != null) {
            g.rotate(Math.toRadians((double) imageElement.getRotate()), imageElement.getX() + imageElement.getWidth() / 2, imageElement.getY() + imageElement.getHeight() / 2);
        }

        g.setComposite(AlphaComposite.getInstance(3, imageElement.getAlpha()));
        g.drawImage(image, imageElement.getX(), imageElement.getY(), width, height, null);
        if (imageElement.getRotate() != null) {
            g.rotate(-Math.toRadians((double) imageElement.getRotate()), imageElement.getX() + imageElement.getWidth() / 2, imageElement.getY() + imageElement.getHeight() / 2);
        }

    }

    private BufferedImage makeBlur(BufferedImage srcImage, int radius) {
        if (radius < 1) {
            return srcImage;
        } else {
            int w = srcImage.getWidth();
            int h = srcImage.getHeight();
            int[] pix = new int[w * h];
            srcImage.getRGB(0, 0, w, h, pix, 0, w);
            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;
            int[] r = new int[wh];
            int[] g = new int[wh];
            int[] b = new int[wh];
            int[] vmin = new int[Math.max(w, h)];
            int divsum = div + 1 >> 1;
            divsum *= divsum;
            int[] dv = new int[256 * divsum];

            int i;
            for (i = 0; i < 256 * divsum; ++i) {
                dv[i] = i / divsum;
            }

            int yi = 0;
            int yw = 0;
            int[][] stack = new int[div][3];
            int r1 = radius + 1;

            int rsum;
            int gsum;
            int bsum;
            int x;
            int y;
            int p;
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int routsum;
            int goutsum;
            int boutsum;
            int rinsum;
            int ginsum;
            int binsum;
            for (y = 0; y < h; ++y) {
                bsum = 0;
                gsum = 0;
                rsum = 0;
                boutsum = 0;
                goutsum = 0;
                routsum = 0;
                binsum = 0;
                ginsum = 0;
                rinsum = 0;

                for (i = -radius; i <= radius; ++i) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 16711680) >> 16;
                    sir[1] = (p & '\uff00') >> 8;
                    sir[2] = p & 255;
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }

                stackpointer = radius;

                for (x = 0; x < w; ++x) {
                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }

                    p = pix[yw + vmin[x]];
                    sir[0] = (p & 16711680) >> 16;
                    sir[1] = (p & '\uff00') >> 8;
                    sir[2] = p & 255;
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer % div];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    ++yi;
                }

                yw += w;
            }

            for (x = 0; x < w; ++x) {
                bsum = 0;
                gsum = 0;
                rsum = 0;
                boutsum = 0;
                goutsum = 0;
                routsum = 0;
                binsum = 0;
                ginsum = 0;
                rinsum = 0;
                int yp = -radius * w;

                for (i = -radius; i <= radius; ++i) {
                    yi = Math.max(0, yp) + x;
                    sir = stack[i + radius];
                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];
                    rbs = r1 - Math.abs(i);
                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }

                yi = x;
                stackpointer = radius;

                for (y = 0; y < h; ++y) {
                    pix[yi] = -16777216 & pix[yi] | dv[rsum] << 16 | dv[gsum] << 8 | dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }

                    p = x + vmin[y];
                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    yi += w;
                }
            }

            srcImage.setRGB(0, 0, w, h, pix, 0, w);
            return srcImage;
        }
    }

    private BufferedImage makeRoundCorner(BufferedImage srcImage, int width, int height, int radius) {
        BufferedImage image = new BufferedImage(width, height, 2);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(0, 0, width, height, radius, radius);
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(srcImage, 0, 0, width, height, null);
        g.dispose();
        return image;
    }
}

