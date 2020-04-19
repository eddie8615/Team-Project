package org.cauldron.scenes;

import org.cauldron.ai.BasicAi;
import org.cauldron.audio.AudioManager;
import org.cauldron.entity.Entity;
import org.cauldron.entity.EntityHandler;
import org.cauldron.entity.entities.Tank;
import org.cauldron.game.*;
import org.cauldron.renderer.Backdrop;
import org.cauldron.renderer.BackdropScroller;
import org.cauldron.renderer.Renderer;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.layers.BackdropLayer;
import org.cauldron.renderer.layers.GUILayer;
import org.cauldron.renderer.layers.ModelLayer;
import org.cauldron.renderer.layers.TerrainLayer;
import org.cauldron.ui.UIHandler;

import static org.lwjgl.nuklear.Nuklear.nk_window_set_focus;

public class MainMenuScene extends Scene {
    public ModelLayer modelLayer = new ModelLayer();
    TerrainLayer terrainLayer = new TerrainLayer();
    BackdropLayer backdropLayer = new BackdropLayer();
    public EntityHandler entityHandler = new EntityHandler();
    public Terrain map;
    private BasicAi aiTank1 = new BasicAi();
    private BasicAi aiTank2 = new BasicAi();
    private String tankAi1ID;
    private String tankAi2ID;
    BackdropScroller bScroll = null;

    public MainMenuScene(GUILayer guiLayer) {
        // Clean the animations from a previous scene
        TextureHandler.wipeAnimations();
        super.guiLayer = guiLayer;
        map = new Terrain(25);
        MapLoader.setTerrain(map);
        MapLoader.load("Default");
        this.setType(Game.SceneType.MAINMENU);

        // Battle of AI tanks in background
        // Create entities
        tankAi1ID = entityHandler.createTank("red").name;
        tankAi2ID = entityHandler.createTank("purple").name;
        aiTank1.setId(tankAi1ID);
        aiTank2.setId(tankAi2ID);
        entityHandler.setPos(tankAi2ID, 1300, 450);
        entityHandler.createGround(map);

        // Initialise AudioManager
        AudioManager.setSources(new String[]{"bg", tankAi1ID, tankAi2ID});
        AudioManager.play("bg", "Song_For_Michael", true);

        // Initialise the layers
        modelLayer.setup();
        terrainLayer.setTerrain(map);
        terrainLayer.setup();
        backdropLayer.setup();
        backdropLayer.setBackdrops(new Backdrop[]{new Backdrop(TextureHandler.getTextureID("clouds_fg")),
                new Backdrop(TextureHandler.getTextureID("clouds_bg")),
                new Backdrop(TextureHandler.getTextureID("sky"))});
        bScroll = new BackdropScroller(backdropLayer, 1, 200);

        // Enable various necessary UI elements for this Scene
        UIHandler.disableAll();
        UIHandler.enable("Title");
        UIHandler.enable("MainMenu");
        nk_window_set_focus(guiLayer.getContext(), "MainMenu");
        modelLayer.setEntities(entityHandler);

        Renderer.clear();
    }

    @Override
    public void cycle() {
        if (Settings.fps)
            UIHandler.enable("FPSCounter");
        else
            UIHandler.disable("FPSCounter");

        // Polling input and clearing the framebuffer
        Input.cycle();
        Renderer.clear();

        // no movement when help screen is up
        if (!UIHandler.isEnabled("HelpScreen")) {
            //AI tanks behavior
            aiTank1.updateProbabilities((float) entityHandler.getEntity(aiTank1.getId()).position.x, (float) entityHandler.getEntity(aiTank1.getId()).position.y);
            if (System.currentTimeMillis() - aiUpdate > 2000)
                aiTank1.update((float) entityHandler.getEntity(aiTank1.getId()).position.x, (float) entityHandler.getEntity(aiTank1.getId()).position.y);
            BasicAi.processAi(aiTank1.move(), aiTank1.getId(), entityHandler);
            if (((Tank) entityHandler.getEntity(aiTank1.getId())).checkIfCanFire()) {
                if (aiTank1.isShot()) {
                    float power = (float) (Math.random() * 4.5f + 0.5);
                    entityHandler.fireProjectile(aiTank1.getId(), power);
                    System.out.println(aiTank1.ranPos);
                    System.out.println(power);
                    AudioManager.play(tankAi1ID, "CannonFire", false);
                }
            }

            aiTank2.updateProbabilities((float) entityHandler.getEntity(aiTank2.getId()).position.x, (float) entityHandler.getEntity(aiTank2.getId()).position.y);
            if (System.currentTimeMillis() - aiUpdate > 2000)
                aiTank2.update((float) entityHandler.getEntity(aiTank2.getId()).position.x, (float) entityHandler.getEntity(aiTank2.getId()).position.y);
            BasicAi.processAi(aiTank2.move(), aiTank2.getId(), entityHandler);
            if (((Tank) entityHandler.getEntity(aiTank2.getId())).checkIfCanFire()) {
                if (aiTank2.isShot()) {
                    float power = (float) (Math.random() * 4.5f + 0.5);
                    entityHandler.fireProjectile(aiTank2.getId(), power);
                    System.out.println(aiTank2.ranPos);
                    System.out.println(power);
                    AudioManager.play(tankAi2ID, "CannonFire", false);
                }
            }
        }

        entityHandler.constrainTanks(map);
        entityHandler.constrainTurret(aiTank1.getId(), aiTank1.ranPos);
        entityHandler.constrainTurret(aiTank2.getId(), aiTank2.ranPos);

        // Rendering calls
        Renderer.draw(backdropLayer);
        Renderer.draw(modelLayer);
        Renderer.draw(terrainLayer);
        Renderer.draw(guiLayer);

        // Physics, animations and removal of entities off-screen
        long curTime = System.currentTimeMillis();
        entityHandler.updatePhysics(curTime - lastUpdated);
        TextureHandler.cycleAnimations(guiLayer);
        bScroll.cycle();
        lastUpdated = curTime;
        entityHandler.removeLostProjectiles();
        entityHandler.removeDeadParticles();
    }

    @Override
    void processInputs() {
    }

    @Override
    public void cleanup() {
        modelLayer.close();
        terrainLayer.close();
        backdropLayer.close();
    }
}
