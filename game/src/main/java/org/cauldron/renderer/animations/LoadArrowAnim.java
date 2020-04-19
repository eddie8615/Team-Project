package org.cauldron.renderer.animations;

import org.cauldron.renderer.Animation;
import org.cauldron.renderer.TextureHandler;

public class LoadArrowAnim extends Animation {
    public LoadArrowAnim() {
        super(75, false, true);
        textureQueue = new int[]{
                TextureHandler.getTextureID("arrow_off"),
                TextureHandler.getTextureID("arrow_1"),
                TextureHandler.getTextureID("arrow_2"),
                TextureHandler.getTextureID("arrow_3"),
                TextureHandler.getTextureID("arrow_4"),
                TextureHandler.getTextureID("arrow_5"),
                TextureHandler.getTextureID("arrow_6"),
                TextureHandler.getTextureID("arrow_7"),
                TextureHandler.getTextureID("arrow_8"),
                TextureHandler.getTextureID("arrow_9"),
                TextureHandler.getTextureID("arrow_10"),
                TextureHandler.getTextureID("arrow_11"),
                TextureHandler.getTextureID("arrow_12")
        };
    }
}
