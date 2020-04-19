package org.cauldron.renderer.animations;

import org.cauldron.renderer.Animation;
import org.cauldron.renderer.TextureHandler;

public class LoadShellAnim extends Animation {
    public LoadShellAnim(String color) {
        super(75, false, true);
        textureQueue = new int[]{
                TextureHandler.getTextureID("turret_" + color),
                TextureHandler.getTextureID("turret_" + color + "_shoot_1"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_2"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_3"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_4"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_5"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_6"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_7"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_8"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_9"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_10"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_11"),
                TextureHandler.getTextureID("turret_" + color + "_shoot_12")
        };
    }
}
