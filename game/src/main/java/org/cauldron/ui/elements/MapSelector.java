package org.cauldron.ui.elements;

import org.cauldron.audio.AudioManager;
import org.cauldron.game.Game;
import org.cauldron.game.MapLoader;
import org.cauldron.game.Settings;
import org.cauldron.game.Terrain;
import org.cauldron.network.LobbySettings;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import static org.cauldron.game.Game.SceneType.*;
import static org.lwjgl.nuklear.Nuklear.*;

public class MapSelector extends UIElement {

    private float currentVolume = 0.5f;
    private final int uiHeight = 800;
    private final int uiWidth = 360;
    public String chosenMap = "Default";
    public Game.SceneType destination = MAPEDITOR;

    public boolean gameModeTimed = true;

    public MapSelector(NkContext ctx, String name, int x, int y) {
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

                if (destination == OFFLINE) {
                    layoutGameOptions();
                }

                nk_layout_row_dynamic(ctx, 50, 1);
                if (nk_combo_begin_label(ctx, chosenMap, NkVec2.mallocStack(stack).set(nk_widget_width(ctx), 400))) {
                    if (isEnabled()) {
                        nk_layout_row_dynamic(ctx, 20, 1);
                        for (String s : MapLoader.list()) {
                            if (nk_combo_item_text(ctx, s, NK_TEXT_ALIGN_CENTERED)) {
                                chosenMap = s;
                            }
                        }
                    }
                    nk_combo_end(ctx);
                }

                if (destination == OFFLINE || destination == ONLINE) {
                    if (nk_button_label(ctx, "SELECT")) {
                        if (destination == OFFLINE) {
                            Game.changeSceneFlag = true;
                            Game.destScene = OFFLINE;
                        } else {
                            Terrain map = new Terrain(25);
                            MapLoader.load(map, ((MapSelector) UIHandler.getUIElements().get("MapSelector")).chosenMap);
                            Game.setRequestedMapCntrlPoints(map.controlPoints);
                            Game.changeSceneFlag = true;
                            Game.destScene = ONLINE;
                        }
                    }
                }

                if (destination == MAPEDITOR) {
                    if (nk_button_label(ctx, "EDIT")) {
                        if (!chosenMap.equals("Default")) {
                            Game.changeSceneFlag = true;
                            Game.destScene = MAPEDITOR;
                        }
                    }

                    if (nk_button_label(ctx, "NEW")) {
                        chosenMap = "Default";
                        Game.changeSceneFlag = true;
                        Game.destScene = MAPEDITOR;
                    }

                    if (nk_button_label(ctx, "DELETE")) {
                        if (!chosenMap.equals("Default")) {
                            MapLoader.delete(chosenMap);
                        }
                        chosenMap = "Default";
                    }
                }

                if (nk_button_label(ctx, "BACK")) {
                    if (destination == ONLINE) {
                        UIHandler.disable("MapSelector");
                        UIHandler.enable("LobbySystem");
                    } else {
                        UIHandler.enable("MainMenu");
                        UIHandler.enable("Title");
                        UIHandler.disable("MapSelector");
                        nk_window_set_focus(ctx, "MainMenu");
                    }
                }

                nk_layout_row_end(ctx);

            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }

    private void layoutGameOptions() {
        nk_layout_row_dynamic(ctx, 50, 1);
        if (gameModeTimed) {
            if (nk_button_label(ctx, "GAMEMODE: TIME LIMIT")) {
                gameModeTimed = false;
                Game.setRequestedGameType(LobbySettings.GameType.LIVES);
            }
        } else {
            if (nk_button_label(ctx, "GAMEMODE: LIVES")) {
                gameModeTimed = true;
                Game.setRequestedGameType(LobbySettings.GameType.DEFAULT);
            }
        }
        nk_layout_row_dynamic(ctx, 20, 1);
        nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);
    }
}
