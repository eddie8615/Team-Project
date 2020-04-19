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

import static org.cauldron.game.Game.SceneType.*;
import static org.lwjgl.nuklear.Nuklear.*;

public class SettingsElem extends UIElement {

    private float currentVolume = 0.5f;
    private final int uiHeight = 800;
    private final int uiWidth = 360;


    public SettingsElem(NkContext ctx, String name, int x, int y) {
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

                nk_layout_row_begin(ctx, NK_DYNAMIC, 20, 2);

                nk_layout_row_push(ctx, 0.3f);
                nk_label_colored(ctx, "   VOLUME: " + ((int) (AudioManager.getVolume() * 20)), NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_BOTTOM, NkColor.create().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF));

                nk_layout_row_push(ctx, 0.7f);
                currentVolume = AudioManager.getVolume();
                float volume = nk_slide_float(ctx, 0f, currentVolume, 1f, 0.05f);

                nk_layout_row_end(ctx);

                if (currentVolume != volume) {
                    System.out.println(volume * 100 + "%");
                    currentVolume = volume;
                    AudioManager.setVolume(currentVolume);
                }
                Settings.volume = (int) (volume * 100f);

                float vert = ctx.style().window().spacing().y();
                ctx.style().window().spacing().y(0.5f);
                nk_spacing(ctx, 1);
                ctx.style().window().spacing().y(vert);
                nk_layout_row_dynamic(ctx, 50, 1);

                if (nk_button_label(ctx, "HEALTHBARS FOLLOW PLAYERS: " + (Settings.barsFollow ? "ON" : "OFF"))) {
                    if (Settings.barsFollow) {
                        System.out.println("healthBarsFollowPlayers set to false");
                        Settings.barsFollow = false;
                    } else {
                        System.out.println("healthBarsFollowPlayers set to true");
                        Settings.barsFollow = true;
                    }
                }

                if (nk_button_label(ctx, "POWERUPS ON BARS: " + (Settings.powerUpsOnBars ? "ON" : "OFF"))) {
                    if (Settings.powerUpsOnBars) {
                        System.out.println("powerUpsOnBars set to false");
                        Settings.powerUpsOnBars = false;
                    } else {
                        System.out.println("powerUpsOnBars set to true");
                        Settings.powerUpsOnBars = true;
                    }
                }

                if (nk_button_label(ctx, "POWER INDICATOR: " + Settings.powerIndicator.toUpperCase())) {
                    if (Settings.powerIndicator.equals("both")) {
                        Settings.powerIndicator = "arrow";
                    } else if (Settings.powerIndicator.equals("arrow")) {
                        Settings.powerIndicator = "bar";
                    } else if (Settings.powerIndicator.equals("bar")) {
                        Settings.powerIndicator = "both";
                    }
                }

                if (nk_combo_begin_label(ctx, Settings.fullscreen ? "N/A" : Settings.resolution, NkVec2.mallocStack(stack).set(nk_widget_width(ctx), 400))) {
                    if (!Settings.fullscreen) {
                        nk_layout_row_dynamic(ctx, 20, 1);
                        for (String s : Settings.resOptions) {
                            if (nk_combo_item_text(ctx, s, NK_TEXT_ALIGN_CENTERED)) {
                                Settings.resolution = s;
                                String[] split = Settings.resolution.split("x");
                                Settings.resInt[0] = Integer.parseInt(split[0]);
                                Settings.resInt[1] = Integer.parseInt(split[1]);
                            }
                        }
                    }
                    nk_combo_end(ctx);
                }

                if (nk_button_label(ctx, "FULLSCREEN: " + (Settings.fullscreen ? "ON" : "OFF"))) {
                    Settings.fullscreen = !Settings.fullscreen;
                }

                if (nk_button_label(ctx, "VSYNC: " + (Settings.vsync ? "ON" : "OFF"))) {
                    Settings.vsync = !Settings.vsync;
                }

                if (nk_button_label(ctx, "FPS COUNTER: " + (Settings.fps ? "ON" : "OFF"))) {
                    Settings.fps = !Settings.fps;
                }

                nk_spacing(ctx, 1);
                if (nk_button_label(ctx, "BACK")) {
                    Settings.write();
                    if (Game.currentScene.getType() == OFFLINE || Game.currentScene.getType() == ONLINE) {
                        ctx.style().window().fixed_background().data().color().set((byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xFF);
                        UIHandler.uiProgress(name, "InGameMenu");
                    } else if (Game.currentScene.getType() == MAINMENU) {
                        UIHandler.enable("MainMenu");
                        UIHandler.enable("Title");
                        UIHandler.disable("Settings");
                        nk_window_set_focus(ctx, "MainMenu");
                    }
                }

                nk_layout_row_end(ctx);

            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }
}
