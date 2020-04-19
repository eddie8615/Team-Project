package org.cauldron.ui;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkHandle;

import static org.lwjgl.nuklear.Nuklear.nk_set_user_data;


public abstract class UIElement {
    protected NkContext ctx = null;
    protected String name = "";
    protected String title = "";
    protected float x = 0;
    protected float y = 0;
    public int id = -1;
    protected float rotation = 0;
    private boolean enable = false;
    protected float scale = 1; // ONLY USE FOR NO_INPUT ITEMS


    public UIElement(NkContext ctx, String name, int x, int y) {
        this.ctx = ctx;
        this.name = name;
        this.title = name;
        this.x = x;
        // Coordinate system flipped in y for Nuklear
        this.y = -y;
        this.id = UIHandler.genUUID();
    }

    protected void setUserDataUUID() {
        NkHandle handle = NkHandle.create();
        handle.id(id);
        nk_set_user_data(ctx, handle);
    }

    protected void resetUserDataUUID() {
        nk_set_user_data(ctx, NkHandle.create());
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = -y;
    }

    public void setPosition(Vector2f v) {
        this.x = v.x;
        this.y = -v.y;
    }

    public void setPosition(Vector2d v) {
        this.x = (float) v.x;
        this.y = (float) -v.y;
    }

    public void setRotation(float r) {
        this.rotation = -r;
    }

    public Vector2f getPosition() {
        return new Vector2f(x, -y);
    }

    public String getTitle() {
        return title;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float s) {
        scale = s;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract void layout();

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

    public void toggle() {
        enable = !enable;
    }
}
