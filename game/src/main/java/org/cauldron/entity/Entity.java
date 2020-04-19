package org.cauldron.entity;

import org.cauldron.entity.components.Friction;
import org.cauldron.entity.components.Mass;
import org.cauldron.entity.components.Material;
import org.cauldron.entity.components.Shape;
import org.cauldron.game.Physics;
import org.cauldron.renderer.Renderer;
import org.joml.Vector2d;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Entity implements Serializable {
    private static final long serialVersionUID = 4755776264473110998L;
    public String name;
    public int loc;
    public Shape shape;
    public Vector2d position = new Vector2d();
    public float rotation = 0;
    public Material material = new Material();
    public Mass mass = new Mass();
    public Friction frictionData = new Friction();
    public Vector2d velocity = new Vector2d();
    public Map<String, Vector2d> forces = new HashMap<>();
    public float drivingForce = 0;
    public float depth = 0;
    public int parent;
    public EntityType type = null;
    public Renderer.COLOR blockColor = null;
    public String playerColor = "green";
    public double creationTime = -1;
    public double lifespan = 0f;

    public Entity(String name) {
        this.name = name;
        this.loc = Integer.parseInt(name.substring(0, name.indexOf("_")));
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return this.name.equals(entity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * @return the four coordinates that make up the rectangle of this entity. [Bottom Left, Top Left, Top Right, Bottom Right]
     */
    public Vector2d[] getCornerCoords() {
        double a = Math.toRadians(this.rotation);
        float x = (float) this.position.x;
        float y = (float) this.position.y;
        float w = this.shape.width;
        float h = this.shape.height;
        return new Vector2d[]{new Vector2d(x, y),//bottom left
                new Vector2d(x + -h * Math.sin(a), y + h * Math.cos(a)),//top left
                new Vector2d(x + -h * Math.sin(a) + w * Math.cos(a), y + h * Math.cos(a) + w * Math.sin(a)),//top right
                new Vector2d(x + w * Math.cos(a), y + w * Math.sin(a)) //bottom right
        };
    }

    public void updateNormal() {
        Vector2d normal = new Vector2d(0, this.mass.mass * -Physics.GRAVITY * Math.cos(Math.toRadians(rotation)));
        forces.put("normal", normal);
    }

    public void updateFriction() {
        double deltaX = 0;
        double deltaY = 0;
        if (velocity.x > 0.1f || velocity.x < -0.1f) {
            deltaX = 0.9f * (0 - velocity.x);
        } else {
            velocity.x = 0;
        }
        if (velocity.y > 0.1f || velocity.y < -0.1f) {
            deltaY = 0.9f * (0 - velocity.y);
        } else {
            velocity.y = 0;
        }

        Vector2d friction = new Vector2d(mass.mass * deltaX, mass.mass * deltaY);
        forces.put("friction", friction);
    }

    public Vector2d getResultant() {
        Vector2d resultant = new Vector2d();
        for (Map.Entry<String, Vector2d> force : forces.entrySet()) {
            resultant.add(force.getValue());
        }
        resultant.add(new Vector2d(drivingForce * Math.cos(Math.toRadians(rotation)),
                drivingForce * Math.sin(Math.toRadians(rotation))));

        return resultant;
    }

    @Override
    public String toString() {
        return name;
    }
}
