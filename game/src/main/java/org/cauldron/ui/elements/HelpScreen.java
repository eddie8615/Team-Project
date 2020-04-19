package org.cauldron.ui.elements;

import org.cauldron.audio.AudioManager;
import org.cauldron.game.Game;
import org.cauldron.game.Settings;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.cauldron.game.Game.SceneType.*;
import static org.lwjgl.nuklear.Nuklear.*;

public class HelpScreen extends UIElement {

    private float currentVolume = 0.5f;
    private final int uiHeight = 900;
    private final int uiWidth = 1600;

    private NkImage help_screen = TextureHandler.getTextureNk("help_screen_clear");

    public HelpScreen(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
    }

    @Override
    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);
            ctx.style().window().fixed_background().data().color().set((byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x00);
            setUserDataUUID();
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, uiWidth, uiHeight, rect),
                    NK_WINDOW_NO_SCROLLBAR
                    //windowOptions
            )) {

                nk_layout_row_static(ctx, 900, 1600, 1);

                nk_image(ctx, help_screen);

                nk_layout_row_end(ctx);

            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }
}
