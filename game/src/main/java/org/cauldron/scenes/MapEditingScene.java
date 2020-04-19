package org.cauldron.scenes;

import org.cauldron.audio.AudioManager;
import org.cauldron.game.*;
import org.cauldron.renderer.Backdrop;
import org.cauldron.renderer.BackdropScroller;
import org.cauldron.renderer.Renderer;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.layers.BackdropLayer;
import org.cauldron.renderer.layers.GUILayer;
import org.cauldron.renderer.layers.TerrainLayer;
import org.cauldron.ui.UIHandler;
import org.cauldron.ui.elements.MapEditor;
import org.cauldron.ui.elements.MapSelector;

import static org.lwjgl.nuklear.Nuklear.nk_window_set_focus;

public class MapEditingScene extends Scene {
    TerrainLayer terrainLayer = new TerrainLayer();
    BackdropLayer backdropLayer = new BackdropLayer();
    public Terrain map;
    BackdropScroller bScroll = null;
    long mapLastUpdated = System.currentTimeMillis();
    long mapUpdateInterval = 500;

    public MapEditingScene(GUILayer guiLayer) {
        // Clean the animations from a previous scene
        TextureHandler.wipeAnimations();
        super.guiLayer = guiLayer;
        map = new Terrain(25);
        MapLoader.init();
        MapLoader.load(map, ((MapSelector) UIHandler.getUIElements().get("MapSelector")).chosenMap);
        this.setType(Game.SceneType.MAPEDITOR);

        // Initialise AudioManager
        AudioManager.setSources(new String[]{});

        // Initialise the layers
        terrainLayer.setTerrain(map);
        terrainLayer.setup();
        backdropLayer.setup();
        backdropLayer.setBackdrops(new Backdrop[]{new Backdrop(TextureHandler.getTextureID("clouds_fg")),
                new Backdrop(TextureHandler.getTextureID("clouds_bg")),
                new Backdrop(TextureHandler.getTextureID("sky"))});
        bScroll = new BackdropScroller(backdropLayer, 1, 200);

        // Enable various necessary UI elements for this Scene
        UIHandler.disableAll();
        UIHandler.enable("GridOverlay");
        UIHandler.enable("MapEditor");
        ((MapEditor) UIHandler.getUIElements().get("MapEditor")).reset();
        ((MapEditor) UIHandler.getUIElements().get("MapEditor")).setMap(map);

        Renderer.clear();
    }

    @Override
    public void cycle() {
        nk_window_set_focus(guiLayer.getContext(), "MapEditor");
        // Update the map on tick
        if (System.currentTimeMillis() - mapLastUpdated > mapUpdateInterval) {
            ((MapEditor) UIHandler.getUIElements().get("MapEditor")).updateMap();
            terrainLayer.update();
            mapLastUpdated = System.currentTimeMillis();
        }
        if (Settings.fps)
            UIHandler.enable("FPSCounter");
        else
            UIHandler.disable("FPSCounter");

        // Polling input and clearing the framebuffer
        Input.cycle();
        Renderer.clear();

        // Rendering calls
        Renderer.draw(backdropLayer);
        Renderer.draw(terrainLayer);
        Renderer.draw(guiLayer);

        // Backdrop animation
        bScroll.cycle();
    }

    @Override
    void processInputs() {
    }

    @Override
    public void cleanup() {
        terrainLayer.close();
        backdropLayer.close();
    }
}
