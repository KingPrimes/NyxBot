/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.nyx.bot.utils.image.jhlabs.image;

import java.awt.*;
import java.awt.image.*;

/**
 * 一个抽象的超类，用于以某种方式扭曲图像的过滤器。子类只需要覆盖
 * 提供源像素和目标像素之间映射的两种方法。
 */
public abstract class TransformFilter extends AbstractBufferedImageOp {

    /**
     * 将边缘外的像素视为零。
     */
    public final static int ZERO = 0;

    /**
     *将像素夹在图像边缘。
     */
    public final static int CLAMP = 1;
    /**
     * 将像素从边缘包裹到对立边缘上。
     */
    public final static int WRAP = 2;

    /**
     * 将像素 RGB 固定到图像边缘，但将 alpha 归零。这可以防止图像上出现灰色边框。
     */
    public final static int RGB_CLAMP = 3;

    /**
     * 使用最近邻内插值。
     */
    public final static int NEAREST_NEIGHBOUR = 0;

    /**
     * 使用双线性插值。
     */
    public final static int BILINEAR = 1;

    /**
     * 对图像边缘的像素执行的操作。
     */
    protected int edgeAction = RGB_CLAMP;
    /**
     * 要使用的插值类型。
     */
    protected int interpolation = BILINEAR;
    /**
     * 输出图像矩形。
     */
    protected Rectangle transformedSpace;

    /**
     * The input image rectangle.
     */
    protected Rectangle originalSpace;

    /**
     * 获取要对图像边缘的像素执行的操作。
     *
     * @return零、夹或换行之一
     * @see #setEdgeAction
     */
    public int getEdgeAction() {
        return edgeAction;
    }

    /**
     * 设置要对图像边缘的像素执行的操作。
     *
     * @param边缘动作之一，零，夹或包裹
     * @see #getEdgeAction
     */
    public void setEdgeAction(int edgeAction) {
        this.edgeAction = edgeAction;
    }

    /**
     * 获取要执行的插值类型。
     *
     * @return NEAREST_NEIGHBOUR或双线性之一
     * @see #setInterpolation
     */
    public int getInterpolation() {
        return interpolation;
    }

    /**
     * 设置要执行的插值类型。
     *
     * @param插值NEAREST_NEIGHBOUR或双线性之一
     * @see #getInterpolation
     */
    public void setInterpolation(int interpolation) {
        this.interpolation = interpolation;
    }

    /**
     * 逆变换一个点。此方法需要由所有子类重写。
     *
     * @param x 输出图像中像素的 X 位置
     * @param y 输出图像中像素的 Y 位置
     * @param出输入图像中像素的位置
     */
    protected abstract void transformInverse(int x, int y, float[] out);

    /**
     * 前向变换矩形。用于确定输出图像的大小。
     *
     * @param矩形进行变换
     */
    protected void transformSpace(Rectangle rect) {
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        int type = src.getType();
        WritableRaster srcRaster = src.getRaster();

        originalSpace = new Rectangle(0, 0, width, height);
        transformedSpace = new Rectangle(0, 0, width, height);
        transformSpace(transformedSpace);

        if (dst == null) {
            ColorModel dstCM = src.getColorModel();
            dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(transformedSpace.width, transformedSpace.height), dstCM.isAlphaPremultiplied(), null);
        }
        WritableRaster dstRaster = dst.getRaster();

        int[] inPixels = getRGB(src, 0, 0, width, height, null);

        if (interpolation == NEAREST_NEIGHBOUR)
            return filterPixelsNN(dst, width, height, inPixels, transformedSpace);

        int srcWidth = width;
        int srcHeight = height;
        int srcWidth1 = width - 1;
        int srcHeight1 = height - 1;
        int outWidth = transformedSpace.width;
        int outHeight = transformedSpace.height;
        int outX, outY;
        int index = 0;
        int[] outPixels = new int[outWidth];

        outX = transformedSpace.x;
        outY = transformedSpace.y;
        float[] out = new float[2];

        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                transformInverse(outX + x, outY + y, out);
                int srcX = (int) Math.floor(out[0]);
                int srcY = (int) Math.floor(out[1]);
                float xWeight = out[0] - srcX;
                float yWeight = out[1] - srcY;
                int nw, ne, sw, se;

                if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
                    // Easy case, all corners are in the image
                    int i = srcWidth * srcY + srcX;
                    nw = inPixels[i];
                    ne = inPixels[i + 1];
                    sw = inPixels[i + srcWidth];
                    se = inPixels[i + srcWidth + 1];
                } else {
                    // Some of the corners are off the image
                    nw = getPixel(inPixels, srcX, srcY, srcWidth, srcHeight);
                    ne = getPixel(inPixels, srcX + 1, srcY, srcWidth, srcHeight);
                    sw = getPixel(inPixels, srcX, srcY + 1, srcWidth, srcHeight);
                    se = getPixel(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight);
                }
                outPixels[x] = ImageMath.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
            }
            setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
        }
        return dst;
    }

    public BufferedImage filter(BufferedImage src) {
        return filter(src, new BufferedImage(src.getWidth() * 2, src.getHeight() * 2, BufferedImage.TYPE_INT_ARGB));
    }

    final private int getPixel(int[] pixels, int x, int y, int width, int height) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            switch (edgeAction) {
                case ZERO:
                default:
                    return 0;
                case WRAP:
                    return pixels[(ImageMath.mod(y, height) * width) + ImageMath.mod(x, width)];
                case CLAMP:
                    return pixels[(ImageMath.clamp(y, 0, height - 1) * width) + ImageMath.clamp(x, 0, width - 1)];
                case RGB_CLAMP:
                    return pixels[(ImageMath.clamp(y, 0, height - 1) * width) + ImageMath.clamp(x, 0, width - 1)] & 0x00ffffff;
            }
        }
        return pixels[y * width + x];
    }

    protected BufferedImage filterPixelsNN(BufferedImage dst, int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int srcWidth = width;
        int srcHeight = height;
        int outWidth = transformedSpace.width;
        int outHeight = transformedSpace.height;
        int outX, outY, srcX, srcY;
        int[] outPixels = new int[outWidth];

        outX = transformedSpace.x;
        outY = transformedSpace.y;
        int[] rgb = new int[4];
        float[] out = new float[2];

        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                transformInverse(outX + x, outY + y, out);
                srcX = (int) out[0];
                srcY = (int) out[1];
                // int casting rounds towards zero, so we check out[0] < 0, not srcX < 0
                if (out[0] < 0 || srcX >= srcWidth || out[1] < 0 || srcY >= srcHeight) {
                    int p;
                    switch (edgeAction) {
                        case ZERO:
                        default:
                            p = 0;
                            break;
                        case WRAP:
                            p = inPixels[(ImageMath.mod(srcY, srcHeight) * srcWidth) + ImageMath.mod(srcX, srcWidth)];
                            break;
                        case CLAMP:
                            p = inPixels[(ImageMath.clamp(srcY, 0, srcHeight - 1) * srcWidth) + ImageMath.clamp(srcX, 0, srcWidth - 1)];
                            break;
                        case RGB_CLAMP:
                            p = inPixels[(ImageMath.clamp(srcY, 0, srcHeight - 1) * srcWidth) + ImageMath.clamp(srcX, 0, srcWidth - 1)] & 0x00ffffff;
                    }
                    outPixels[x] = p;
                } else {
                    int i = srcWidth * srcY + srcX;
                    rgb[0] = inPixels[i];
                    outPixels[x] = inPixels[i];
                }
            }
            setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
        }
        return dst;
    }

}

