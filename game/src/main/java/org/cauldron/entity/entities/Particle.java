package org.cauldron.entity.entities;

import org.cauldron.entity.Entity;
import org.cauldron.entity.components.Mass;
import org.cauldron.entity.components.Material;
import org.cauldron.entity.components.Shape;
import org.cauldron.renderer.Renderer;

import static org.cauldron.entity.EntityType.PARTICLE;

public class Particle extends Entity {

    public Particle(String name, double lifespan, Renderer.COLOR color) {
        super(name);
        rotation = 0;
        shape = new Shape(3f, 3f);
        depth = -0.001f;
        material = new Material.Rock();
        mass = new Mass(5);
        super.blockColor = color;
        type = PARTICLE;
        super.lifespan = lifespan;
    }
}
