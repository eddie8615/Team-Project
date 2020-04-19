package org.cauldron.ui.elements;

import org.cauldron.game.Game;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.styles.DefaultStyle;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public class CountdownTimer extends UIElement {

    public long timeRemaining = 0;

    public CountdownTimer(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
        scale = 1.5f;
    }

    @Override
    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);

            new DefaultStyle().setStyle(ctx, true);
            // Ensure that GUILayer knows how to identify the element giving the draw command
            setUserDataUUID();
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, 100, 200, rect),
                    NK_WINDOW_NO_INPUT
            )) {
                nk_layout_row_static(ctx, 8, 24, 1);
                nk_label(ctx, formatTimeLeft(timeRemaining), NK_TEXT_ALIGN_CENTERED);
            }

            resetUserDataUUID();
            nk_end(ctx);
        }
    }

    /**
     * @param timeRemaining in milliseconds
     * @return string in format mm:ss
     */
    private String formatTimeLeft(long timeRemaining) {
        int seconds = (int) (timeRemaining / 1000) % 60;
        int minutes = (int) (timeRemaining / 1000 / 60);
        return minutes + ":" + String.format("%02d", seconds);
    }
}
