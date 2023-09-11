package com.nyx.bot.utils.image.combiner.combiner;


import com.nyx.bot.utils.image.combiner.combiner.element.CombineElement;
import com.nyx.bot.utils.image.combiner.combiner.element.ImageElement;
import com.nyx.bot.utils.image.combiner.combiner.element.RectangleElement;
import com.nyx.bot.utils.image.combiner.combiner.element.TextElement;
import com.nyx.bot.utils.image.combiner.combiner.enums.OutputFormat;
import com.nyx.bot.utils.image.combiner.combiner.enums.ZoomMode;
import com.nyx.bot.utils.image.combiner.combiner.painter.IPainter;
import com.nyx.bot.utils.image.combiner.combiner.painter.PainterFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ImageCombiner {
    private final List<CombineElement> combineElements;
    private final int canvasWidth;
    private final int canvasHeight;
    private final OutputFormat outputFormat;
    private BufferedImage combinedImage;
    private Integer roundCorner;
    private Font font;

    /**
     * 构造ImageCombiner
     *
     * @param canvasWidth  图片宽
     * @param canvasHeight 图片高
     * @param outputFormat OutputFormat
     * @see OutputFormat
     */
    public ImageCombiner(int canvasWidth, int canvasHeight, OutputFormat outputFormat) {
        this.combineElements = new ArrayList<>();
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.outputFormat = outputFormat;
    }

    /**
     * 构造ImageCombiner
     *
     * @param canvasWidth  图片宽
     * @param canvasHeight 图片高
     * @param bgColor      Color
     * @param outputFormat OutputFormat
     * @see Color
     * @see OutputFormat
     */
    public ImageCombiner(int canvasWidth, int canvasHeight, Color bgColor, OutputFormat outputFormat) {
        this.combineElements = new ArrayList<>();
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.outputFormat = outputFormat;
        RectangleElement bgElement = new RectangleElement(0, 0, canvasWidth, canvasHeight);
        bgElement.setColor(bgColor);
        this.combineElements.add(bgElement);
    }

    /**
     * 通过Url图片地址构造
     *
     * @param bgImageUrl   Url地址
     * @param outputFormat OutputFormat
     * @throws Exception 错误信息
     * @see OutputFormat
     */
    public ImageCombiner(String bgImageUrl, OutputFormat outputFormat) throws Exception {
        this(ImageIO.read(new URL(bgImageUrl)), outputFormat);
    }

    /**
     * 通过现有的Image构造
     *
     * @param bgImage      BufferedImage
     * @param outputFormat OutputFormat
     * @see BufferedImage
     * @see OutputFormat
     */
    public ImageCombiner(BufferedImage bgImage, OutputFormat outputFormat) {
        this.combineElements = new ArrayList();
        ImageElement bgImageElement = new ImageElement(bgImage, 0, 0);
        this.combineElements.add(bgImageElement);
        this.canvasWidth = bgImage.getWidth();
        this.canvasHeight = bgImage.getHeight();
        this.outputFormat = outputFormat;
    }

    /**
     * 通过Url构造
     *
     * @param bgImageUrl   Url地址
     * @param width        图片宽
     * @param height       图片高
     * @param zoomMode     ZoomMode
     * @param outputFormat OutputFormat
     * @throws Exception 错误信息
     * @see ZoomMode
     * @see OutputFormat
     */
    public ImageCombiner(String bgImageUrl, int width, int height, ZoomMode zoomMode, OutputFormat outputFormat) throws Exception {
        this(ImageIO.read(new URL(bgImageUrl)), width, height, zoomMode, outputFormat);
    }

    /**
     * 通过现有的Image构造
     *
     * @param bgImage      BufferedImage
     * @param width        图片宽
     * @param height       图片高
     * @param zoomMode     ZoomMode
     * @param outputFormat OutputFormat
     * @see BufferedImage
     * @see ZoomMode
     * @see OutputFormat
     */
    public ImageCombiner(BufferedImage bgImage, int width, int height, ZoomMode zoomMode, OutputFormat outputFormat) {
        this.combineElements = new ArrayList();
        ImageElement bgImageElement = new ImageElement(bgImage, 0, 0, width, height, zoomMode);
        int canvasWidth = 0;
        int canvasHeight = 0;
        switch (zoomMode) {
            case Origin -> {
                canvasWidth = bgImage.getWidth();
                canvasHeight = bgImage.getHeight();
            }
            case Width -> {
                canvasWidth = width;
                canvasHeight = bgImage.getHeight() * width / bgImage.getWidth();
            }
            case Height -> {
                canvasHeight = height;
                canvasWidth = bgImage.getWidth() * height / bgImage.getHeight();
            }
            case WidthHeight -> {
                canvasHeight = width;
                canvasWidth = height;
            }
        }

        this.combineElements.add(bgImageElement);
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.outputFormat = outputFormat;
    }

    /**
     * 设置字体
     *
     * @param font 字体
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * 生成图片
     *
     * @return BufferedImage
     * @throws Exception 错误信息
     */
    public BufferedImage combine() throws Exception {
        this.combinedImage = new BufferedImage(this.canvasWidth, this.canvasHeight, 1);
        Graphics2D g = this.combinedImage.createGraphics();
        if (this.outputFormat == OutputFormat.PNG) {
            this.combinedImage = g.getDeviceConfiguration().createCompatibleImage(this.canvasWidth, this.canvasHeight, 3);
            g = this.combinedImage.createGraphics();
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.white);

        for (CombineElement element : this.combineElements) {
            IPainter painter = PainterFactory.createInstance(element);
            painter.draw(g, element, this.canvasWidth);
        }

        g.dispose();
        if (this.roundCorner != null) {
            this.combinedImage = this.makeRoundCorner(this.combinedImage, this.canvasWidth, this.canvasHeight, this.roundCorner);
        }
        return this.combinedImage;
    }

    /**
     * 设置背景模糊
     *
     * @param blur 模糊度
     */
    public void setBackgroundBlur(int blur) {
        ImageElement bgElement = (ImageElement) this.combineElements.get(0);
        bgElement.setBlur(blur);
    }

    /**
     * 设置图片圆角
     *
     * @param roundCorner 圆角度数
     * @throws Exception 错误信息
     */
    public ImageCombiner setCanvasRoundCorner(Integer roundCorner) throws Exception {
        if (this.outputFormat != OutputFormat.PNG) {
            throw new Exception("整图圆角，输出格式必须设置为PNG");
        } else {
            this.roundCorner = roundCorner;
        }
        return this;
    }

    /**
     * 获取画布宽度
     *
     * @return 画布宽度
     */
    public int getCanvasWidth() {
        return this.canvasWidth;
    }

    /**
     * 获取画布高度
     *
     * @return 画布高度
     */
    public int getCanvasHeight() {
        return this.canvasHeight;
    }

    /**
     * 获取组合图像
     *
     * @return BufferedImage
     * @see BufferedImage
     */
    public BufferedImage getCombinedImage() {
        return this.combinedImage;
    }

    /**
     * 获取图像流
     *
     * @return 流
     * @throws Exception 错误信息
     * @see InputStream
     */
    public InputStream getCombinedImageStream() throws Exception {
        if (this.combinedImage != null) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Throwable var2 = null;

                ByteArrayInputStream var3;
                try {
                    ImageIO.write(this.combinedImage, this.outputFormat.getName(), os);
                    var3 = new ByteArrayInputStream(os.toByteArray());
                } catch (Throwable var13) {
                    var2 = var13;
                    throw var13;
                } finally {
                    if (os != null) {
                        if (var2 != null) {
                            try {
                                os.close();
                            } catch (Throwable var12) {
                                var2.addSuppressed(var12);
                            }
                        } else {
                            os.close();
                        }
                    }

                }

                return var3;
            } catch (Exception var15) {
                throw new Exception("执行图片合成失败，无法输出文件流");
            }
        } else {
            throw new Exception("尚未执行图片合成，无法输出文件流");
        }
    }

    /**
     * 获取图像流
     *
     * @return 流
     * @throws Exception 错误信息
     * @see InputStream
     */
    public OutputStream getCombinedImageOutStream() throws Exception {
        if (this.combinedImage != null) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Throwable var2 = null;

                try {
                    ImageIO.write(this.combinedImage, this.outputFormat.getName(), os);
                } finally {
                    if (os != null) {
                        if (var2 != null) {
                            try {
                                os.close();
                            } catch (Throwable var12) {
                                var2.addSuppressed(var12);
                            }
                        } else {
                            os.close();
                        }
                    }

                }
                return os;
            } catch (Exception var15) {
                throw new Exception("执行图片合成失败，无法输出文件流");
            }
        } else {
            throw new Exception("尚未执行图片合成，无法输出文件流");
        }
    }


    /**
     * 保存图片
     *
     * @param filePath 保存路径
     * @throws Exception 错误信息
     */
    public void save(String filePath) throws Exception {
        if (this.combinedImage != null) {
            ImageIO.write(this.combinedImage, this.outputFormat.getName(), new File(filePath));
        } else {
            throw new Exception("尚未执行图片合成，无法保存文件");
        }
    }

    /**
     * 添加元素
     *
     * @param element 元素
     * @see ImageElement
     * @see TextElement
     * @see RectangleElement
     */
    public void addElement(CombineElement element) {
        this.combineElements.add(element);
    }

    /**
     * 通过Url图片地址添加
     *
     * @param imgUrl Url
     * @param x      x
     * @param y      y
     * @return ImageElement
     * @throws Exception 错误信息
     * @see ImageElement
     */
    public ImageElement addImageElement(String imgUrl, int x, int y) throws Exception {
        ImageElement imageElement = new ImageElement(imgUrl, x, y);
        this.combineElements.add(imageElement);
        return imageElement;
    }

    /**
     * 通过BufferedImage添加
     *
     * @param image image
     * @param x     x
     * @param y     y
     * @return ImageElement
     * @see BufferedImage
     * @see ImageElement
     */
    public ImageElement addImageElement(BufferedImage image, int x, int y) {
        ImageElement imageElement = new ImageElement(image, x, y);
        this.combineElements.add(imageElement);
        return imageElement;
    }

    /**
     * 通过Url图片地址添加
     *
     * @param imgUrl   Url
     * @param x        x
     * @param y        y
     * @param width    width
     * @param height   height
     * @param zoomMode ZoomMode
     * @return ImageElement
     * @see ZoomMode
     * @see ImageElement
     */
    public ImageElement addImageElement(String imgUrl, int x, int y, int width, int height, ZoomMode zoomMode) {
        ImageElement imageElement = new ImageElement(imgUrl, x, y, width, height, zoomMode);
        this.combineElements.add(imageElement);
        return imageElement;
    }

    /**
     * 通过BufferedImage添加
     *
     * @param image    image
     * @param x        x
     * @param y        y
     * @param width    width
     * @param height   height
     * @param zoomMode ZoomMode
     * @return ImageElement
     * @see BufferedImage
     * @see ZoomMode
     * @see ImageElement
     */
    public ImageElement addImageElement(BufferedImage image, int x, int y, int width, int height, ZoomMode zoomMode) {
        ImageElement imageElement = new ImageElement(image, x, y, width, height, zoomMode);
        this.combineElements.add(imageElement);
        return imageElement;
    }

    /**
     * 添加文字
     *
     * @param text 文字
     * @param font 字体
     * @param x    x
     * @param y    y
     * @return TextElement
     * @see TextElement
     */
    public TextElement addTextElement(String text, Font font, int x, int y) {
        TextElement textElement = new TextElement(text, font, x, y);
        this.combineElements.add(textElement);
        return textElement;
    }

    /**
     * 添加文字 使用此方法前 必须setFont
     *
     * @param text 文字
     * @param x    x
     * @param y    y
     * @return TextElement
     * @see TextElement
     */
    public TextElement addTextElement(String text, int x, int y) {
        TextElement textElement = new TextElement(text, font, x, y);
        this.combineElements.add(textElement);
        return textElement;
    }

    /**
     * 添加文字
     *
     * @param text     文字
     * @param fontSize 字体大小
     * @param x        x
     * @param y        y
     * @return TextElement
     * @see TextElement
     */
    public TextElement addTextElement(String text, int fontSize, int x, int y) {
        TextElement textElement = new TextElement(text, fontSize, x, y);
        this.combineElements.add(textElement);
        return textElement;
    }

    /**
     * 添加文字
     *
     * @param text     文字
     * @param fontName 字体名称
     * @param fontSize 字体大小
     * @param x        x
     * @param y        y
     * @return TextElement
     * @see TextElement
     */
    public TextElement addTextElement(String text, String fontName, int fontSize, int x, int y) {
        TextElement textElement = new TextElement(text, fontName, fontSize, x, y);
        this.combineElements.add(textElement);
        return textElement;
    }

    /**
     * 添加矩形
     *
     * @param x      x
     * @param y      y
     * @param width  宽
     * @param height 高
     * @return RectangleElement
     * @see RectangleElement
     */
    public RectangleElement addRectangleElement(int x, int y, int width, int height) {
        RectangleElement rectangleElement = new RectangleElement(x, y, width, height);
        this.combineElements.add(rectangleElement);
        return rectangleElement;
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

