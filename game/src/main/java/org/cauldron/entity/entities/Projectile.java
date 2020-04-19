package org.cauldron.entity.entities;

import org.cauldron.entity.Entity;
import org.cauldron.entity.components.Mass;
import org.cauldron.entity.components.Material;
import org.cauldron.entity.components.Shape;
import org.joml.Vector2d;

import static org.cauldron.entity.EntityType.PROJECTILE;

public class Projectile extends Entity {
    private static final long serialVersionUID = -8188734675068563306L;
    public int damage = 25;
    public boolean guided = false;
    public Vector2d target;

    public Projectile(String id) {
        super(id);
        rotation = 0;
        initProjectile();
    }

    private void initProjectile() {
        shape = new Shape(10f, 20f);
        depth = -0.001f;
        material = new Material.Rock();
        mass = new Mass(5);
        type = PROJECTILE;
    }
}
