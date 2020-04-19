package org.cauldron.entity.components;

import java.io.Serializable;

/**
 * Stores mass data
 * <p>
 * This makes calculating certain physics equations much quicker as we only have to calculate inverse mass once, rather than many times every game cycle.
 */
public class Mass implements Serializable {
    private static final long serialVersionUID = -3563329797495493727L;
    public float mass = 0;
    public float invMass = 0;
    public float inertia = 0;
    public float invInertia = 0;

    public Mass() {
    }

    public Mass(float mass) {
        this.mass = mass;
        if (this.mass == 0) {
            this.invMass = 0;
        } else {
            this.invMass = 1 / this.mass;
        }
        inertia = 0;
        invInertia = 0;
    }
}
