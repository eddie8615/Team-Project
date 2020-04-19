package org.cauldron.entity.entities;

import org.cauldron.entity.Entity;
import org.cauldron.entity.PowerUpType;
import org.cauldron.entity.components.Mass;
import org.cauldron.entity.components.Material;
import org.cauldron.entity.components.Shape;
import org.cauldron.renderer.layers.Layer;

import java.util.Random;

import static org.cauldron.entity.EntityType.CRATE;

/**
 * Crates will give the person who deals the last bit of damage to open the crate a power-up.
 * Possibly the crate texture will change depending on its health value
 */
public class Crate extends Entity {
    private static final long serialVersionUID = -8168282023833602536L;
    public int hitsTaken = 0;
    public int maxHealth = 75;
    public int health = maxHealth;
    public PowerUpType powerUpType;

    public Crate(String id) {
        super(id);
        // gives the crate a random x position
        position.x = (int) (Math.random() * Layer.WIDTH);
        position.y = 450;
        rotation = 0;
        initCrate();
    }

    public Crate(String id, float x, float y, float rotation) {
        super(id);
        position.x = x;
        position.y = y;
        super.rotation = rotation;
        initCrate();
    }

    private void initCrate() {
        shape = new Shape(40, 40);
        depth = -0.001f;
        material = new Material.Wood();
        mass = new Mass(5);
        type = CRATE;
        powerUpType = PowerUpType.values()[new Random().nextInt(PowerUpType.values().length)];
    }
}
