package org.cauldron.renderer.animations;

import org.cauldron.renderer.Animation;
import org.cauldron.renderer.TextureHandler;

public class DriveAnim extends Animation {
    public DriveAnim(String color) {
        super(125, true, false);
        textureQueue = new int[]{
                TextureHandler.getTextureID("tank_" + color),
                TextureHandler.getTextureID("tank_" + color + "_drive_1"),
                TextureHandler.getTextureID("tank_" + color + "_drive_2"),
                TextureHandler.getTextureID("tank_" + color + "_drive_3")
        };
    }
}
