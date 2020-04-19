package org.cauldron.entity.entities;

import org.cauldron.entity.Entity;
import org.cauldron.entity.components.Mass;
import org.cauldron.entity.components.Material;
import org.cauldron.entity.components.Shape;

import static org.cauldron.entity.EntityType.TURRET;

public class Turret extends Entity {
    //    public Source source = new Source();
    private static final long serialVersionUID = -7022282925770876956L;

    public Turret(String name) {
        super(name);
        rotation = 0;
        shape = new Shape(12, 50);
        material = new Material.TankMetal();
        mass = new Mass(50);
        type = TURRET;
//        source.setPosition((float)position.x, (float)position.y,1);
    }
}
