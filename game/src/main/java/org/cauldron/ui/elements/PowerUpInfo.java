package org.cauldron.ui.elements;

import org.cauldron.entity.PowerUpType;
import org.cauldron.entity.components.AppliedPowerUp;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;

import static org.lwjgl.nuklear.Nuklear.*;

public class PowerUpInfo extends UIElement {
    public ArrayList<AppliedPowerUp> appliedPowerUps = new ArrayList<AppliedPowerUp>();

    private NkImage crate_strength = TextureHandler.getTextureNk("crate_strength");
    private NkImage crate_shield = TextureHandler.getTextureNk("crate_shield");
    private NkImage crate_cluster = TextureHandler.getTextureNk("crate_cluster");
    private NkImage crate_reverse = TextureHandler.getTextureNk("crate_reverse");
    private NkImage crate_speed = TextureHandler.getTextureNk("crate_speed");
    private NkImage crate_targeting = TextureHandler.getTextureNk("crate_targeting");
    private NkImage crate_default = TextureHandler.getTextureNk("crate");

    private NkColor white = NkColor.create().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);

    public PowerUpInfo(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
    }

    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            setUserDataUUID();
            NkRect nkRect = NkRect.mallocStack(stack);
            UIHandler.style.setStyle(ctx, true);
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, 200, 100, nkRect),
                    NK_WINDOW_NO_INPUT | NK_WINDOW_NO_SCROLLBAR
            )) {
                nk_layout_row_dynamic(ctx, 50, 3);
                for (AppliedPowerUp appliedPowerUp : appliedPowerUps) {
                    if (appliedPowerUp.powerUpType == PowerUpType.STRENGTH) {
                        nk_image(ctx, crate_strength);
                    } else if (appliedPowerUp.powerUpType == PowerUpType.SHIELD) {
                        nk_image(ctx, crate_shield);
                    } else if (appliedPowerUp.powerUpType == PowerUpType.CLUSTER) {
                        nk_image(ctx, crate_cluster);
                    } else if (appliedPowerUp.powerUpType == PowerUpType.REVERSE) {
                        nk_image(ctx, crate_reverse);
                    } else if (appliedPowerUp.powerUpType == PowerUpType.SPEED) {
                        nk_image(ctx, crate_speed);
                    } else if (appliedPowerUp.powerUpType == PowerUpType.TARGETING) {
                        nk_image(ctx, crate_targeting);
                    } else {
                        nk_image(ctx, crate_default);
                    }
                }
            }
            nk_end(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
