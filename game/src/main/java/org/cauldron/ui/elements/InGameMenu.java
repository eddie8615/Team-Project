package org.cauldron.ui.elements;

import org.cauldron.game.Game;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.scenes.OfflineScene;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkCommandBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import static org.cauldron.game.Game.SceneType.*;
import static org.lwjgl.nuklear.Nuklear.*;

public class InGameMenu extends UIElement {
    private final int uiWidth = 360;
    private final int uiHeight = 400;
    private final int buttonHeight = 50;

    public InGameMenu(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
    }

    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            setUserDataUUID();
            NkRect rect = NkRect.create();
            UIHandler.style.setStyle(ctx, true);
            if (nk_begin(ctx, name, nk_rect(x, y, uiWidth, uiHeight, rect),
                    NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR
            )) {

                ctx.style().window().background().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
                ctx.style().window().fixed_background().data().color().set((byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x00);

                nk_layout_row_static(ctx, 90, 100, 1);
                NkRect picRect = NkRect.create();
                NkCommandBuffer cmd = nk_window_get_canvas(ctx);
                nk_draw_image(cmd, nk_rect(x, y, 360, 60, picRect), TextureHandler.getTextureNk("paused"), NkColor.create().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF));

                nk_layout_row_dynamic(ctx, buttonHeight, 1);

                if (nk_button_label(ctx, "SETTINGS")) {
                    if (Game.currentScene.getType() == OFFLINE || Game.currentScene.getType() == ONLINE) {
                        UIHandler.uiProgress(name, "Settings");
                    } else {
                        UIHandler.disable(name);
                        UIHandler.enable("Settings");
                    }
                }

                if (nk_button_label(ctx, "RETURN TO MATCH")) {
                    UIHandler.disable(name);
                    if (Game.currentScene.getType() == OFFLINE) {
                        OfflineScene offline = (OfflineScene) Game.currentScene;
                        offline.paused = false;
                    }
                }

                if (nk_button_label(ctx, "QUIT MATCH")) {
                    Game.changeSceneFlag = true;
                    Game.destScene = MAINMENU;
                }
            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }

}

