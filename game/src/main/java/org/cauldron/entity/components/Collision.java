package org.cauldron.entity.components;

import org.joml.Vector2d;

import java.io.Serializable;

/**
 * A data structure to hold the information about a collision. Are they colliding? and if they are then what is the collision normal?
 */
public class Collision implements Serializable {
    private static final long serialVersionUID = 4548674616210980304L;

    public Vector2d colNormal;
    public boolean colliding;

    public Collision(boolean b, Vector2d colNormal) {
        this.colliding = b;
        this.colNormal = colNormal;
    }

    public Collision(boolean b) {
        this.colliding = b;
        //when there isnt a colision normal we will use a random one
        // such as when one object is fully within another!
        //TODO fix collision normals
        this.colNormal = new Vector2d(1, 0);
    }
}
