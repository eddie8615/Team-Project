package org.cauldron.renderer;

public class Backdrop {
    public int texID = -1;
    public float x = 0;
    public float y = 0;

    public Backdrop(int texID) {
        this.texID = texID;
    }

    public Backdrop(Backdrop b) {
        this.texID = b.texID;
        this.x = b.x;
        this.y = b.y;
    }

    public Backdrop(int texID, float x, float y) {
        this.texID = texID;
        this.x = x;
        this.y = y;
    }
}
