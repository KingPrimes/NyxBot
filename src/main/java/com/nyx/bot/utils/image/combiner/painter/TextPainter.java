package com.nyx.bot.utils.image.combiner.painter;


import com.nyx.bot.utils.image.combiner.element.CombineElement;
import com.nyx.bot.utils.image.combiner.element.TextElement;
import com.nyx.bot.utils.image.combiner.enums.Direction;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

public class TextPainter implements IPainter {
    public TextPainter() {
    }

    public void draw(Graphics2D g, CombineElement element, int canvasWidth) {
        TextElement textElement = (TextElement) element;
        List<TextElement> textLineElements = new ArrayList<>();
        textLineElements.add(textElement);
        if (textElement.isAutoBreakLine()) {
            textLineElements = textElement.getBreakLineElements();
        }

        for (int i = 0; i < textLineElements.size(); ++i) {
            TextElement firstLineElement = (TextElement) ((List<?>) textLineElements).get(0);
            TextElement currentLineElement = (TextElement) ((List<?>) textLineElements).get(i);
            g.setFont(currentLineElement.getFont());
            g.setColor(currentLineElement.getColor());
            if (currentLineElement.isCenter()) {
                if (i == 0) {
                    currentLineElement.setX((int) ((canvasWidth - currentLineElement.getWidth()) / 2));
                } else {
                    switch (textElement.getLineAlign()) {
                        case Left -> currentLineElement.setX(firstLineElement.getX());
                        case Center ->
                                currentLineElement.setX((int) ((canvasWidth - currentLineElement.getWidth()) / 2));
                        case Right ->
                                currentLineElement.setX((int) (firstLineElement.getX() + firstLineElement.getWidth() - currentLineElement.getWidth()));
                    }
                }
            } else if (i == 0) {
                if (currentLineElement.getDirection() == Direction.RightLeft) {
                    currentLineElement.setX((int) (currentLineElement.getX() - currentLineElement.getWidth()));
                } else if (currentLineElement.getDirection() == Direction.CenterLeftRight) {
                    currentLineElement.setX((int) (currentLineElement.getX() - currentLineElement.getWidth() / 2));
                }
            } else {
                switch (textElement.getLineAlign()) {
                    case Left -> currentLineElement.setX(firstLineElement.getX());
                    case Center ->
                            currentLineElement.setX((int) (firstLineElement.getX() + (firstLineElement.getWidth() - currentLineElement.getWidth()) / 2));
                    case Right ->
                            currentLineElement.setX((int) (firstLineElement.getX() + firstLineElement.getWidth() - currentLineElement.getWidth()));
                }
            }

            if (currentLineElement.getRotate() != null) {
                g.rotate(Math.toRadians((double) currentLineElement.getRotate()), currentLineElement.getX() + currentLineElement.getWidth() / 2, currentLineElement.getDrawY());
            }

            g.setComposite(AlphaComposite.getInstance(3, currentLineElement.getAlpha()));
            if (currentLineElement.isStrikeThrough()) {
                AttributedString as = new AttributedString(currentLineElement.getText());
                as.addAttribute(TextAttribute.FONT, currentLineElement.getFont());
                as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, 0, currentLineElement.getText().length());
                g.drawString(as.getIterator(), currentLineElement.getX(), currentLineElement.getDrawY());
            } else {
                g.drawString(currentLineElement.getText(), currentLineElement.getX(), currentLineElement.getDrawY());
            }

            if (currentLineElement.getRotate() != null) {
                g.rotate(-Math.toRadians((double) currentLineElement.getRotate()), currentLineElement.getX() + currentLineElement.getWidth() / 2, currentLineElement.getDrawY());
            }
        }

    }
}

