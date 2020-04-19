package org.cauldron.ui.elements;

import org.cauldron.ui.UIElement;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public class NameTag extends UIElement {
    String userName = null;
    private NkColor white = NkColor.create().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
    private NkColor green = NkColor.create().set((byte) 0x00, (byte) 0x99, (byte) 0x11, (byte) 0xFF);

    public NameTag(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);

            NkColor oldBG = ctx.style().window().background();

            NkColor newBG = NkColor.create().set(oldBG);
            newBG.a((byte) 0x00);
            ctx.style().window().fixed_background().data().color().set(newBG);
//            nk_style_item_hide(ctx.style().window().fixed_background());

            setUserDataUUID();
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, 230, 25, rect),
                    NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_NO_INPUT
            )) {

                nk_layout_row_static(ctx, 8, 100, 1);
                if (userName == null)
                    nk_label_colored(ctx, "YOU", NK_TEXT_ALIGN_CENTERED, green);
                else
                    nk_label_colored(ctx, userName, NK_TEXT_ALIGN_CENTERED, white);
            }
            resetUserDataUUID();
            nk_end(ctx);

            ctx.style().window().fixed_background().data().color().set(oldBG);
        }
    }

}
