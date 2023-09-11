package com.nyx.bot.utils.image.combiner.combiner.painter;


import com.nyx.bot.utils.image.combiner.combiner.element.CombineElement;

import java.awt.*;
import java.io.IOException;

public interface IPainter {
    /**
     * 绘制图片
     *
     * @param var1 Graphics2D
     * @param var2 CombineElement
     * @param var3 圆角度
     * @throws IOException
     * @throws Exception
     */
    void draw(Graphics2D var1, CombineElement var2, int var3) throws IOException, Exception;
}

