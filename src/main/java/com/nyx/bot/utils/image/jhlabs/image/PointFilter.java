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


import java.awt.image.*;

/**
 * 点滤波器的抽象超类。该接口与旧的RGBImageFilter相同。
 */
public abstract class PointFilter extends AbstractBufferedImageOp {

    protected boolean canFilterIndexColorModel = false;

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        int type = src.getType();
        WritableRaster srcRaster = src.getRaster();

        if (dst == null)
            dst = createCompatibleDestImage(src, null);
        WritableRaster dstRaster = dst.getRaster();

        setDimensions(width, height);

        int[] inPixels = new int[width];
        for (int y = 0; y < height; y++) {
            // 我们尽量避免在图像上调用 getRGB，因为这会导致它们变得不受管理，从而导致可怕的性能问题。
            if (type == BufferedImage.TYPE_INT_ARGB) {
                srcRaster.getDataElements(0, y, width, 1, inPixels);
                for (int x = 0; x < width; x++)
                    inPixels[x] = filterRGB(x, y, inPixels[x]);
                dstRaster.setDataElements(0, y, width, 1, inPixels);
            } else {
                src.getRGB(0, y, width, 1, inPixels, 0, width);
                for (int x = 0; x < width; x++)
                    inPixels[x] = filterRGB(x, y, inPixels[x]);
                dst.setRGB(0, y, width, 1, inPixels, 0, width);
            }
        }

        return dst;
    }

    public void setDimensions(int width, int height) {
    }

    public abstract int filterRGB(int x, int y, int rgb);
}
