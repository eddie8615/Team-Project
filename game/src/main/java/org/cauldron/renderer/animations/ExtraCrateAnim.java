package org.cauldron.renderer.animations;

import org.cauldron.renderer.Animation;
import org.cauldron.renderer.TextureHandler;

public class ExtraCrateAnim extends Animation {
    public ExtraCrateAnim() {
        super(125, true, false);
        textureQueue = new int[]{
                TextureHandler.getTextureID("crate_extra_a"),
                TextureHandler.getTextureID("crate_extra_b"),
                TextureHandler.getTextureID("crate_extra_c"),
                TextureHandler.getTextureID("crate_extra_d"),
                TextureHandler.getTextureID("crate_extra_e"),
                TextureHandler.getTextureID("crate_extra_f"),
                TextureHandler.getTextureID("crate_extra_g"),
                TextureHandler.getTextureID("crate_extra_h"),
                TextureHandler.getTextureID("crate_extra_i"),
                TextureHandler.getTextureID("crate_extra_j"),
                TextureHandler.getTextureID("crate_extra_k"),
                TextureHandler.getTextureID("crate_extra_l")
        };
    }
}
