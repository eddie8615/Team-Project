package org.cauldron.ui.elements;

import org.cauldron.game.Game;
import org.cauldron.server.Server;
import org.cauldron.game.MapLoader;
import org.cauldron.game.Terrain;
import org.cauldron.network.Client;
import org.cauldron.scenes.Scene;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;

import static org.cauldron.game.Game.SceneType.ONLINE;
import static org.cauldron.network.LobbySettings.GameType.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class LobbySystem extends UIElement {

    private final int uiWidth = 360;
    private final int uiHeight = 430;
    private final int buttonHeight = 50;
    public TextField ipaddress;
    public TextField username;

    public boolean isHost = false;
    public boolean isChosen = false;
    private int editOptions = NK_EDIT_FIELD | NK_EDIT_ALWAYS_INSERT_MODE | NK_EDIT_AUTO_SELECT
            | NK_EDIT_SELECTABLE | NK_EDIT_CLIPBOARD;

    public LobbySystem(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
        ipaddress = new TextField(15, false);
        username = new TextField(15, false);
    }

    @Override
    public void layout() {
        try (MemoryStack stack = stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);

            setUserDataUUID();
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, uiWidth, uiHeight, rect),
                    NK_WINDOW_NO_SCROLLBAR
            )) {
                if (!isChosen) {
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_label(ctx, "SERVER ADDRESS (LEAVE BLANK IF HOSTING):", NK_TEXT_ALIGN_BOTTOM | NK_TEXT_ALIGN_LEFT);
                    nk_layout_row_dynamic(ctx, buttonHeight, 1);
                    nk_edit_string(ctx,
                            editOptions,
                            ipaddress.getContent(),
                            ipaddress.getLength(),
                            ipaddress.maxLength(),
                            ipaddress.getPluginFilter());

                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_label(ctx, "USERNAME:", NK_TEXT_ALIGN_BOTTOM | NK_TEXT_ALIGN_LEFT);
                    nk_layout_row_dynamic(ctx, buttonHeight, 1);
                    nk_edit_string(ctx,
                            editOptions,
                            username.getContent(),
                            username.getLength(),
                            username.maxLength(),
                            username.getPluginFilter());

                    if (nk_button_label(ctx, "HOST GAME")) {
                        isHost = true;
                        isChosen = true;
//                        Client.address = ipaddress.getValue();
                        Client.address = "localhost";
                        Client.isHost = true;
                    }

                    if (nk_button_label(ctx, "JOIN GAME")) {
                        Client.address = ipaddress.getValue();
                        UIHandler.username = username.getValue();
                        Game.changeSceneFlag = true;
                        Game.destScene = ONLINE;
                        isHost = false;
                        isChosen = false;
                    }
                }
                if (isHost && isChosen) {
                    nk_layout_row_dynamic(ctx, buttonHeight, 1);
                    //Classic is equal to default
                    if (nk_button_label(ctx, "PLAY CLASSIC")) {
                        Game.setRequestedGameType(DEFAULT);
                        isChosen = false;
                        isHost = false;
                        UIHandler.enable("MapSelector");
                        UIHandler.disable("LobbySystem");
                        UIHandler.username = username.getValue();
                    }

                    if (nk_button_label(ctx, "PLAY TEAMS")) {
                        Game.setRequestedGameType(TEAM);
                        isChosen = false;
                        isHost = false;
                        UIHandler.enable("MapSelector");
                        UIHandler.disable("LobbySystem");
                        UIHandler.username = username.getValue();
                    }

                    if (nk_button_label(ctx, "PLAY BOSS")) {
                        Game.setRequestedGameType(BOSS);
                        isChosen = false;
                        isHost = false;
                        UIHandler.enable("MapSelector");
                        UIHandler.disable("LobbySystem");
                        UIHandler.username = username.getValue();
                    }

                }

                if (nk_button_label(ctx, "BACK")) {
                    if (!isChosen) {
                        Scene.userInfo = null;
                        UIHandler.enable("MainMenu");
                        UIHandler.enable("Title");
                        UIHandler.disable("LobbySystem");
                    }

                    isHost = false;
                    isChosen = false;
                }

                nk_end(ctx);
                resetUserDataUUID();
            }
        }
    }
}
