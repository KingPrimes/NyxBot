package com.nyx.bot.utils.image.combiner.element;


import com.nyx.bot.utils.image.combiner.enums.Direction;

public abstract class CombineElement<T extends CombineElement> {
    private int x;
    private int y;
    private boolean center;
    private Direction direction;
    private float alpha;

    public CombineElement() {
        this.direction = Direction.LeftRight;
        this.alpha = 1.0F;
    }

    public int getX() {
        return this.x;
    }

    public T setX(int x) {
        this.x = x;
        return (T) this;
    }

    public int getY() {
        return this.y;
    }

    public T setY(int y) {
        this.y = y;
        return (T) this;
    }

    public boolean isCenter() {
        return this.center;
    }

    public T setCenter(boolean center) {
        this.center = center;
        return (T) this;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public T setDirection(Direction direction) {
        this.direction = direction;
        return (T) this;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public T setAlpha(float alpha) {
        this.alpha = alpha;
        return (T) this;
    }
}
