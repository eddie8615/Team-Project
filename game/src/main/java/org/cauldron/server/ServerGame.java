package org.cauldron.server;

import org.cauldron.entity.Entity;
import org.cauldron.entity.EntityHandler;
import org.cauldron.entity.EntityType;
import org.cauldron.entity.entities.Tank;
import org.cauldron.game.MapLoader;
import org.cauldron.network.LobbySettings;
import org.cauldron.game.Terrain;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class ServerGame {
    public EntityHandler entityHandler = new EntityHandler();
    public long lastUpdated = System.currentTimeMillis();
    public Terrain map;
    public HashMap<Integer, String> IDTankMap = new HashMap<>();
    public HashMap<String, Integer> IDTeamMap = new HashMap<>();
    public LobbySettings settings;
    public long timeStarted;
    public Map<String, String> tankToNameMap = new HashMap<>();
    ArrayList<ArrayList<String>> teamsList;
    private long lastCrateDropped = System.currentTimeMillis();

    /**
     * Empty constructor.
     */
    public ServerGame() {
    }

    /**
     * This will create all the necessary entities to start a game.
     * @param userCount The number of users in the game.
     * @param lobbySettings The lobby settings for the game.
     * @param idNameMap
     */
    public void createInitialEntityHandler(int userCount, LobbySettings lobbySettings, Map<Integer, String> idNameMap) {
        this.settings = lobbySettings;

        map = new Terrain(25);
        ArrayList<Vector2d> mapPoints = lobbySettings.getMap();

        if (mapPoints != null)
            map.loadMap(mapPoints);
        else {
            MapLoader.load(map, "Default");
            lobbySettings.setMap(map.controlPoints);
        }

        entityHandler.createGround(map);

        double[] spawnPoints = { 200, 600, 1000, 1400 };
        String[] colours = { "red", "blue", "green", "purple" };

        int num = settings.isTeams() ? settings.getNumberOfTeams() : userCount;
        teamsList = new ArrayList<>();
        for (int i = 0; i < num; i++)
            teamsList.add(new ArrayList<>());

        int teamNumber = 0;
        int bossNumber = new Random().nextInt(userCount);

        for (int userID = 0; userID < userCount; userID++) {
            int teamNum = 0;
            Tank tank = null;
            String name = null;
            
            switch (settings.getType()) {

                case DEFAULT:
                    teamNum = userID;
                    tank = (Tank) entityHandler.createTank(colours[teamNum % colours.length]);
                    name = tank.name;

                    // Each user on different team
                    teamsList.get(userID).add(name);

                    tank.maxHealth = settings.getMaxHealth();
                    tank.position.x = spawnPoints[userID % spawnPoints.length];

                    // Update maps
                    IDTeamMap.put(name, userID); // Link ID to Team Number
                    IDTankMap.put(userID, name); // Link ID to Tank
                    break;
                case TEAM:
                    teamNum = teamNumber % lobbySettings.getNumberOfTeams();
                    tank = (Tank) entityHandler.createTank(colours[teamNum % colours.length]);
                    name = tank.name;

                    tank.maxHealth = settings.getMaxHealth();
                    tank.position.x = spawnPoints[userID % spawnPoints.length];

                    teamsList.get(teamNum).add(name);

                    // Update maps
                    IDTeamMap.put(name, teamNum); // Link ID to Team Number;
                    IDTankMap.put(userID, name); // Link ID to Tank

                    teamNumber++;
                    break;
                case BOSS:
                    teamNum = userID == bossNumber ? 0 : 1;
                    tank = (Tank) entityHandler.createTank(colours[teamNum % colours.length]);
                    name = tank.name;

                    // Boss tank
                    if (userID == bossNumber) {
                        int bossHealth = settings.getMaxHealth() * 10;
                        double bossPos = spawnPoints[bossNumber % spawnPoints.length];

                        tank.maxHealth = bossHealth;
                        tank.position.x = bossPos;
                    }
                    // Normal tank
                    else {
                        tank.maxHealth = settings.getMaxHealth();
                        tank.position.x = spawnPoints[userID % spawnPoints.length];
                    }

                    teamsList.get(teamNum).add(name);

                    // Update maps
                    IDTeamMap.put(name, teamNum); // Link ID to Team Number;
                    IDTankMap.put(userID, name); // Link ID to Tank
                    break;
                default:
                    Server.log("Unknown gamemode. Going default");
                    return;
            }

            tankToNameMap.put(name, idNameMap.get(userID));
            // will wrap around to stop oob error but will make some teams the same colour if >4 teams
            Server.log("Colour", colours[teamNum % colours.length] + " : " + teamNum);
        }

        Server.log("teamsList arr", teamsList);
        Server.log("IDTeamMap", IDTeamMap);

        timeStarted = System.currentTimeMillis();
    }

    /**
     * Update the entity handler based on an update from a client. Can a also be used to update an AI.
     * <p> We also update the physics here.
     * @param id The ID of the client.
     * @param eh The client's EntityHandler to update the turret position.
     * @param keysPressed The keys the client is pressing
     * @param mouseHeldTime The time the client has been holding the mouse down for.
     */
    public void cycle(int id, EntityHandler eh, Integer[] keysPressed, double mouseHeldTime) {
        processInputs(id, eh, keysPressed, mouseHeldTime);
        cyclePhysics();
    }

    public void cyclePhysics() {
        if (timeToCreateCrate())
            entityHandler.createCrate();

        entityHandler.constrainTanks(map);
        entityHandler.constrainCrates(map);

        entityHandler.updatePhysics(System.currentTimeMillis() - lastUpdated);
        lastUpdated = System.currentTimeMillis();
        entityHandler.removeLostProjectiles();
        entityHandler.removeDeadParticles();
    }

    private boolean timeToCreateCrate() {
        if (System.currentTimeMillis() - lastCrateDropped > 10000) {
            lastCrateDropped = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Applies inputs from a client to its tank.
     * @param id The ID of the client.
     * @param userEntityHandler The client's EntityHandler to update the turret position.
     * @param keysPressed The keys the client is pressing
     * @param mouseHeldTime The time the client has been holding the mouse down for.
     */
    public void processInputs(int id, EntityHandler userEntityHandler, Integer[] keysPressed, double mouseHeldTime) {
        String entityName = IDTankMap.get(id);
        if (entityName == null) {
            Server.error("Unknown Client ID-Entity pairing. ID: " + id);
            return;
        }
        Entity e = entityHandler.getEntity(entityName);
        if (e.type != EntityType.TANK)
            Server.error("Process inputs: oh no it happened again");
        else {
            entityHandler.setDrivingForce(entityName, 0);
            for (int key : keysPressed) {
                switch (key) {
                    case GLFW_KEY_D:
                        entityHandler.setDrivingForce(entityName, 10000f);
                        break;
                    case GLFW_KEY_A:
                        entityHandler.setDrivingForce(entityName, -10000f);
                        break;
                }
            }

            updateTurretPosition(entityName, userEntityHandler);

            if (mouseHeldTime > 0) {
                if (((Tank) entityHandler.getEntity(entityName)).checkIfCanFire())
                    entityHandler.fireProjectile(entityName, (float) mouseHeldTime);
            }
        }
    }

    /**
     * Update the rotation of the turret.
     * @param entityName The clients tank name.
     * @param userEntityHandler The client's EntityHandler.
     */
    public void updateTurretPosition(String entityName, EntityHandler userEntityHandler) {
        Entity serverEntity =     entityHandler.getEntity(entityName);
        Entity   userEntity = userEntityHandler.getEntity(entityName);
        if (serverEntity.type != EntityType.TANK)
            Server.error("Update Turret Error: Server Entity not tank.");
        else if (userEntity.type != EntityType.TANK)
            Server.error("Update Turret Error: User Entity not tank.");
        else {
            Tank serverTank = (Tank) serverEntity;
            Tank userTank = (Tank) userEntity;
            entityHandler.setEntity(serverTank.turret, userEntityHandler.getEntity(userTank.turret));
        }
    }
}
