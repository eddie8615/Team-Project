package org.cauldron.entity.components;

import java.io.Serializable;

public class Shape implements Serializable {
    private static final long serialVersionUID = -6923852783929863337L;
    public float width;
    public float height;
    public float maxWidth;

    public Shape(float w, float h) {
        this.width = w;
        this.height = h;
        this.maxWidth = (float) Math.sqrt(w * w + h * h);
    }
}
