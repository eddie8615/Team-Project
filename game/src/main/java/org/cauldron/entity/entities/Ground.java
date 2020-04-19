package org.cauldron.entity.entities;

import org.cauldron.entity.Entity;
import org.cauldron.entity.components.Mass;
import org.cauldron.entity.components.Material;
import org.cauldron.entity.components.Shape;
import org.cauldron.game.Terrain;
import org.cauldron.renderer.layers.Layer;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import static org.cauldron.entity.EntityType.GROUND;

public class Ground extends Entity {
    private static final long serialVersionUID = -5516413529279723034L;
    public Terrain map;
    private Vector2d[] coords;

    public Ground(String id, Terrain map) {
        super(id);
        this.map = map;
        coords = new Vector2d[map.vertices.size() + 2];
        coords[0] = new Vector2d(0, 0);
        coords[map.vertices.size() + 1] = new Vector2d(Layer.WIDTH, 0);
        for (int i = 1; i < map.vertices.size() + 1; i++)
            coords[i] = map.vertices.get(i - 1);
        position.x = 200;
        position.y = 450;
        rotation = 0;
        initGround();
    }

    private void initGround() {
        shape = new Shape(Layer.WIDTH, Layer.HEIGHT);
        material = new Material.Static();
        mass = new Mass(100000);
        type = GROUND;
    }

    @Override
    public Vector2d[] getCornerCoords() {
        return coords;
    }
}
