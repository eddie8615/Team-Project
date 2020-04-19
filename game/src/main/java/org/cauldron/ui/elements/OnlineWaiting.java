package org.cauldron.ui.elements;

import org.cauldron.audio.AudioManager;
import org.cauldron.game.Game;
import org.cauldron.game.Settings;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import static org.cauldron.game.Game.SceneType.*;
import static org.lwjgl.nuklear.Nuklear.*;

public class OnlineWaiting extends UIElement {

    private final int uiHeight = 800;
    private final int uiWidth = 360;

    private String externalIP = "";
    private String localIP = "";

    public OnlineWaiting(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
        URL whatismyip = null;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            externalIP = in.readLine(); //you get the IP as a String
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                nk_layout_row_dynamic(ctx, 50, 1);
                nk_label_colored(ctx, "WAITING FOR MATCH TO START...", NK_TEXT_ALIGN_CENTERED, NkColor.create().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF));
                nk_label_colored(ctx, "YOUR EXTERNAL IP ADDRESS IS " + externalIP, NK_TEXT_ALIGN_CENTERED, NkColor.create().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF));
                nk_layout_row_dynamic(ctx, 15, 1);
                nk_label_colored(ctx, "IF YOU'D LIKE TO PLAY ON LAN, USE YOUR LOCAL IP", NK_TEXT_ALIGN_CENTERED, NkColor.create().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF));
                nk_label_colored(ctx, "TO QUIT, PRESS ESCAPE", NK_TEXT_ALIGN_CENTERED, NkColor.create().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF));
            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }
}
