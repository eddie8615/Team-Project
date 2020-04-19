package org.cauldron.renderer.animations;

import org.cauldron.renderer.Animation;
import org.cauldron.renderer.TextureHandler;

public class ShootShellAnim extends Animation {
    public ShootShellAnim(String color) {
        super(200, false, false);
        textureQueue = new int[]{
                TextureHandler.getTextureID("turret_" + color + "_shoot_end"),
                TextureHandler.getTextureID("turret_" + color)
        };
    }
}
