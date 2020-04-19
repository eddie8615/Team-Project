package org.cauldron.network;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.cauldron.entity.EntityHandler;
import org.cauldron.entity.entities.Tank;
import org.cauldron.game.Input;
import org.cauldron.network.LobbySettings.GameType;
import org.cauldron.scenes.OnlineScene;
import org.cauldron.scenes.Scene;
import org.cauldron.ui.UIHandler;
import org.joml.Vector2d;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class Client {
    private SocketChannel client;
    public static String address;
    private final int port = 10004;
    private double packetNum = 0;
    private ArrayList<Message> reconciliation = new ArrayList<>();
    public ArrayList<Message> testMessages = new ArrayList<>();

    private static int TICK_COUNT = 10; // number of input updates until send again
    private int cycleCount = 0;
    private String name;
    private OnlineScene game;
    public boolean accepted = false;
    public boolean started = false;
    public boolean ended = false;
    public Map<String, String> tankToUsernameMap = new HashMap<>();
    public Map<String, Double> tankToHeldLength = new HashMap<>();
    private GameType requestedGameType = null;
    private ArrayList<Vector2d> requestedMapCntrlPoints = null;
    public static boolean isHost = false;

    public void setRequestedMapCntrlPoints(ArrayList<Vector2d> requestedMapCntrlPoints) {
        this.requestedMapCntrlPoints = requestedMapCntrlPoints;
    }


    /**
     * Creates a new client.
     */
    public Client() {
    }

    /**
     * We start the connection to the server here.
     *
     * @param game The game so we can later update.
     * @return The success in starting the client.
     */
    public boolean startClient(OnlineScene game) {
        InetSocketAddress hostAddress = new InetSocketAddress(Client.address, port);
        try {
            client = SocketChannel.open(hostAddress);
            client.configureBlocking(true);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        name = Scene.userInfo != null ? Scene.userInfo.getUserID() : UIHandler.username;

        log("started");
        this.game = game;

        startThreads();
        sendRegisterMessage();
        return true;
    }

    /**
     * Starts the listener for receiving updates from the server.
     */
    private void startThreads() {
        Listener listener = new Listener();
        Thread listenerThread = new Thread(listener);

        listenerThread.start();
    }

    /**
     * Send a register message to the server.
     */
    private void sendRegisterMessage() {
        Message initMessage = new Message("register");
        initMessage.name = name;
        sendMessage(initMessage);

        log("sent init. message:" + initMessage);
    }

    /**
     * Attempt to send our inputs to the server.
     * <p> If a shot has been fired client-side, always send a message.
     *
     * @param eh The entity handler.
     */
    public void sendUpdateToServer(EntityHandler eh) {
        if (!started)
            return;

        // Always send update if a shot is fired
        boolean shotFired = Input.mouseHeldTime > -1
                && ((Tank) game.entityHandler.getEntity(game.tankID)).checkIfCanFire();

        if (shotFired || cycleCount++ % TICK_COUNT == 0) {
            Integer[] keys = new Integer[Input.keysPressed.size()];
            Input.keysPressed.toArray(keys);

            Message message = new Message("update");
            message.entityHandler = eh;
            message.keysPressed = keys;
            message.currentMouseHeldTime = Input.currentMouseHeldTime;
            System.out.println("Current held time according to client: " + Input.currentMouseHeldTime);
            message.mouseHeldTime = Input.mouseHeldTime;

            sendMessage(message);
        }
    }

    /**
     * Send the current lobby settings to the server.
     *
     * @param lobbySettings The lobby settings.
     */
    public void sendLobbySettingsToServer(LobbySettings lobbySettings) {
        Message message = new Message("lobby");
        message.lobbySettings = lobbySettings;
        log("sending lobby message");
        sendMessage(message);
    }

    /**
     * This class allows us to start another thread to listen for updates from the server.
     */
    private class Listener implements Runnable {
        @Override
        public void run() {
            log("listener started");
            ByteBuffer buffer = ByteBuffer.allocate(100000); // allocate DUMMY amounts

            /*
             * Always listens for updates from the server.
             */
            while (true) {
                int numRead = -1;
                try {
                    numRead = client.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (numRead == -1)
                    break;

                byte[] data = buffer.array();

                try {
                    Message message = SerializationUtils.deserialize(data);
                    processMessage(message);
                } catch (SerializationException e) {
                    error("Serialisation Exception");
                    e.printStackTrace();
                } catch (Exception e) {
                    error("Exception from process");
                    e.printStackTrace();
                }

                buffer.clear();
            }
        }

        /**
         * This takes a message from the server and updates our game accordingly.
         *
         * @param message The message from the server.
         */
        private void processMessage(Message message) {
            log("");
            log("");
//            log("recon list: " + reconciliation);
            log("message", message);

            switch (message.type) {
                // Response from server when registering
                case "accepted":
                    accepted = true;

                    // lobby leader, this user sets the info for all users.
                    if (message.stringData != null && message.stringData.equals("leader")) {

                        LobbySettings lobbySettings = new LobbySettings();
                        if (requestedGameType != null)
                            lobbySettings.updateType(requestedGameType);
                        log("req points", requestedMapCntrlPoints);
                        lobbySettings.setMap(requestedMapCntrlPoints);

                        sendLobbySettingsToServer(lobbySettings);
                    }

                    if (message.name != name) {
                        name = message.name;
                        System.out.println("Name rejected. New name: " + name);
                    }

                    break;
                // Resend register message
                case "rejected":
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sendRegisterMessage();
                    break;
                // the first game message from the server
                case "initialUpdate":
                    EntityHandler eh = message.entityHandler;
                    game.entityHandler = eh;
                    game.modelLayer.setEntities(eh);
                    game.lobbySettings = message.lobbySettings;
                    game.timeRemaining = message.timeRemaining;
                    game.teamsList = message.teamsList;
                    game.tankID = message.stringData;
                    game.teamID = message.teamID;
                    tankToUsernameMap = message.tankToNameMap;
                    log(String.format("We are '%s' on team %d.", message.stringData, message.teamID));

                    // TODO this is the shit
                    game.map.loadMap(message.lobbySettings.getMap());
                    game.setupTerrainFlag = true;

                    started = true;

                    game.postConnect();
                    log("game started!!");
                    break;
                // main update case
                case "update":
                    if (!started)
                        return;

                    log("received new entity handler");

                    EntityHandler serverEntityHandler = message.entityHandler;
                    game.entityHandler = serverEntityHandler;
                    tankToHeldLength = message.tankToHeldLength;
                    game.modelLayer.setEntities(serverEntityHandler);

                    game.entityHandler.constrainTanks(game.map);
                    game.entityHandler.constrainCrates(game.map);
                    game.entityHandler.constrainTurrets(game.tankID, Input.cursorPos);

                    game.timeRemaining = message.timeRemaining;

                    double serverPacketNumber = message.packetNum;
                    log("server packet:" + serverPacketNumber + ", last packet we sent:" + packetNum);

                    int i = 0;
                    while (i < reconciliation.size()) {
                        Message reconMessage = reconciliation.get(i);

                        if (reconMessage.packetNum <= serverPacketNumber) {
                            log("reconcile!, m packet:" + reconMessage.packetNum + "<= server packet:" + serverPacketNumber);
                            reconciliation.remove(i);
                        } else {
                            log("server lagging behind, apply inputs again for packet: " + reconMessage.packetNum);

                            Integer[] keysPressed = reconMessage.keysPressed;
                            double mouseHeldTime = reconMessage.mouseHeldTime;
                            reconciliationUpdate(keysPressed, mouseHeldTime);
                            i++;
                        }
                    }
                    break;
                case "endUpdate":
                    if (!started)
                        return;

                    log("received final entity handler");
                    accepted = false;
                    started = false;
                    ended = true;
                    game.entityHandler = message.entityHandler;
                    game.modelLayer.setEntities(message.entityHandler);
                    game.entityHandler.constrainTanks(game.map);
                    game.entityHandler.constrainTurrets(game.tankID, Input.cursorPos);
                    game.setEndOfGame(); // assuming we are in online mode
                    break;
                case "debug":
                    String str = message.stringData;
                    log(str);
                    testMessages.add(message);
                    break;
                default:
                    error("unknown type: " + message.type);
                    break;
            }
        }

        /**
         * Takes inputs from an old message sent to the server and applies it to our EntityHandler. Used when the server is lagging behind on updates.
         *
         * @param keysPressed   The keys pressed.
         * @param mouseHeldTime The time the mouse was down for.
         */
        private void reconciliationUpdate(Integer[] keysPressed, double mouseHeldTime) {
            log("recon", "update");

            game.entityHandler.setDrivingForce(game.tankID, 0);
            if (keysPressed == null)
                return;

            for (int key : keysPressed) {
                switch (key) {
                    case GLFW_KEY_D:
                        log("recon", "Applying D.");
                        game.entityHandler.setDrivingForce(game.tankID, 10000f);
                        break;
                    case GLFW_KEY_A:
                        log("recon", "Applying A.");
                        game.entityHandler.setDrivingForce(game.tankID, -10000f);
                        break;
                }
            }
            if (mouseHeldTime > 0)
                if (((Tank) game.entityHandler.getEntity(game.tankID)).checkIfCanFire())
                    game.entityHandler.fireProjectile(game.tankID, (float) mouseHeldTime);

            game.entityHandler.constrainEntityToGround(game.tankID, game.map);
            game.entityHandler.constrainTurret(game.tankID, Input.cursorPos);
        }
    }


    /**
     * Takes a message and attempts to send it to the server.
     *
     * @param message The message.
     */
    public void sendMessage(Message message) {
        if (accepted || !ended || message.type.equals("register") || message.type.equals("lobby")) {
            message.packetNum = packetNum;
            ByteBuffer buffer = ByteBuffer.wrap(SerializationUtils.serialize(message));
            SocketAddress addr = null;
            try {
                addr = client.getLocalAddress();
                log("sending [" + message + "] to " + addr);
                client.write(buffer);
                reconciliation.add(message);
                packetNum++;
            } catch (IOException e) {
                error("Failed to send message to:" + addr);
                e.printStackTrace();
            }
        }
    }

    /**
     * Output to the client's console. Takes a name to add the address before the message. Calls .toString() on the provided objects.
     * <p> EG:
     * <p>{@code [XYZ] This is an example message}
     *
     * @param name    The name.
     * @param message The message to write.
     */
    public static void log(Object name, Object message) {
        if (message != null && name != null)
            System.out.println("[" + name.toString() + "] " + message.toString());
    }

    /**
     * Output to the client's console. Calls .toString() on the provided object.
     * <p> EG:
     * <p>{@code This is an example message}
     *
     * @param message The message to write.
     */
    public static void log(Object message) {
        if (message != null)
            System.out.println(message.toString());
    }

    /**
     * Output to the client's console as an error. Takes a name to add the address before the message. Calls .toString() on the provided objects.
     * <p> EG:
     * <p>{@code [XYZ] This is an example message}
     *
     * @param name    The name.
     * @param message The message to write.
     */
    public static void error(Object name, Object message) {
        if (message != null && name != null)
            System.err.println("[" + name.toString() + "] " + message.toString());
    }

    /**
     * Output to the client's console as an error. Calls .toString() on the provided object.
     * <p> EG:
     * <p>{@code This is an example message}
     *
     * @param message The message to write.
     */
    public static void error(Object message) {
        if (message != null)
            System.err.println(message.toString());
    }

    public void setRequestedGameType(GameType gameType) {
        this.requestedGameType = gameType;
    }

    public GameType getRequestedGameType() {
        return requestedGameType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public boolean closeClient() {
        try {

            client.close();
            started = false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ArrayList<Message> getReconciliation(){
        return reconciliation;
    }
}
