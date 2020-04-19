package org.cauldron.ui.elements;

import org.cauldron.entity.PowerUpType;
import org.cauldron.entity.components.AppliedPowerUp;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.styles.DefaultStyle;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkImage;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.nuklear.Nuklear.*;

public class Bars extends UIElement {
    public BigInteger health;
    public BigInteger power;
    public ArrayList<AppliedPowerUp> appliedPowerUps = new ArrayList<AppliedPowerUp>();
    public int lives;

    private NkColor white = NkColor.create().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);

    private ByteBuffer buffer = BufferUtils.createByteBuffer(120);
    private PointerBuffer cur = PointerBuffer.create(buffer);

    public boolean livesEnabled = false;
    public boolean healthEnabled;
    public boolean powerEnabled;
    public boolean powerUpsEnabled = true;

    private NkImage crate_strength = TextureHandler.getTextureNk("crate_strength");
    private NkImage crate_shield = TextureHandler.getTextureNk("crate_shield");
    private NkImage crate_cluster = TextureHandler.getTextureNk("crate_cluster");
    private NkImage crate_reverse = TextureHandler.getTextureNk("crate_reverse");
    private NkImage crate_speed = TextureHandler.getTextureNk("crate_speed");
    private NkImage crate_targeting = TextureHandler.getTextureNk("crate_targeting");
    private NkImage crate_default = TextureHandler.getTextureNk("crate");

    private NkImage heart = TextureHandler.getTextureNk("heart");
    private NkImage heart_black = TextureHandler.getTextureNk("heart_black");

    public Bars(NkContext ctx, String name, int x, int y, boolean healthEnabled, boolean powerEnabled) {
        super(ctx, name, x, y);
        health = BigInteger.valueOf(80);
        power = BigInteger.valueOf(80);
        this.healthEnabled = healthEnabled;
        this.powerEnabled = powerEnabled;
    }

    @Override
    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);

            ctx.style().window().fixed_background().data().color().a((byte) 0x00);

            // Ensure that GUILayer knows how to identify the element giving the draw command
            setUserDataUUID();
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, 230, 250, rect),
                    NK_WINDOW_NO_INPUT
            )) {
                if (livesEnabled)
                    layoutLives();
                if (healthEnabled)
                    layoutHealth();
                if (powerEnabled)
                    layoutPower();
                if (powerUpsEnabled)
                    layoutPowerUps();
            }

            resetUserDataUUID();
            nk_end(ctx);
            new DefaultStyle().setStyle(ctx, true);
        }
    }

    private void layoutLives() {
        nk_layout_row_static(ctx, 20, 22, 4);
        for (int i = 0; i < lives; i++) {
            nk_image(ctx, heart);
        }
        for (int i = 0; i < 8 - lives; i++) {
            nk_image(ctx, heart_black);
        }
    }

    private void layoutPower() {
        ctx.style().progress().rounding(3f);
        ctx.style().progress().normal().data().color().set((byte) 0x66, (byte) 0xCC, (byte) 0xFF, (byte) 0x66);
        ctx.style().progress().cursor_normal().data().color().set((byte) 0x66, (byte) 0xCC, (byte) 0xFF, (byte) 0xFF);
        ctx.style().progress().border(1f);
        nk_layout_row_static(ctx, 8, 100, 1);
        buffer.put(0, power.byteValueExact());
        nk_progress(ctx, cur, 100, false);
    }

    private void layoutHealth() {
        ctx.style().progress().rounding(3f);
        ctx.style().progress().normal().data().color().set((byte) 0x33, (byte) 0xCC, (byte) 0x33, (byte) 0x66);
        ctx.style().progress().cursor_normal().data().color().set((byte) 0x33, (byte) 0xCC, (byte) 0x33, (byte) 0xFF);
        ctx.style().progress().border(1f);
        nk_layout_row_static(ctx, 8, 100, 1);
        buffer.put(0, health.byteValueExact());
        nk_progress(ctx, cur, 100, false);
    }

    private void layoutPowerUps() {
        nk_layout_row_static(ctx, 20, 22, 4);
        // used to alternate sides
        boolean left = true;
        for (AppliedPowerUp appliedPowerUp : appliedPowerUps) {
            layoutPowerUp(appliedPowerUp, left);
            left = !left;
        }
    }

    private void layoutPowerUp(AppliedPowerUp appliedPowerUp, boolean left) {
        if (left) {
            nk_label_colored(ctx, appliedPowerUp.timeLeft, NK_TEXT_ALIGN_CENTERED, white);
        }
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
        if (!left) {
            nk_label_colored(ctx, appliedPowerUp.timeLeft, NK_TEXT_ALIGN_CENTERED, white);
        }
    }
}
