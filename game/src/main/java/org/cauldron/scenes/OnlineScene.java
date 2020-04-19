package org.cauldron.scenes;

import org.cauldron.audio.AudioManager;
import org.cauldron.entity.EntityHandler;
import org.cauldron.entity.entities.Tank;
import org.cauldron.game.*;
import org.cauldron.network.Client;
import org.cauldron.renderer.Backdrop;
import org.cauldron.renderer.BackdropScroller;
import org.cauldron.renderer.Renderer;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.animations.*;
import org.cauldron.renderer.layers.BackdropLayer;
import org.cauldron.renderer.layers.GUILayer;
import org.cauldron.renderer.layers.ModelLayer;
import org.cauldron.renderer.layers.TerrainLayer;
import org.cauldron.server.Server;
import org.cauldron.ui.UIHandler;
import org.cauldron.ui.elements.NameTag;
import org.cauldron.ui.elements.StaticHealth;

import java.util.HashMap;
import java.util.Map;

import static org.cauldron.game.Game.SceneType.MAINMENU;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class OnlineScene extends Scene {
    private boolean clientStarted;
    public ModelLayer modelLayer = new ModelLayer();
    TerrainLayer terrainLayer = new TerrainLayer();
    BackdropLayer backdropLayer = new BackdropLayer();
    BackdropScroller bScroll = null;

    public Client client;
    public int teamID;
    public EntityHandler entityHandler = new EntityHandler();
    public Terrain map;
    private Map<String, Boolean> tankToPowering = new HashMap<String, Boolean>();
    public boolean setupTerrainFlag = false; // Waiting for server to send out map
    private Server server;

    public OnlineScene(GUILayer guiLayer) {
        // Clean the animations from a previous scene
        TextureHandler.wipeAnimations();
        super.guiLayer = guiLayer;
        client = new Client();
        map = new Terrain(25);
        MapLoader.load(map, "Default");
        this.setType(Game.SceneType.ONLINE);

        // Initialise Layers
        modelLayer.setup();
        backdropLayer.setup();
        backdropLayer.setBackdrops(new Backdrop[]{new Backdrop(TextureHandler.getTextureID("clouds_fg")),
                new Backdrop(TextureHandler.getTextureID("clouds_bg")),
                new Backdrop(TextureHandler.getTextureID("sky"))});
        bScroll = new BackdropScroller(backdropLayer, 1, 200);

        modelLayer.setEntities(entityHandler);

        UIHandler.setGUILayer(guiLayer);

        UIHandler.disableAll();
        ((StaticHealth) UIHandler.getUIElements().get("StaticHealth")).resetBars();
        UIHandler.enable("OnlineWaiting");
        UIHandler.resetScoreboard();
        UIHandler.enable("CountdownTimer");

        Renderer.clear();

        if (Client.isHost) {
            System.out.println("Server started");
            server = new Server(10004);
            Thread t = new Thread(server);
            t.start();
            Client.isHost = false;
        }

        client.setRequestedGameType(Game.getRequestedGameType());
        client.setRequestedMapCntrlPoints(Game.getRequestedMapCntrlPoints());
        clientStarted = client.startClient(this);
    }

    public OnlineScene(GUILayer guiLayer, boolean isTest) {
        // Clean the animations from a previous scene
        TextureHandler.wipeAnimations();
        super.guiLayer = guiLayer;
        client = new Client();
        map = new Terrain(25);
        MapLoader.load(map, "Default");
        this.setType(Game.SceneType.ONLINE);

        // Initialise Layers
        modelLayer.setup();
        backdropLayer.setup();
        backdropLayer.setBackdrops(new Backdrop[]{new Backdrop(TextureHandler.getTextureID("clouds_fg")),
                new Backdrop(TextureHandler.getTextureID("clouds_bg")),
                new Backdrop(TextureHandler.getTextureID("sky"))});
        bScroll = new BackdropScroller(backdropLayer, 1, 200);

        modelLayer.setEntities(entityHandler);

        UIHandler.disableAll();
        ((StaticHealth) UIHandler.getUIElements().get("StaticHealth")).resetBars();

        Renderer.clear();

        client.setRequestedGameType(Game.getRequestedGameType());
        client.setRequestedMapCntrlPoints(Game.getRequestedMapCntrlPoints());
        if(!isTest) {
            boolean flag = client.startClient(this);
            while (!flag)
                flag = client.startClient(this);
        }
    }

    /**
     * Set up audio sources and health bar UI
     */
    public void postConnect() {
        String[] sources = new String[client.tankToUsernameMap.keySet().size() + 1];
        sources[0] = "bg";
        int i = 1;
        for (String tank : client.tankToUsernameMap.keySet()) {
            sources[i++] = tank;
        }
        AudioManager.setSources(sources);
        AudioManager.play("bg", "Song_For_Michael", true);

        generateHealthBars(client.tankToUsernameMap);
        generateNameTags(client.tankToUsernameMap);

        UIHandler.disable("OnlineWaiting");
    }

    @Override
    public void cycle() {
        if (!clientStarted)
            clientStarted = client.startClient(this);

        if (setupTerrainFlag) {
            terrainLayer.setTerrain(map);
            terrainLayer.setup();
            setupTerrainFlag = false;
        }

        if (Settings.fps)
            UIHandler.enable("FPSCounter");
        else
            UIHandler.disable("FPSCounter");

        if (!client.started && !client.ended) {
            Client.log("Waiting for the game to start.");
            Input.cycle();

            Renderer.clear();
            Renderer.draw(backdropLayer);
            Renderer.draw(guiLayer);

            TextureHandler.cycleAnimations(guiLayer);
            bScroll.cycle();
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
        } else {
            // Poll inputs
            Input.cycle();

            // Snap tanks and crates to ground, and turrets to tanks
            entityHandler.constrainTanks(map);
            entityHandler.constrainCrates(map);
            entityHandler.constrainTurrets(tankID, Input.cursorPos);

            // Process UI (constrain bars to tanks) and animations
            for (String tank : client.tankToUsernameMap.keySet()) {
                String userName = client.tankToUsernameMap.get(tank);
                String nameTag = userName + "NameTag";
                UIHandler.setBarsHealth(entityHandler.getTankHealth(tank), userName);
                UIHandler.setNameTag(userName, nameTag);
                UIHandler.constrainUIToTank(entityHandler, tank, userName, client.tankToUsernameMap.get(tankID));

                UIHandler.setBarsPower(userName, Input.currentMouseHeldTime);
//                UIHandler.setBarsHealth(entityHandler.getTankHealth(tank));
                Tank e = (Tank) entityHandler.getEntity(tank);
                UIHandler.setPowerUpInfo(userName, e.getAppliedPowerUps());
                UIHandler.setTimeLeft(timeRemaining);

                if (Math.abs(entityHandler.getEntity(tank).velocity.x) <= 5) {
                    TextureHandler.wipeAnimation(entityHandler.getEntity(tank));
                } else {
                    TextureHandler.setAnimation(entityHandler.getEntity(tank), new DriveAnim(entityHandler.getEntity(tank).playerColor));
                }

                if (client.tankToHeldLength.get(tank) != null) {
                    if (client.tankToHeldLength.get(tank).compareTo(0.0) > 0 && (tankToPowering.get(tank) == null || (tankToPowering.get(tank) != null && !tankToPowering.get(tank)))) {
                        System.out.println("Loading anim");
                        TextureHandler.setAnimation(((Tank) entityHandler.getEntity(tank)).turret, new LoadShellAnim(entityHandler.getEntity(tank).playerColor));
                        tankToPowering.put(tank, true);
                    }
                    if (client.tankToHeldLength.get(tank).equals(0.0) && tankToPowering.get(tank) != null && tankToPowering.get(tank)) {
                        System.out.println("Firing anim");
                        TextureHandler.setAnimation(((Tank) entityHandler.getEntity(tank)).turret, new ShootShellAnim(entityHandler.getEntity(tank).playerColor));
                        tankToPowering.put(tank, false);
                    }
                }

                processInputs();
            }

            Renderer.clear();
            Renderer.draw(backdropLayer);
            Renderer.draw(modelLayer);
            Renderer.draw(terrainLayer);
            Renderer.draw(guiLayer);

            long curTime = System.currentTimeMillis();
            entityHandler.updatePhysics(curTime - lastUpdated);
            TextureHandler.cycleAnimations(guiLayer);
            bScroll.cycle();
            scores = entityHandler.scores;
            lastUpdated = curTime;
            entityHandler.removeLostProjectiles();
            entityHandler.removeDeadParticles();
        }

        if (client.ended) {
            Game.changeSceneFlag = true;
            Game.destScene = MAINMENU;
        }
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client){
        this.client = client;
    }

    private void generateNameTags(Map<String, String> tankToUsernameMap) {
        for (String username : tankToUsernameMap.values()) {
            String name = username + "NameTag";
            ((NameTag) UIHandler.addNameTag(name)).setUserName(name);
            UIHandler.enable(name);
        }
    }

    private void generateHealthBars(Map<String, String> tankToUsernameMap) {
        ((StaticHealth) UIHandler.getUIElements().get("StaticHealth")).clientName = tankToUsernameMap.get(tankID);
        for (String username : tankToUsernameMap.values()) {
            String name = username + "Bars";
            UIHandler.addBars(name);
            UIHandler.enable(name);
        }
    }

    @Override
    void processInputs() {
        if (!client.ended)
            client.sendUpdateToServer(this.entityHandler);

        // Movement, shooting and animations
        entityHandler.setDrivingForce(tankID, 0);
        for (int key : Input.keysPressed) {
            switch (key) {
                case GLFW_KEY_D:
                    entityHandler.setDrivingForce(tankID, 10000f);
                    break;
                case GLFW_KEY_A:
                    entityHandler.setDrivingForce(tankID, -10000f);
                    break;
            }
        }
        if (Input.mouseHeldTime > 0) {
            if (((Tank) entityHandler.getEntity(tankID)).checkIfCanFire()) {
                entityHandler.fireProjectile(tankID, (float) Input.mouseHeldTime);
                System.out.println("Audio for: " + tankID);
                System.out.println();
                AudioManager.play(tankID, "CannonFire");
            }
            TextureHandler.setUIAnimation("PowerArrow", new ShootArrowAnim());
        }

        if (Input.currentMouseHeldTime > 0)
            TextureHandler.setUIAnimation("PowerArrow", new LoadArrowAnim());
        Input.mouseHeldTime = -1;
    }

    @Override
    public void cleanup() {
        modelLayer.close();
        terrainLayer.close();
        backdropLayer.close();
        client.closeClient();
        if (server != null)
            server.close();
    }

    /**
     * when the server ends an "endUpdate" this method is called to set the game over overlay
     */
    public void setEndOfGame() {
        UIHandler.enable("Scoreboard");
        UIHandler.setGameOver();
        gameOver = true;
    }
}
