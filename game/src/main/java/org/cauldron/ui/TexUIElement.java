package org.cauldron.ui;

import org.joml.Vector2d;
import org.joml.Vector2f;

public class TexUIElement {
    protected String name = "";
    protected float x = 0;
    protected float y = 0;
    protected float rotation = 0;
    private boolean enable = false;
    public int texID = -1;
    private int defaultTexID = -1;
    private int width = 1;
    private int height = 1;

    public TexUIElement(String name, int x, int y, int width, int height, int tex) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.texID = tex;
        this.defaultTexID = tex;
        this.width = width;
        this.height = height;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setPosition(Vector2f v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void setPosition(Vector2d v) {
        this.x = (float) v.x;
        this.y = (float) v.y;
    }

    public void setRotation(float r) {
        this.rotation = r;
    }

    public Vector2f getPosition() {
        return new Vector2f(x, y);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return enable;
    }

    public void enable() {
        enable = true;
    }

    public void disable() {
        enable = false;
    }

    public float getRotation() {
        return rotation;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void resetTex() {
        texID = defaultTexID;
    }

    public void toggle() {
        enable = !enable;
    }
}
