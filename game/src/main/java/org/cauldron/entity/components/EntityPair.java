package org.cauldron.entity.components;

import org.cauldron.entity.Entity;

/**
 * A pair of entities. Custom equals method to test if the same pair in the different order is the same.
 */
public class EntityPair {
    public Entity A;
    public Entity B;

    public EntityPair(Entity A, Entity B) {
        this.A = A;
        this.B = B;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EntityPair)) return false;
        EntityPair otherPair = (EntityPair) object;
        if ((A.equals(otherPair.A)) && B.equals(otherPair.B)) return true;
        return (A.equals(otherPair.B)) && B.equals(otherPair.A);
    }

    public String toString() {
        return A.toString() + " and " + A.toString();
    }

    @Override
    public int hashCode() {
        int result = A != null ? A.hashCode() : 0;
        result = 31 * result + (B != null ? B.hashCode() : 0);
        return result;
    }
}
