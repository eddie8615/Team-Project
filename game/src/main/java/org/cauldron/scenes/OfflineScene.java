package org.cauldron.scenes;

import org.cauldron.ai.BasicAi;
import org.cauldron.audio.AudioManager;
import org.cauldron.entity.EntityHandler;
import org.cauldron.entity.PowerUpType;
import org.cauldron.entity.entities.Tank;
import org.cauldron.game.*;
import org.cauldron.renderer.Backdrop;
import org.cauldron.renderer.BackdropScroller;
import org.cauldron.renderer.Renderer;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.animations.*;
import org.cauldron.renderer.layers.BackdropLayer;
import org.cauldron.renderer.layers.GUILayer;
import org.cauldron.renderer.layers.ModelLayer;
import org.cauldron.renderer.layers.TerrainLayer;
import org.cauldron.ui.UIHandler;
import org.cauldron.ui.elements.MapSelector;
import org.cauldron.ui.elements.StaticHealth;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class OfflineScene extends Scene {
    private final long timeStarted;
    private final long timeLimit;
    //    GUILayer guiLayer = new GUILayer();
    public ModelLayer modelLayer = new ModelLayer();
    TerrainLayer terrainLayer = new TerrainLayer();
    BackdropLayer backdropLayer = new BackdropLayer();
    public EntityHandler entityHandler = new EntityHandler();
    BackdropScroller bScroll = null;
    public Terrain map;
    private boolean flag;
    public boolean paused = false;
    public boolean gameModeTimed;

    public OfflineScene(GUILayer guiLayer) {
        // Clean the animations from a previous scene
        TextureHandler.wipeAnimations();
        super.guiLayer = guiLayer;
        map = new Terrain(25);
        MapLoader.load(map, ((MapSelector) UIHandler.getUIElements().get("MapSelector")).chosenMap);
        this.setType(Game.SceneType.OFFLINE);
        gameModeTimed = ((MapSelector) UIHandler.getUIElements().get("MapSelector")).gameModeTimed;

        // Create entities
        tankID = entityHandler.createTank().name;
        entityHandler.createGround(map);
        tankAiID = entityHandler.createTank("blue").name;
        entityHandler.setPos(tankAiID, 1300, 450);
        basicAi.setId(tankAiID);

        // Initialise AudioManager
        AudioManager.setSources(new String[]{"bg", tankID, tankAiID});
        AudioManager.play("bg", "Song_For_Michael", true);

        int num = 2;
        for (int i = 0; i < num; i++) {
            teamsList.add(new ArrayList<>());
            if (i == 0)
                teamsList.get(0).add(tankID);
            else
                teamsList.get(1).add(tankAiID);
        }

        entityHandler.teamsList = teamsList;
        scores = entityHandler.scores;

        // Initialise the layers
        modelLayer.setup();
        terrainLayer.setTerrain(map);
        terrainLayer.setup();
        backdropLayer.setup();
        backdropLayer.setBackdrops(new Backdrop[]{new Backdrop(TextureHandler.getTextureID("clouds_fg")),
                new Backdrop(TextureHandler.getTextureID("clouds_bg")),
                new Backdrop(TextureHandler.getTextureID("sky"))});
        bScroll = new BackdropScroller(backdropLayer, 1, 200);

        // setup timer
        timeStarted = System.currentTimeMillis();
        timeLimit = timeRemaining = 120 * 1000;
        gameOver = false;

        // Enable various necessary UI elements for this Scene
        UIHandler.disableAll();
        UIHandler.enable("NameTag");
        ((StaticHealth) UIHandler.getUIElements().get("StaticHealth")).resetBars();
        ((StaticHealth) UIHandler.getUIElements().get("StaticHealth")).clientName = "YOU";
        UIHandler.resetScoreboard();
        if (gameModeTimed) {
            UIHandler.enable("CountdownTimer");
            UIHandler.setLivesOnBars(false);
        } else {
            UIHandler.setLivesOnBars(true);
        }

        modelLayer.setEntities(entityHandler);

        Renderer.clear();
    }

    @Override
    public void cycle() {
        if (Settings.fps)
            UIHandler.enable("FPSCounter");
        else
            UIHandler.disable("FPSCounter");

        timeRemaining = timeLimit - (System.currentTimeMillis() - timeStarted);

        Tank tank = (Tank) entityHandler.getEntity(tankID);
        Tank AITank = (Tank) entityHandler.getEntity(tankAiID);

        // check for pause or end of game
        if (paused) {
            Input.cycle();
            Renderer.clear();

            Renderer.draw(backdropLayer);
            Renderer.draw(modelLayer);
            Renderer.draw(terrainLayer);
            Renderer.draw(guiLayer);
            lastUpdated = System.currentTimeMillis();
            return;
        } else if (gameOver) {
            Input.cycle();
            Renderer.clear();

            TextureHandler.cycleAnimations(guiLayer);
            bScroll.cycle();
            Renderer.draw(backdropLayer);
            Renderer.draw(modelLayer);
            Renderer.draw(terrainLayer);
            Renderer.draw(guiLayer);
            lastUpdated = System.currentTimeMillis();
            return;
        } else if (gameModeTimed && timeRemaining < 0) {
            gameOver = true;
            UIHandler.setGameOver();
            UIHandler.enable("Scoreboard");
            return;
        } else if (!gameModeTimed) {
            if (tank.lives <= 0 | AITank.lives <= 0) {
                gameOver = true;
                UIHandler.setGameOver();
                UIHandler.enable("Scoreboard");
            }
        }

        UIHandler.constrainUIToTank(entityHandler, tankID, "", "");
        UIHandler.constrainUIToTank(entityHandler, tankAiID, "AI", "");
        // Polling input
        Input.cycle();

        // set bars info
        UIHandler.setBarsPower("", Input.currentMouseHeldTime);
        UIHandler.setBarsHealth(entityHandler.getTankHealth(tankID));
        UIHandler.setAIBarsHealth(entityHandler.getTankHealth(tankAiID));

        // lives info
        if (!gameModeTimed) {
            UIHandler.setBarsLives(tank.lives, AITank.lives);
        }

        // set powerup info
        UIHandler.setPowerUpInfo("", tank.getAppliedPowerUps());
        UIHandler.setPowerUpInfo("AI", AITank.getAppliedPowerUps());

        //set scoreboard info
        UIHandler.setScoreboardStats(tank.stats, AITank.stats);
        UIHandler.setTimeLeft(timeRemaining);

        // Clear the framebuffer
        Renderer.clear();
        processInputs();

        if (timeToCreateCrate()) {
            entityHandler.createCrate();
        }

        handleAI();

        // Snap tanks and crates to ground and turrets to tanks
        entityHandler.constrainTanks(map);
        entityHandler.constrainCrates(map);
        entityHandler.constrainTurret(tankID, Input.cursorPos);
        entityHandler.constrainTurret(tankAiID, basicAi.ranPos);

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
        scores = entityHandler.scores;
        lastUpdated = curTime;
        entityHandler.removeLostProjectiles();
        entityHandler.removeDeadParticles();
    }

    /**
     * @return true if we haven't spawned a crate in the past 10 seconds.
     */
    private boolean timeToCreateCrate() {
        if (System.currentTimeMillis() - lastCrateDropped > 10000) {
            lastCrateDropped = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void handleAI() {
        basicAi.updateProbabilities((float) entityHandler.getEntity(tankAiID).position.x, (float) entityHandler.getEntity(tankAiID).position.y);
        if (System.currentTimeMillis() - aiUpdate > 2000)
            basicAi.update((float) entityHandler.getEntity(tankAiID).position.x, (float) entityHandler.getEntity(tankAiID).position.y);
        BasicAi.processAi(basicAi.move(), basicAi.getId(), entityHandler);
        if (((Tank) entityHandler.getEntity(tankAiID)).checkIfCanFire()) {
            if (basicAi.isShot()) {
                float power = (float) (Math.random() * 4.5f + 0.5);
                entityHandler.fireProjectile(tankAiID, power);
                AudioManager.play(tankAiID, "CannonFire");
            }
        }

        if (Math.abs(entityHandler.getEntity(tankAiID).velocity.x) <= 5) {
            TextureHandler.wipeAnimation(entityHandler.getEntity(tankAiID));
        } else {
            TextureHandler.setAnimation(entityHandler.getEntity(tankAiID), new DriveAnim(entityHandler.getEntity(tankAiID).playerColor));
        }
    }


    @Override
    void processInputs() {
        entityHandler.setDrivingForce(tankID, 0);
        int multiplier = 1;
        Tank tank;
        // Movement key processing
        for (int key : Input.keysPressed) {
            switch (key) {
                case GLFW_KEY_D:
                    tank = ((Tank) entityHandler.getEntity(tankID));
                    if (tank.hasPowerUp(PowerUpType.SPEED)) {
                        multiplier *= 2;
                    }
                    if (tank.hasPowerUp(PowerUpType.REVERSE)) {
                        multiplier *= -1;
                    }
                    entityHandler.setDrivingForce(tankID, 10000f * multiplier);
                    break;
                case GLFW_KEY_A:
                    tank = ((Tank) entityHandler.getEntity(tankID));
                    if (tank.hasPowerUp(PowerUpType.SPEED)) {
                        multiplier *= 2;
                    }
                    if (tank.hasPowerUp(PowerUpType.REVERSE)) {
                        multiplier *= -1;
                    }
                    entityHandler.setDrivingForce(tankID, -10000f * multiplier);
                    break;
            }
        }

        // Movement animation
        if (Math.abs(entityHandler.getEntity(tankID).velocity.x) <= 5) {
            TextureHandler.wipeAnimation(entityHandler.getEntity(tankID));
        } else {
            TextureHandler.setAnimation(entityHandler.getEntity(tankID), new DriveAnim(entityHandler.getEntity(tankID).playerColor));
        }

        // Shooting and animations for it
        if (Input.mouseHeldTime > 0) {
            TextureHandler.setAnimation(((Tank) entityHandler.getEntity(tankID)).turret, new ShootShellAnim(entityHandler.getEntity(tankID).playerColor));
            TextureHandler.setUIAnimation("PowerArrow", new ShootArrowAnim());
            if (((Tank) entityHandler.getEntity(tankID)).checkIfCanFire()) {
                entityHandler.fireProjectile(tankID, (float) Input.mouseHeldTime);
                AudioManager.play(tankID, "CannonFire");
            }
            Input.mouseHeldTime = -1;
        }
        if (Input.currentMouseHeldTime > 0) {
            TextureHandler.setAnimation(((Tank) entityHandler.getEntity(tankID)).turret, new LoadShellAnim(entityHandler.getEntity(tankID).playerColor));
            TextureHandler.setUIAnimation("PowerArrow", new LoadArrowAnim());
        }
    }

    @Override
    public void cleanup() {
        modelLayer.close();
        terrainLayer.close();
        backdropLayer.close();
    }
}
