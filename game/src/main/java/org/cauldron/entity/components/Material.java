package org.cauldron.entity.components;

import java.io.Serializable;

public class Material implements Serializable {
    private static final long serialVersionUID = -4460224644902538582L;
    public float density;
    public float restitution;

    public static class TankMetal extends Material {
        float density = 1.2f;
        float restitution = 0.05f;
    }

    public static class Rock extends Material {
        float density = 0.6f;
        float restitution = 0.1f;
    }

    public static class Wood extends Material {
        float density = 0.3f;
        float restitution = 0.2f;
    }

    public static class Static extends Material {
        float density = 0f;
        float restitution = 0.4f;
    }
}
