package org.cauldron.renderer.animations;

import org.cauldron.renderer.Animation;
import org.cauldron.renderer.TextureHandler;

public class ShootArrowAnim extends Animation {
    public ShootArrowAnim() {
        super(200, false, false);
        textureQueue = new int[]{
                TextureHandler.getTextureID("arrow_end"),
                TextureHandler.getTextureID("arrow_off")
        };
    }
}
