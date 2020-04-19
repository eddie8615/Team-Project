package org.cauldron.entity.components;

import java.io.Serializable;

/**
 * Stores friction coefficient data
 */
public class Friction implements Serializable {
    private static final long serialVersionUID = -2278072716221607559L;
    public float staticFriction = 1.7f;
    public float dynamicFriction = 0.3f;
}
