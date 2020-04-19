package org.cauldron.renderer.animations;

import org.cauldron.renderer.Animation;
import org.cauldron.renderer.TextureHandler;

public class RandomCrateAnim extends Animation {
    public RandomCrateAnim() {
        super(125, true, false);
        textureQueue = new int[]{
                TextureHandler.getTextureID("crate_question"),
                TextureHandler.getTextureID("crate_question_bold")
        };
    }
}
