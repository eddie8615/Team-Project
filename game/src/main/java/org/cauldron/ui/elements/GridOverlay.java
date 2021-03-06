package org.cauldron.ui.elements;

import org.cauldron.renderer.TextureHandler;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public class GridOverlay extends UIElement {
    private final int width = 1600;
    private final int height = 900;
    private NkImage logo = TextureHandler.getTextureNk("grid");
    private NkColor white = NkColor.create().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);

    public GridOverlay(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
    }

    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            setUserDataUUID();
            NkRect nkRect = NkRect.mallocStack(stack);
            UIHandler.style.setStyle(ctx, true);
            if (nk_begin(ctx, name, nk_rect(x, y, width, height, nkRect), NK_WINDOW_NO_SCROLLBAR)) {
                setUserDataUUID();
                NkCommandBuffer cmd = nk_window_get_canvas(ctx);
                nk_draw_image(cmd, nkRect, logo, white);
            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }
}
