package com.nyx.bot.utils.image.combiner.combiner.element;


import com.nyx.bot.utils.image.combiner.combiner.enums.LineAlign;
import lombok.Getter;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TextElement extends CombineElement<TextElement> {

    private FontRenderContext metrics;
    private String text;
    private Font font;
    private boolean strikeThrough;
    private Color color = new Color(0, 0, 0);
    private Integer rotate;
    private Double lineHeight;
    private Double width;
    private Double height;
    private Integer drawY;
    private boolean autoBreakLine = false;
    private int maxLineWidth = 600;
    private int maxLineCount = 2;
    private LineAlign lineAlign;
    private List<TextElement> breakLineElements;

    public TextElement(String text, Font font, int x, int y) {
        this.lineAlign = LineAlign.Left;
        this.text = text;
        this.font = font;
        super.setX(x);
        super.setY(y);
    }

    public TextElement(String text, int fontSize, int x, int y) {
        this.lineAlign = LineAlign.Left;
        this.text = text;
        this.font = new Font("阿里巴巴普惠体", Font.PLAIN, fontSize);
        super.setX(x);
        super.setY(y);
    }

    public TextElement(String text, String fontName, int fontSize, int x, int y) {
        this.lineAlign = LineAlign.Left;
        this.text = text;
        this.font = new Font(fontName, Font.PLAIN, fontSize);
        super.setX(x);
        super.setY(y);
    }

    public Double getWidth() {
        if (this.width == null) {
            this.width = this.font.getStringBounds(this.text, this.metrics).getWidth();
        }

        return this.width;
    }

    public Double getHeight() {
        if (this.height == null) {
            if (this.autoBreakLine) {
                this.height = this.getLineHeight() * this.getBreakLineElements().size();
            } else {
                this.height = this.getLineHeight();
            }
        }

        return this.height;
    }

    public int getDrawY() {
        if (this.drawY == 0) {
            Rectangle2D rec = this.font.getStringBounds("", this.metrics);
            this.drawY = (int) (this.getY() + (this.getLineHeight() - rec.getHeight()) / 2);
        }

        return this.drawY;
    }

    public List<TextElement> getBreakLineElements() {
        if (this.breakLineElements == null) {
            this.breakLineElements = this.computeBreakLineElements();
        }

        return this.breakLineElements;
    }


    public TextElement setText(String text) {
        this.text = text;
        this.resetProperties();
        return this;
    }

    public TextElement setFont(Font font) {
        this.font = font;
        this.resetProperties();
        return this;
    }


    public TextElement setRotate(Integer rotate) {
        this.rotate = rotate;
        return this;
    }


    public TextElement setColor(Color color) {
        this.color = color;
        return this;
    }

    public TextElement setColor(int r, int g, int b) {
        return this.setColor(new Color(r, g, b));
    }

    public Double getLineHeight() {
        if (this.lineHeight == null) {
            Rectangle2D rec = this.font.getStringBounds("", this.metrics);
            this.lineHeight = rec.getHeight();
        }

        return this.lineHeight;
    }

    public TextElement setLineHeight(Double lineHeight) {
        this.lineHeight = lineHeight;
        this.resetProperties();
        return this;
    }

    public TextElement setStrikeThrough(boolean strikeThrough) {
        this.strikeThrough = strikeThrough;
        return this;
    }


    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, double lineHeight) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineHeight = lineHeight;
        return this;
    }

    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        return this;
    }

    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, double lineHeight, LineAlign lineAlign) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineHeight = lineHeight;
        this.lineAlign = lineAlign;
        return this;
    }

    public TextElement setAutoBreakLine(int maxLineWidth, int maxLineCount, LineAlign lineAlign) {
        this.autoBreakLine = true;
        this.maxLineWidth = maxLineWidth;
        this.maxLineCount = maxLineCount;
        this.lineAlign = lineAlign;
        return this;
    }


    private void resetProperties() {
        this.width = null;
        this.height = null;
        this.drawY = null;
        this.breakLineElements = null;
    }

    private FontRenderContext getMetrics() {
        if (this.metrics == null) {
            this.metrics = new FontRenderContext(new AffineTransform(), true, true);

        }

        return this.metrics;
    }

    private List<TextElement> computeBreakLineElements() {
        List<TextElement> breakLineElements = new ArrayList<>();
        List<String> breakLineTexts = this.computeLines(this.text);
        int currentY = this.getY();

        for (int i = 0; i < breakLineTexts.size() && i < this.maxLineCount; ++i) {
            String text = breakLineTexts.get(i);
            if (i == this.maxLineCount - 1 && i < breakLineTexts.size() - 1) {
                text = text.substring(0, text.length() - 1) + "...";
            }

            TextElement textLineElement = new TextElement(text, this.font, this.getX(), currentY);
            textLineElement.setColor(this.color);
            textLineElement.setStrikeThrough(this.strikeThrough);
            textLineElement.setCenter(this.isCenter());
            textLineElement.setAlpha(this.getAlpha());
            textLineElement.setRotate(this.rotate);
            textLineElement.setLineHeight(this.getLineHeight());
            textLineElement.setDirection(this.getDirection());
            breakLineElements.add(textLineElement);
            currentY += this.getLineHeight();
        }

        return breakLineElements;
    }

    private List<String> computeLines(String text) {
        List<String> computedLines = new ArrayList<>();
        String strToComputer = "";
        String word = "";
        boolean hasWord = false;
        char[] chars = text.toCharArray();
        int count = 0;

        for (int i = 0; i < chars.length && count++ <= 2000; ++i) {
            char c = chars[i];
            if (!this.isChineseChar(c) && c != ' ' && i != chars.length - 1) {
                word = word + c;
            } else {
                word = word + c;
                hasWord = true;
            }

            if (hasWord) {
                Rectangle2D rec = this.font.getStringBounds(strToComputer, this.metrics);
                Rectangle2D recw = this.font.getStringBounds(word, this.metrics);
                double originWidth = rec.getWidth();
                double wordWidth = recw.getWidth();
                strToComputer = strToComputer + word;
                double newWidth = originWidth + wordWidth;
                if (wordWidth > this.maxLineWidth) {
                    int fetch = (int) ((float) (this.maxLineWidth - originWidth) / (float) wordWidth * (float) word.length());
                    strToComputer = strToComputer.substring(0, strToComputer.length() - word.length() + fetch);
                    computedLines.add(strToComputer);
                    strToComputer = "";
                    i -= word.length() - fetch;
                } else if (newWidth > this.maxLineWidth) {
                    strToComputer = strToComputer.substring(0, strToComputer.length() - word.length());
                    computedLines.add(strToComputer);
                    strToComputer = "";
                    i -= word.length();
                }

                word = "";
                hasWord = false;
            }
        }

        if (!strToComputer.isEmpty()) {
            computedLines.add(strToComputer);
        }

        return computedLines;
    }

    private boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[一-龥]");
    }
}
