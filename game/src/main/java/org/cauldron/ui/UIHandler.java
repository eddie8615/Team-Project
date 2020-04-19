package org.cauldron.ui;

import org.cauldron.entity.Entity;
import org.cauldron.entity.EntityHandler;
import org.cauldron.entity.components.AppliedPowerUp;
import org.cauldron.entity.components.Stats;
import org.cauldron.entity.entities.Tank;
import org.cauldron.game.Game;
import org.cauldron.game.Settings;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.layers.GUILayer;
import org.cauldron.ui.elements.*;
import org.cauldron.ui.styles.DefaultStyle;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.nuklear.NkContext;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cauldron.game.Game.SceneType.OFFLINE;
import static org.cauldron.game.Game.SceneType.ONLINE;


public class UIHandler {
    static Map<String, UIElement> elements = new HashMap<>();
    static Map<String, TexUIElement> texElements = new HashMap<>();
    static NkContext ctx = null;
    static GUILayer guiLayer = null;
    static long window;
    static private final int x = 615;
    static private final int y = 650;
    static public DefaultStyle style = new DefaultStyle();
    static public Game game;
    static private List<Integer> uuidsInUse = new ArrayList<Integer>();
    static public String username = "";

    /**
     * Set up default UIElements
     *
     * @param w window id, so the MainMenu can have power over the window
     */
    public static void init(long w) {
        window = w;
        elements.put("MainMenu", new MainMenu(ctx, "MainMenu", x, 450, window));
        elements.put("Title", new Title(ctx, "Title", 400, 750));
        elements.put("InGameMenu", new InGameMenu(ctx, "InGameMenu", x, y));
        elements.put("FPSCounter", new FPSCounter(ctx, "FPSCounter", -10, 900));
        elements.put("CountdownTimer", new CountdownTimer(ctx, "CountdownTimer", 1500, 900));
        elements.put("Login", new Login(ctx, "Login", x, y));
        elements.put("Register", new Register(ctx, "Register", x, y));
        elements.put("Settings", new SettingsElem(ctx, "Settings", x, y + 100));
        elements.put("HelpScreen", new HelpScreen(ctx, "HelpScreen", -16, 905));
        elements.put("InGameMenu", new InGameMenu(ctx, "InGameMenu", x, y));
        elements.put("NameTag", new NameTag(ctx, "NameTag", 650, 650));
        elements.put("StaticHealth", new StaticHealth(ctx, "StaticHealth", 20, 95));
        elements.put("Bars", new Bars(ctx, "Bars", x, y, true, true));
        elements.put("AIBars", new Bars(ctx, "AIBars", x, y, true, false));
        elements.put("LobbySystem", new LobbySystem(ctx, "LobbySystem", x, y));
        elements.put("Scoreboard", new Scoreboard(ctx, "Scoreboard", 500, 850));
        elements.put("PowerUpInfo", new PowerUpInfo(ctx, "PowerUpInfo", 900, 100));
        elements.put("GridOverlay", new GridOverlay(ctx, "GridOverlay", 0, 900));
        elements.put("MapEditor", new MapEditor(ctx, "MapEditor", 1200, 850));
        elements.put("MapSelector", new MapSelector(ctx, "MapSelector", x, y));
        elements.put("OnlineWaiting", new OnlineWaiting(ctx, "OnlineWaiting", x, y - 200));
        // TODO add lobby buttons
        texElements.put("PowerArrow", new TexUIElement("PowerArrow", x, y, 12, 50, TextureHandler.getTextureID("arrow_off")));
    }

    /**
     * Generate unique ID for each UIElement
     *
     * @return UUID or -1 if failed
     */
    public static int genUUID() {
        for (int i = 1; i < 100; i++) {
            if (!uuidsInUse.contains(i)) {
                uuidsInUse.add(i);
                return i;
            }
        }
        return -1;
    }

    public static GUILayer getGUILayer() {
        return guiLayer;
    }

    /**
     * Enables a UIElement and disables another
     *
     * @param disableUI UIElement to disable
     * @param enableUI  UIElement to enable
     */
    public static void uiProgress(String disableUI, String enableUI) {
        if (elements.get(disableUI) == null)
            return;
        elements.get(disableUI).disable();
        if (elements.get(enableUI) == null)
            return;
        elements.get(enableUI).enable();
    }

    public static void enable(String uiName) {
        if (elements.get(uiName) != null)
            elements.get(uiName).enable();
        if (texElements.get(uiName) != null)
            texElements.get(uiName).enable();
    }

    public static void toggle(String uiName) {
        if (elements.get(uiName) != null)
            elements.get(uiName).toggle();
        if (texElements.get(uiName) != null)
            texElements.get(uiName).toggle();
    }

    public static void disableAll() {
        for (UIElement elem : elements.values()) {
            elem.disable();
        }
        for (TexUIElement elem : texElements.values()) {
            elem.disable();
        }
    }

    public static void disable(String uiName) {
        if (elements.get(uiName) != null)
            elements.get(uiName).disable();
        if (texElements.get(uiName) != null)
            texElements.get(uiName).disable();
    }

    public static void setPosition(String uiName, float x, float y) {
        if (elements.get(uiName) != null)
            elements.get(uiName).setPosition(x, y);
        if (texElements.get(uiName) != null)
            texElements.get(uiName).setPosition(x, y);
    }

    public static void setPosition(String uiName, Vector2f v) {
        if (elements.get(uiName) != null)
            elements.get(uiName).setPosition(v);
        if (texElements.get(uiName) != null)
            texElements.get(uiName).setPosition(v);
    }

    public static void setPosition(String uiName, Vector2d v) {
        if (elements.get(uiName) != null)
            elements.get(uiName).setPosition(v);
        if (texElements.get(uiName) != null)
            texElements.get(uiName).setPosition(v);
    }

    public static void setRotation(String uiName, float r) {
        if (elements.get(uiName) != null)
            elements.get(uiName).setRotation(r);
        if (texElements.get(uiName) != null)
            texElements.get(uiName).setRotation(r);
    }

    public static Map<String, UIElement> getUIElements() {
        return elements;
    }

    public static void setGUILayer(GUILayer gl) {
        guiLayer = gl;
        ctx = guiLayer.getContext();

        for (Map.Entry<String, UIElement> elem : elements.entrySet()) {
            elem.getValue().ctx = ctx;
        }

        guiLayer.setUIElements(elements);
        guiLayer.setTexUIElements(texElements);
    }

    /**
     * Used for debugging position of a UIElement
     *
     * @param nameTag the nametag of the element
     * @return vector2D of the position of the element
     */
    public static Vector2d getPosition(String nameTag) {
        UIElement element = elements.get(nameTag);
        return new Vector2d(element.x, element.y);
    }

    /**
     * sets the strength and shield booleans for the powerbar info widget
     *
     * @param appliedPowerUps list of currently applied powerups - will all have their timeLeft string set.
     */
    public static void setPowerUpInfo(String username, ArrayList<AppliedPowerUp> appliedPowerUps) {
        PowerUpInfo powerUpInfo = (PowerUpInfo) elements.get("PowerUpInfo");
        powerUpInfo.appliedPowerUps = appliedPowerUps;

        Bars bars = (Bars) elements.get(username + "Bars");
        bars.appliedPowerUps = appliedPowerUps;
    }

    public static void setAIPowerUpInfo(ArrayList<AppliedPowerUp> appliedPowerUps) {
        Bars bars = (Bars) elements.get("AIBars");
        bars.appliedPowerUps = appliedPowerUps;
    }


    /**
     * Used to set the PowerBar value for your tank.
     *
     * @param currentPower double value of current power. Realistically will be current mouse held time capped to max.
     */
    public static void setBarsPower(String username, double currentPower) {
        Bars bars = (Bars) elements.get(username + "Bars");
        // caps at max
        if (currentPower > EntityHandler.maxPowerUpTime) {
            currentPower = EntityHandler.maxPowerUpTime;
        }
        bars.power = BigInteger.valueOf((long) (currentPower * (100 / EntityHandler.maxPowerUpTime)));
        //System.out.println(powerBar.power);

        StaticHealth staticHealth = (StaticHealth) elements.get("StaticHealth");
        staticHealth.power = bars.power;
    }

    /**
     * Used to set the HealthBar value for your tank.
     *
     * @param currentHealth double value of current health.
     */
    public static void setBarsHealth(double currentHealth) {
        Bars bars = (Bars) elements.get("Bars");
        currentHealth = Math.min(100, currentHealth);
        bars.health = BigInteger.valueOf((long) currentHealth);

        StaticHealth staticHealth = (StaticHealth) elements.get("StaticHealth");
        staticHealth.nameToHealth.put("YOU", bars.health);
    }

    public static void setBarsHealth(double currentHealth, String userName) {
        Bars bars = (Bars) elements.get(userName + "Bars");
        currentHealth = Math.min(100, currentHealth);
        bars.health = BigInteger.valueOf((long) currentHealth);

        StaticHealth staticHealth = (StaticHealth) elements.get("StaticHealth");
        staticHealth.nameToHealth.put(userName, bars.health);
    }

    /**
     * Used to set the AIHealthBar value for the AI tank.
     *
     * @param currentHealth double value of current health.
     */
    public static void setAIBarsHealth(double currentHealth) {
        Bars bars = (Bars) elements.get("AIBars");
        currentHealth = Math.min(100, currentHealth);
        bars.health = BigInteger.valueOf((long) currentHealth);

        StaticHealth staticHealth = (StaticHealth) elements.get("StaticHealth");
        staticHealth.nameToHealth.put("AI", bars.health);
    }

    /**
     * sets the lives number (out of 8 max) to appear on the bars for the players.
     *
     * @param playerLives lives remaining for the player
     * @param AILives     lives remaining for the AI tank
     */
    public static void setBarsLives(int playerLives, int AILives) {
        Bars bars = (Bars) elements.get("Bars");
        bars.lives = playerLives;
        Bars AIBars = (Bars) elements.get("AIBars");
        AIBars.lives = AILives;
    }

    public static void setLivesOnBars(boolean enabled) {
        Bars bars = (Bars) elements.get("Bars");
        bars.livesEnabled = enabled;
        Bars AIBars = (Bars) elements.get("AIBars");
        AIBars.livesEnabled = enabled;
    }

    public static void setUITexture(String name, int texID) {
        if (texElements.get(name) != null)
            texElements.get(name).texID = texID;
    }

    public static void setNameTag(String name) {
        NameTag nameTag = (NameTag) elements.get("NameTag");
        nameTag.setUserName(name);
        elements.put("NameTag", nameTag);
    }

    public static void setNameTag(String name, String nameTagUI) {
        NameTag nameTag = (NameTag) elements.get(nameTagUI);
        nameTag.setUserName(name);
        elements.put(nameTagUI, nameTag);
    }

    public static UIElement addNameTag(String name) {
        if (elements.containsKey(name)) {
            return elements.get(name);
        }
        elements.put(name, new NameTag(ctx, name, x, y));
        return elements.get(name);
    }

    public static void addBars(String name) {
        if (elements.containsKey(name)) {
            return;
        }
        elements.put(name, new Bars(ctx, name, x, y, true, false));
    }

    public static boolean isEnabled(String uiName) {
        if (elements.get(uiName) != null)
            return elements.get(uiName).isEnabled();
        return false;
    }

    public static void setPowerUpsOnBars(String name, boolean powerUpsOnBars) {
        Bars bars = (Bars) elements.get(name);
        bars.powerUpsEnabled = powerUpsOnBars;
    }

    private static Vector2f getBarPos(Entity e, float offset) {
        Vector2f v = new Vector2f((float) e.position.x, (float) e.position.y);
        float rot = e.rotation;
        Vector2f direction = new Vector2f((float) Math.cos(Math.toRadians(rot)), (float) Math.sin(Math.toRadians(rot))).normalize();
        Vector2f normal = new Vector2f((float) Math.cos(Math.toRadians(rot + 90)), (float) Math.sin(Math.toRadians(rot + 90))).normalize();
        return v.add(direction.mul(-25)).add(normal.mul(offset));
    }

    /**
     * Make UIElements follow and rotate with the tanks
     *
     * @param eh             EntityHandler containing entities you'd like to constrain UI to
     * @param tank           The name of the tank to constrain UI to
     * @param username       The username of the user who's UI elements you wish to constrain to tank
     * @param clientUsername The client's username, to determine who to display power indicators for
     */
    public static void constrainUIToTank(EntityHandler eh, String tank, String username, String clientUsername) {
        UIHandler.setPosition(username + "NameTag", getBarPos(eh.getEntity(tank), 0));
        UIHandler.setRotation(username + "NameTag", eh.getEntity(tank).rotation);
        UIHandler.setPowerUpsOnBars(username + "Bars", Settings.powerUpsOnBars);
        if (Settings.powerUpsOnBars)
            UIHandler.disable("PowerUpInfo");
        else
            UIHandler.enable("PowerUpInfo");
        //update power bars
        if (Settings.barsFollow) {
            UIHandler.disable("StaticHealth");
            UIHandler.enable(username + "Bars");
            UIHandler.setPosition(username + "Bars", getBarPos(eh.getEntity(tank), -20));
            UIHandler.setRotation(username + "Bars", eh.getEntity(tank).rotation);
        } else {
            UIHandler.enable("StaticHealth");
            UIHandler.disable(username + "Bars");
        }
        if ((Game.currentScene.getType() == ONLINE && username.equals(clientUsername)) ||
                (Game.currentScene.getType() == OFFLINE && username.equals(""))) {
            if (Settings.powerIndicator.equals("both") || Settings.powerIndicator.equals("arrow")) {
                ((Bars) UIHandler.getUIElements().get(username + "Bars")).powerEnabled = Settings.powerIndicator.equals("both");
                UIHandler.enable("PowerArrow");
                // update power arrow
                UIHandler.setPosition("PowerArrow", eh.getEntity(((Tank) eh.getEntity(tank)).turret).getCornerCoords()[1]);
                UIHandler.setRotation("PowerArrow", eh.getEntity(((Tank) eh.getEntity(tank)).turret).rotation);
            } else if (Settings.powerIndicator.equals("bar")) {
                UIHandler.disable("PowerArrow");
                ((Bars) UIHandler.getUIElements().get(username + "Bars")).powerEnabled = true;
            }
        }
    }

    /**
     * used to set the statistics of the tanks in offline mode to show on the scoreboard
     *
     * @param youStats
     * @param AIStats
     */
    public static void setScoreboardStats(Stats youStats, Stats AIStats) {
        Scoreboard sb = (Scoreboard) UIHandler.getUIElements().get("Scoreboard");
        sb.youStats = youStats;
        sb.AIStats = AIStats;
    }

    public static void setGameOver() {
        Scoreboard sb = (Scoreboard) UIHandler.getUIElements().get("Scoreboard");
        sb.gameFinished = true;
    }

    public static void setTimeLeft(long timeRemaining) {
        Scoreboard sb = (Scoreboard) UIHandler.getUIElements().get("Scoreboard");
        sb.timeRemaining = timeRemaining;
        CountdownTimer ct = (CountdownTimer) UIHandler.getUIElements().get("CountdownTimer");
        ct.timeRemaining = timeRemaining;
    }

    public static void resetScoreboard() {
        Scoreboard sb = (Scoreboard) UIHandler.getUIElements().get("Scoreboard");
        sb.initSB();
    }
}
