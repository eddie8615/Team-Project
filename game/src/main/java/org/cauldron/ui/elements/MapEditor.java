package org.cauldron.ui.elements;

import org.cauldron.audio.AudioManager;
import org.cauldron.game.Game;
import org.cauldron.game.MapLoader;
import org.cauldron.game.Settings;
import org.cauldron.game.Terrain;
import org.cauldron.renderer.layers.Layer;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.joml.Vector2d;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.cauldron.game.Game.SceneType.*;
import static org.cauldron.network.LobbySettings.GameType.DEFAULT;
import static org.lwjgl.nuklear.Nuklear.*;

public class MapEditor extends UIElement {

    private final int uiHeight = 800;
    private final int uiWidth = 360;
    private ArrayList<Vector2d> controlPoints = new ArrayList<Vector2d>();
    private int pointsCount = 2;
    public Terrain terrain;
    private TextField mapName = new TextField(15, false);

    private int editOptions = NK_EDIT_FIELD | NK_EDIT_ALWAYS_INSERT_MODE | NK_EDIT_AUTO_SELECT
            | NK_EDIT_SELECTABLE | NK_EDIT_CLIPBOARD;

    public MapEditor(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
        controlPoints.add(new Vector2d(0, 200));
        controlPoints.add(new Vector2d(1600, 200));
    }

    public void reset() {
        mapName = new TextField(15, false);
        controlPoints = new ArrayList<Vector2d>();
        controlPoints.add(new Vector2d(0, 200));
        controlPoints.add(new Vector2d(1600, 200));
        pointsCount = 2;
    }

    public void setMap(Terrain map) {
        controlPoints = map.controlPoints;
        pointsCount = map.controlPoints.size();
        terrain = map;
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
                nk_edit_string(ctx,
                        editOptions,
                        mapName.getContent(),
                        mapName.getLength(),
                        mapName.maxLength(),
                        mapName.getPluginFilter());

                for (int i = 0; i < pointsCount; i++) {
                    layoutSliders(i);
                }

                nk_layout_row_dynamic(ctx, 50, 1);

                if (terrain != null && pointsCount < 15 && nk_button_label(ctx, "ADD CONTROL POINT")) {
                    pointsCount++;
                    int increment = Layer.WIDTH / (pointsCount - 1);
                    controlPoints = new ArrayList<>();
                    controlPoints.add(new Vector2d(0, 200));
                    controlPoints.add(new Vector2d(Layer.WIDTH, 200));
                    for (int i = 0; i < pointsCount - 2; i++)
                        controlPoints.add(controlPoints.size() - 1, new Vector2d(
                                controlPoints.get(controlPoints.size() - 2).x + increment,
                                terrain.getYVal((float) controlPoints.get(controlPoints.size() - 2).x)));
                }

                if (nk_button_label(ctx, "SAVE MAP AND BACK")) {
                    MapLoader.save(mapName.getValue(), controlPoints);
                    Game.changeSceneFlag = true;
                    Game.destScene = MAINMENU;
                }

                if (nk_button_label(ctx, "BACK")) {
                    Game.changeSceneFlag = true;
                    Game.destScene = MAINMENU;
                }

                nk_layout_row_end(ctx);

            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }

    private void layoutSliders(int i) {
        if (terrain == null)
            return;

        nk_layout_row_begin(ctx, NK_DYNAMIC, 20, 2);
        nk_layout_row_push(ctx, 0.3f);
        double x = controlPoints.get(i).x;

        nk_label(ctx, String.valueOf(x), NK_TEXT_ALIGN_CENTERED);

        nk_layout_row_push(ctx, 0.7f);
        float y = nk_slide_float(ctx, 0f, (float) controlPoints.get(i).y, Layer.HEIGHT - 100, 25f);
        controlPoints.set(i, new Vector2d(x, y));

        nk_layout_row_end(ctx);
        nk_layout_row_dynamic(ctx, 5, 1);
        nk_spacing(ctx, 1);
    }

    public void updateMap() {
        if (terrain != null)
            terrain.loadMap(new ArrayList<Vector2d>(controlPoints));
    }
}
