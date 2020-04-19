package org.cauldron.ui.elements;

import org.cauldron.game.Game;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.cauldron.ui.styles.DefaultStyle;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import static org.cauldron.game.Game.SceneType.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;


public class MainMenu extends UIElement {

    private final int uiWidth = 360;
    private final int uiHeight = 1000;
    private final int buttonHeight = 50;
    private long window;

    public MainMenu(NkContext ctx, String name, int x, int y, long window) {
        super(ctx, name, x, y);
        this.window = window;
    }

    @Override
    public void layout() {
        try (MemoryStack stack = stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);
            new DefaultStyle().setStyle(ctx, true);

            setUserDataUUID();
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, uiWidth, uiHeight, rect),
                    NK_WINDOW_NO_SCROLLBAR
                    //windowOptions
            )) {
                nk_layout_row_dynamic(ctx, buttonHeight, 1);

                if (nk_button_label(ctx, "SINGLEPLAYER")) {
                    ((MapSelector) UIHandler.getUIElements().get("MapSelector")).destination = OFFLINE;
                    UIHandler.enable("MapSelector");
                    UIHandler.disable("MainMenu");
                    UIHandler.disable("Title");
                    ((MapSelector) UIHandler.getUIElements().get("MapSelector")).destination = OFFLINE;
                }

                if (nk_button_label(ctx, "MULTIPLAYER")) {
                    UIHandler.enable("Login"); // Normal
//                    UIHandler.enable("LobbySystem"); // Without DB
                    UIHandler.disable("MainMenu");
                    UIHandler.disable("Title");
                    ((MapSelector) UIHandler.getUIElements().get("MapSelector")).destination = ONLINE;
                }

                if (nk_button_label(ctx, "MAP EDITOR")) {
                    ((MapSelector) UIHandler.getUIElements().get("MapSelector")).destination = MAPEDITOR;
                    UIHandler.disable("Title");
                    UIHandler.disable("MainMenu");
                    UIHandler.enable("MapSelector");
                }

                if (nk_button_label(ctx, "SETTINGS")) {
                    UIHandler.disable("Title");
                    UIHandler.disable("MainMenu");
                    UIHandler.enable("Settings");
                }

                if (nk_button_label(ctx, "HELP")) {
                    UIHandler.disable("Title");
                    UIHandler.disable("MainMenu");
                    UIHandler.enable("HelpScreen");
                }

                nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);

                if (nk_button_label(ctx, "EXIT")) {
                    glfwSetWindowShouldClose(window, true);
                }

                nk_spacing(ctx, 1);
            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }

}

