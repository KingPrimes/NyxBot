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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * 对图像执行透视失真的滤镜。
 * 坐标被视为图像是单位正方形，即图像的右下角位于 （1， 1）。
 * 过滤器将单位正方形映射到任意凸四边形上，反之亦然。
 */
public class PerspectiveFilter extends TransformFilter {

    private float x0, y0, x1, y1, x2, y2, x3, y3;
    private float dx1, dy1, dx2, dy2, dx3, dy3;
    private float A, B, C, D, E, F, G, H, I;
    private float a11, a12, a13, a21, a22, a23, a31, a32, a33;
    private boolean scaled;
    private boolean clip = false;

    /**
     * 构造透视过滤器。
     */
    public PerspectiveFilter() {
        this(0, 0, 1, 0, 1, 1, 0, 1);
    }

    /**
     * 构造透视过滤器。
     *
     * @param x0 左上角的新位置
     * @param y0 左上角的新位置
     * @param x1 右上角的新位置
     * @param y1 右上角的新位置
     * @param x2 右下角的新位置
     * @param y2 右下角的新位置
     * @param x3 左下角的新位置
     * @param y3 左下角的新位置
     */
    public PerspectiveFilter(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        unitSquareToQuad(x0, y0, x1, y1, x2, y2, x3, y3);
    }

    public boolean getClip() {
        return clip;
    }

    public void setClip(boolean clip) {
        this.clip = clip;
    }

    /**
     * 设置图像角的新位置
     * 这与 unitSquareToQuad() 相同,但坐标以像素为单位
     * 与单位正方形无关
     * 提供此方法是为了方便
     *
     * @param x0 左上角 X坐标
     * @param y0 左上角 Y坐标
     * @param x1 右上角 X坐标
     * @param y1 右上角 Y坐标
     * @param x2 右下角 X坐标
     * @param y2 右下角 Y坐标
     * @param x3 左下角 X坐标
     * @param y3 左下角 Y坐标
     */
    public void setCorners(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        unitSquareToQuad(x0, y0, x1, y1, x2, y2, x3, y3);
        scaled = true;
    }

    /**
     设置变换以将单位正方形映射到四边形上。过滤时，所有坐标都将按图像大小缩放。
     *
     * @param x0 左上角的新位置
     * @param y0 左上角的新位置
     * @param x1 右上角的新位置
     * @param y1 右上角的新位置
     * @param x2 右下角的新位置
     * @param y2 右下角的新位置
     * @param x3 左下角的新位置
     * @param y3 左下角的新位置
     */
    public void unitSquareToQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;

        dx1 = x1 - x2;
        dy1 = y1 - y2;
        dx2 = x3 - x2;
        dy2 = y3 - y2;
        dx3 = x0 - x1 + x2 - x3;
        dy3 = y0 - y1 + y2 - y3;

        if (dx3 == 0 && dy3 == 0) {
            a11 = x1 - x0;
            a21 = x2 - x1;
            a31 = x0;
            a12 = y1 - y0;
            a22 = y2 - y1;
            a32 = y0;
            a13 = a23 = 0;
        } else {
            a13 = (dx3 * dy2 - dx2 * dy3) / (dx1 * dy2 - dy1 * dx2);
            a23 = (dx1 * dy3 - dy1 * dx3) / (dx1 * dy2 - dy1 * dx2);
            a11 = x1 - x0 + a13 * x1;
            a21 = x3 - x0 + a23 * x3;
            a31 = x0;
            a12 = y1 - y0 + a13 * y1;
            a22 = y3 - y0 + a23 * y3;
            a32 = y0;
        }
        a33 = 1;
        scaled = false;
    }

    /**
     * 设置变换以将四边形映射到单位正方形。过滤时，所有坐标都将被缩放
     * 按图像大小。
     *
     * @param x0 左上角的旧位置
     * @param y0 左上角的旧位置
     * @param x1 右上角的旧位置
     * @param y1 右上角的旧位置
     * @param x2 右下角的旧位置
     * @param y2 右下角的旧位置
     * @param x3 左下角的旧位置
     * @param y3 左下角的旧位置
     */
    public void quadToUnitSquare(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        unitSquareToQuad(x0, y0, x1, y1, x2, y2, x3, y3);

        // Invert the transformation
        float ta11 = a22 * a33 - a32 * a23;
        float ta21 = a32 * a13 - a12 * a33;
        float ta31 = a12 * a23 - a22 * a13;
        float ta12 = a31 * a23 - a21 * a33;
        float ta22 = a11 * a33 - a31 * a13;
        float ta32 = a21 * a13 - a11 * a23;
        float ta13 = a21 * a32 - a31 * a22;
        float ta23 = a31 * a12 - a11 * a32;
        float ta33 = a11 * a22 - a21 * a12;
        float f = 1.0f / ta33;

        a11 = ta11 * f;
        a21 = ta12 * f;
        a31 = ta13 * f;
        a12 = ta21 * f;
        a22 = ta22 * f;
        a32 = ta23 * f;
        a13 = ta31 * f;
        a23 = ta32 * f;
        a33 = 1.0f;
    }

    /**
     * 处理图像
     *
     * @param src 要处理的图像
     * @param dst 图像底图
     * @return 处理之后的图像
     */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (dst == null) {
            dst = new BufferedImage(src.getWidth() * 2, src.getHeight() * 2, BufferedImage.TYPE_INT_ARGB);
        }
        A = a22 * a33 - a32 * a23;
        B = a31 * a23 - a21 * a33;
        C = a21 * a32 - a31 * a22;
        D = a32 * a13 - a12 * a33;
        E = a11 * a33 - a31 * a13;
        F = a31 * a12 - a11 * a32;
        G = a12 * a23 - a22 * a13;
        H = a21 * a13 - a11 * a23;
        I = a11 * a22 - a21 * a12;
        if (!scaled) {
            int width = src.getWidth();
            int height = src.getHeight();
            float invWidth = 1.0f / width;
            float invHeight = 1.0f / height;

            A *= invWidth;
            D *= invWidth;
            G *= invWidth;
            B *= invHeight;
            E *= invHeight;
            H *= invHeight;
        }

        return super.filter(src, dst);
    }

    /**
     * 处理图像
     *
     * @param src 要处理的图像
     * @return 处理之后的图像
     */
    public BufferedImage filter(BufferedImage src) {
        return filter(src, null);
    }

    protected void transformSpace(Rectangle rect) {
        if (scaled) {
            rect.x = (int) Math.min(Math.min(x0, x1), Math.min(x2, x3));
            rect.y = (int) Math.min(Math.min(y0, y1), Math.min(y2, y3));
            rect.width = (int) Math.max(Math.max(x0, x1), Math.max(x2, x3)) - rect.x;
            rect.height = (int) Math.max(Math.max(y0, y1), Math.max(y2, y3)) - rect.y;
            return;
        }
        if (!clip) {
            float w = (float) rect.getWidth(), h = (float) rect.getHeight();
            Rectangle r = new Rectangle();
            r.add(getPoint2D(new Point2D.Float(0, 0), null));
            r.add(getPoint2D(new Point2D.Float(w, 0), null));
            r.add(getPoint2D(new Point2D.Float(0, h), null));
            r.add(getPoint2D(new Point2D.Float(w, h), null));
            rect.setRect(r);
        }
    }

    /**
     * 获取输出图像的原点。使用它来确定在哪里绘制新图像。
     *
     * @return X 原点。
     */
    public float getOriginX() {
        return x0 - (int) Math.min(Math.min(x0, x1), Math.min(x2, x3));
    }

    /**
     * 获取输出图像的原点。使用它来确定在哪里绘制新图像。
     *
     * @return Y 原点。
     */
    public float getOriginY() {
        return y0 - (int) Math.min(Math.min(y0, y1), Math.min(y2, y3));
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
        if (clip)
            return new Rectangle(0, 0, src.getWidth(), src.getHeight());
        float w = src.getWidth(), h = src.getHeight();
        Rectangle2D r = new Rectangle2D.Float();
        r.add(getPoint2D(new Point2D.Float(0, 0), null));
        r.add(getPoint2D(new Point2D.Float(w, 0), null));
        r.add(getPoint2D(new Point2D.Float(0, h), null));
        r.add(getPoint2D(new Point2D.Float(w, h), null));
        return r;
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null)
            dstPt = new Point2D.Float();
        float x = (float) srcPt.getX();
        float y = (float) srcPt.getY();
        float f = 1.0f / (x * a13 + y * a23 + a33);
        dstPt.setLocation((x * a11 + y * a21 + a31) * f, (x * a12 + y * a22 + a32) * f);
        return dstPt;
    }

    protected void transformInverse(int x, int y, float[] out) {
        out[0] = originalSpace.width * (A * x + B * y + C) / (G * x + H * y + I);
        out[1] = originalSpace.height * (D * x + E * y + F) / (G * x + H * y + I);
    }

    public String toString() {
        return "Distort/Perspective...";
    }
}

