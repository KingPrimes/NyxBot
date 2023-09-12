package com.nyx.bot.utils.image.combiner.element;


import com.nyx.bot.utils.image.combiner.enums.GradientDirection;
import lombok.Getter;

import java.awt.*;

@Getter
public class RectangleElement extends CombineElement<RectangleElement> {
    private Integer width;
    private Integer height;
    private Integer roundCorner = 0;
    private Color color = new Color(255, 255, 255);
    private Color fromColor;
    private Color toColor;
    private Integer fromExtend = 0;
    private Integer toExtend = 0;
    private GradientDirection gradientDirection;

    public RectangleElement(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        super.setX(x);
        super.setY(y);
    }

    public RectangleElement setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public RectangleElement setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public RectangleElement setRoundCorner(Integer roundCorner) {
        this.roundCorner = roundCorner;
        return this;
    }

    public RectangleElement setColor(Color color) {
        this.color = color;
        return this;
    }

    public RectangleElement setColor(int r, int g, int b) {
        return this.setColor(new Color(r, g, b));
    }

    public RectangleElement setGradient(Color fromColor, Color toColor, GradientDirection gradientDirection) {
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.gradientDirection = gradientDirection;
        return this;
    }

    public RectangleElement setGradient(Color fromColor, Color toColor, int fromExtend, int toExtend, GradientDirection gradientDirection) {
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.fromExtend = fromExtend;
        this.toExtend = toExtend;
        this.gradientDirection = gradientDirection;
        return this;
    }

}
