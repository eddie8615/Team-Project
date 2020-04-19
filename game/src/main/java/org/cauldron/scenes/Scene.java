package org.cauldron.scenes;

import org.cauldron.ai.BasicAi;
import org.cauldron.database.Database;
import org.cauldron.game.Game.SceneType;
import org.cauldron.network.LobbySettings;
import org.cauldron.renderer.layers.BackdropLayer;
import org.cauldron.renderer.layers.GUILayer;
import org.cauldron.renderer.layers.ModelLayer;
import org.cauldron.renderer.layers.TerrainLayer;

import java.util.ArrayList;

/**
 * Scenes hold the setup and running instructions for different distinct areas, or 'scenes', in the game
 */
public abstract class Scene {
    private SceneType type;
    public LobbySettings lobbySettings;
    public long timeRemaining;
    long lastUpdated = System.currentTimeMillis();
    long lastCrateDropped = System.currentTimeMillis();
    long aiUpdate = System.currentTimeMillis();
    public static Database userInfo = null;
    GUILayer guiLayer = new GUILayer();
    public String tankID;
    public String tankAiID;
    public boolean gameOver = false;

    public ArrayList<ArrayList<String>> teamsList = new ArrayList<>();
    public int[] scores = {0, 0};

    BasicAi basicAi = new BasicAi();

    /**
     * Set the type of Scene this is, so Game can easily identify it
     *
     * @param sceneType
     */
    public void setType(SceneType sceneType) {
        this.type = sceneType;
    }

    /**
     * @return the SceneType associated with this Scene
     */
    public SceneType getType() {
        return type;
    }

    /**
     * The loop of Scene code to be run every Game cycle
     */
    public abstract void cycle();

    /**
     * Used to process various inputs from Input, according to the needs of this Scene
     */
    abstract void processInputs();

    /**
     * Clean up any memory allocated during Scene operation
     */
    public abstract void cleanup();

}
