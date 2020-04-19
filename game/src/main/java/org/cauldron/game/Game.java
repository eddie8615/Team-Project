package org.cauldron.game;

import org.cauldron.audio.AudioManager;
import org.cauldron.network.Client;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.layers.GUILayer;
import org.cauldron.scenes.*;
import org.cauldron.ui.UIHandler;
import org.joml.Vector2d;

import java.util.ArrayList;

import static org.cauldron.network.LobbySettings.GameType;
import static org.cauldron.network.LobbySettings.GameType.DEFAULT;

/**
 * The root Game, which switches Scenes and handles framerate ticks, as well as one-time initialisations
 */
public class Game {

    public enum SceneType {
        MAINMENU, OFFLINE, ONLINE, MAPEDITOR
    }

    private static GUILayer guiLayer;
    public static Scene currentScene;
    public static boolean changeSceneFlag = false;
    public static SceneType destScene = null;
    private static GameType requestedGameType = DEFAULT;
    private static ArrayList<Vector2d> requestedMapCntrlPoints = null;
    private static long fpsTick = 1000000000; // One sec between fps calc
    public static long startFPSTime;
    private static int frameCount = 0;
    public static int fps = 0;

    /**
     * Process all one-time setup
     *
     * @param window id of the window the game is running in
     */
    public static void init(long window) {
        TextureHandler.loadTextures("game/textures/list");
        TextureHandler.loadTextures("ui/textures/list");

        AudioManager.init();
        AudioManager.loadAudioFiles("game/audioFiles/list");
        UIHandler.init(window);
        Input.init(window);
        MapLoader.init();
        guiLayer = new GUILayer();
        guiLayer.setup();
        Input.setGUILayer(guiLayer);
        UIHandler.setGUILayer(guiLayer);
        currentScene = new MainMenuScene(guiLayer);
    }

    /**
     * Cycle all scenes; process scene changes and framerate calculation
     */
    public static void cycle() {
        long currentNanoTime = System.nanoTime();
        if (fpsTick < currentNanoTime - startFPSTime) {
            fps = frameCount;
            frameCount = 0;
            startFPSTime = currentNanoTime;
        }

        currentScene.cycle();
        if (changeSceneFlag) {
            Game.setScene(destScene);
            changeSceneFlag = false;
            destScene = null;
        }
        frameCount++;
    }

    public static void cleanup() {
        guiLayer.close();
        TextureHandler.cleanup();
        AudioManager.cleanup();
    }

    /**
     * Switches to a Scene of the specified type
     *
     * @param type
     */
    public static void setScene(SceneType type) {
        Scene oldScene = currentScene;
        switch (type) {
            case MAINMENU:
                currentScene = new MainMenuScene(guiLayer);
                break;
            case OFFLINE:
                currentScene = new OfflineScene(guiLayer);
                break;
            case ONLINE:
                currentScene = new OnlineScene(guiLayer);
                break;
            case MAPEDITOR:
                currentScene = new MapEditingScene(guiLayer);
                break;
        }
        oldScene.cleanup();
    }

    public static void setRequestedGameType(GameType gametype) {
        requestedGameType = gametype;
    }

    public static GameType getRequestedGameType() {
        return requestedGameType;
    }

    public static void setRequestedMapCntrlPoints(ArrayList<Vector2d> mapCntrlPoints) {
        requestedMapCntrlPoints = mapCntrlPoints;
    }

    public static ArrayList<Vector2d> getRequestedMapCntrlPoints() {
        return requestedMapCntrlPoints;
    }

    public static GUILayer getGuiLayer(){
        return guiLayer;
    }
}
