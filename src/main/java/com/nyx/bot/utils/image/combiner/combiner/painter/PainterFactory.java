package com.nyx.bot.utils.image.combiner.combiner.painter;


import com.nyx.bot.utils.image.combiner.combiner.element.CombineElement;
import com.nyx.bot.utils.image.combiner.combiner.element.ImageElement;
import com.nyx.bot.utils.image.combiner.combiner.element.RectangleElement;
import com.nyx.bot.utils.image.combiner.combiner.element.TextElement;

public class PainterFactory {
    private static ImagePainter imagePainter;
    private static TextPainter textPainter;
    private static RectanglePainter rectanglePainter;

    public PainterFactory() {
    }

    public static IPainter createInstance(CombineElement element) throws Exception {
        if (element instanceof ImageElement) {
            if (imagePainter == null) {
                imagePainter = new ImagePainter();
            }

            return imagePainter;
        } else if (element instanceof TextElement) {
            if (textPainter == null) {
                textPainter = new TextPainter();
            }

            return textPainter;
        } else if (element instanceof RectangleElement) {
            if (rectanglePainter == null) {
                rectanglePainter = new RectanglePainter();
            }

            return rectanglePainter;
        } else {
            throw new Exception("不支持的Painter类型");
        }
    }
}