package org.cauldron.server;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.cauldron.entity.EntityHandler;
import org.cauldron.game.MapLoader;
import org.cauldron.network.LobbySettings;
import org.cauldron.network.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {
    private Selector selector;
    private InetSocketAddress listenAddress;

    private final int MAX_USERS = 4;
    private int forced_user_count;
    private int numberConnected;
    private int idNum;

    private ServerGame game;
    private final int updatePeriod = 100; // in milliseconds
    private boolean initMessage;
    private double[] lastProcessedInput;
    private double[] shotFiredLength;
    private double[] heldLength;
    private long[] lastHeard;
    private Map<SocketChannel, Integer> userIDMap;
    private SocketChannel[] channelArr;
    private boolean lobbySettingsMessage;
    private LobbySettings lobbySettings;
    private Map<Integer, String> idNameMap;
    private long currentTime;
    private boolean closed = false;
    private final int FAILCOUNT = 5;
    private int count_failed = 0;

    /**
     * Server constructor.
     * @param address The IP of the host machine.
     * @param port The port to use.
     */
    private Server(String address, int port) throws IllegalArgumentException {
        listenAddress = new InetSocketAddress(address, port);
    }

    /**
     * Server constructor. Runs for the wildcard IP.
     * @param port The port to use.
     */
    public Server(int port) throws IllegalArgumentException {
        listenAddress = new InetSocketAddress(port);
    }

    public static void main(String[] args) {
        String address = "localhost";
        int port = 10004;

        switch(args.length) {
            case 2:
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    error("Port was not an integer.");
                    e.printStackTrace();
                    return;
                }
            case 1:
                address = args[0];
            case 0:
                break;
            default:
                log("Unknown number of parameters.\nCorrect usage is:");
                log("  0: hosts on " + address + " : " + port);
                log("  1: hosts on argument  : " + port);
                log("  2: hosts on argument1 : argument2");
                return;
        }

        log(String.format("%d %s. Starting server on %s:%d.", args.length, args.length == 1 ? "arg" : "args", address, port));

        try {
            new Server(address, port).startServer();
        } catch (IOException e) {
            error("Uh oh, failed to start server.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            error("Failed to create InetSocketAddress.");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new server instance and opens the server socket. Creates a game as well as starting the updater and listener.
     * @throws IOException If creation of the socket fails, we throw an exception.
     */
    private void startServer() throws IOException {
        setupGame();

        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        log("Server started...: " + listenAddress);

        MapLoader.init();

        startUpdater();
        startListener();
    }

    /**
     * Setup all attributes for the game.
     */
    public void setupGame() {
        //For testing, forced_user_count would be 1;
        forced_user_count = 2;
        numberConnected = 0;
        idNum = 0;
        initMessage = false;
        lobbySettingsMessage = false;
        game = new ServerGame();
        channelArr = new SocketChannel[MAX_USERS];
        lastProcessedInput = new double[MAX_USERS];
        heldLength = new double[MAX_USERS];
        lastHeard = new long[MAX_USERS];
        for (int i = 0; i < MAX_USERS; i++)
            lastHeard[i] = -1;

        idNameMap = new HashMap<>();
        userIDMap = new HashMap<>();
    }

    /**
     * This is where we send out updates to our clients from.
     */
    private void startUpdater() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable updater = new Runnable() {
            /**
             * Checks whether we need to send a message to the clients and which message to send.
             * We also will create the base entity handler for our game here
             */
            public void run() {
                if (closed) {
                    log("Host disconnected");
                    endGame();
                    executor.shutdownNow();
                    return;
                }

                if(!lobbySettingsMessage) {
                    log("Waiting for the lobby settings message.");
                    log(String.format("number of users. %d.", numberConnected));
                } else {
                    if (numberConnected != forced_user_count) {
                        log(String.format("Waiting for correct number of users. %d/%d.", numberConnected, forced_user_count));
                    } else {
                        // We have all users connected
                        if (!initMessage) {
                            log("Creating & sending initial Entity Handler to connected users.");
                            game.createInitialEntityHandler(forced_user_count, lobbySettings, idNameMap);

                            broadCastEntityHandler("initialUpdate", game.entityHandler);

                            initMessage = true;
                        } else {
                            log("Sending usual update.");
                            if (!checkValidConnections()) {
                                error("User timed out", "Ending the game");
                                endGame();
                                return;
                            }

                            currentTime = System.currentTimeMillis();
                            game.entityHandler.teamsList = game.teamsList;
                            game.cyclePhysics();
                            if ((currentTime - game.timeStarted) < game.settings.getTimeLimit()) {
                                broadCastEntityHandler("update", game.entityHandler);
                                // reset heldLength
                                heldLength = new double[MAX_USERS];
                            } else {
                                // the game should end as time-up
                                endGame();
                            }
                        }
                    }
                }
            }

            public void endGame() {
                log("Game", "TIME UP");

                broadCastEntityHandler("endUpdate", game.entityHandler);

                for (SocketChannel socketChannel : channelArr) {
                    if (socketChannel == null)
                        continue;
                    try {
                        socketChannel.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                setupGame();
            }


            /**
             * Sends an entity handler to every connected user.
             * @param type The message type.
             * @param entityHandler The EntityHandler.
             */
            private void broadCastEntityHandler(String type, EntityHandler entityHandler) {
                // Set up base message
                Message message = new Message(type);
                message.entityHandler = entityHandler;
                message.tankToHeldLength = createTankHeldLengthMap();
                message.timeRemaining = lobbySettings.getTimeLimit() - (currentTime - game.timeStarted);

                if (type.equals("initialUpdate")) {
                    message.lobbySettings = lobbySettings;
                    message.tankToNameMap = game.tankToNameMap;
                    message.teamsList = game.teamsList;
                    message.timeRemaining = lobbySettings.getTimeLimit();
                }

                log("Broadcast", message);

                // Loop over all channels
                for (SocketChannel channel : channelArr) {
                    if (channel == null)
                        continue;

                    // Send the ID of the lastProcessed packet from client
                    // Send the tankID too :)
                    int id = userIDMap.get(channel);
                    message.packetNum = lastProcessedInput[id];

                    if (type.equals("initialUpdate")) {
                        String name = game.IDTankMap.get(id);
                        message.stringData = name;
                        message.teamID = game.IDTeamMap.get(name);
                    }

                    sendMessage(channel, message);
                }
            }
        };

        executor.scheduleAtFixedRate(updater, 0, updatePeriod, TimeUnit.MILLISECONDS);

        log("Updater Started w/ period: " + updatePeriod);
    }

    /**
     * This method is checking when the last time a message was received from the users.
     * If it was more than N seconds, then end the game.
     * @return true if all connections are still valid
     */
    private boolean checkValidConnections() {
        for (long time : lastHeard)
            if (time != -1 && System.currentTimeMillis() - time > 5000)
                return false;
        return true;
    }

    /**
     * Create a map of tank names to their latest held length.
     * @return a map of tank names to their latest held length.
     */
    private HashMap<String, Double> createTankHeldLengthMap() {
        HashMap<String, Double> tankToHeldFiredLength = new HashMap<>();
        for (int i = 0; i < heldLength.length; i++)
            tankToHeldFiredLength.put(game.IDTankMap.get(i), heldLength[i]);

        return tankToHeldFiredLength;
    }

    /**
     * Makes the selector repeatedly check for new messages.
     * @throws IOException If the server's selector fails to select, there is an issue so throws an exception.
     */
    private void startListener() throws IOException {
        while (!closed) {
            // wait for events
            this.selector.select();

            // work on selected keys
            Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();

                // this is necessary to prevent the same key from coming up again the next time around.
                keys.remove();

                if (!key.isValid())
                    continue;

                if (key.isAcceptable())
                    this.accept(key);

                if (key.isReadable())
                    this.read(key);
            }
        }
    }

    /**
     * Accepts a new key to another client.
     * @param key The key which is being accepted.
     * @throws IOException If the accept failed, we throw an exception.
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        log(channel, "Connected.");

        // register channel with selector for further IO
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    /**
     * Reads data from a client.
     * @param key The key to read from.
     * @throws IOException If the read failed, we throw an exception.
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1000000);
        int numRead = -1;
        numRead = channel.read(buffer);

        // -1 means the read broke
        if (numRead == -1) {
            log(channel, "Connection closed by client");
            numberConnected--;
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = buffer.array();

        try {
            Message message = SerializationUtils.deserialize(data);
            processMessage(channel, message);
        } catch (SerializationException e) {
            error("Serialisation Exception");
            e.printStackTrace();
        } catch (Exception e) {
            error("Exception from process");
            e.printStackTrace();
        }

        buffer.clear();
    }

    /**
     * We need to take the message from the client and update the server accordingly.
     * @param channel The client's channel.
     * @param message The message.
     */
    private void processMessage(SocketChannel channel, Message message) {
        log(channel, "Received: " + message);
        switch (message.type) {
            case "register":
                // Set up server information
                userIDMap.put(channel, idNum);
                channelArr[idNum] = channel;
                numberConnected++;
                lastProcessedInput[idNum] = message.packetNum;
                String username = message.name;
                // If the username is already in the map, give a different username
                if (idNameMap.values().contains(username))
                    username += "1";

                int i = 2;
                while (idNameMap.values().contains(username))
                    username = username.substring(0, -2) + i++;

                idNameMap.put(idNum, username);

                // Reply to client with its ID
                Message reply = new Message("accepted");
                if (idNum == 0)
                    reply.stringData = "leader";

                reply.name = username;

                sendMessage(channel, reply);
                idNum++;
                break;
            case "lobby":
                if (!userIDMap.containsKey(channel)) {
                    error(channel, "This channel does not have an associated ID.");
                    break;
                }

                if (lobbySettingsMessage)
                    break;

                this.lobbySettings = message.lobbySettings;
                this.forced_user_count = lobbySettings.getNumberOfPlayers();
                lobbySettingsMessage = true;
                break;
            case "update":
                if (!userIDMap.containsKey(channel)) {
                    error(channel, "This channel does not have an associated ID.");
                    break;
                }

                int id = userIDMap.get(channel);

                // Get the data
                EntityHandler eh = message.entityHandler;
                Integer[] keysPressed = message.keysPressed;
                boolean shotFired = message.shotFired;
                double mouseHeldTime = message.mouseHeldTime;
                double currentMouseHeldTime = message.currentMouseHeldTime;

                heldLength[id] = currentMouseHeldTime;
                if (shotFired)
                    shotFiredLength[id] = currentMouseHeldTime;

                // Update the Game
                game.cycle(id, eh, keysPressed, mouseHeldTime);

                // Record the ID of the packet
                lastHeard[id] = System.currentTimeMillis();
                lastProcessedInput[id] = message.packetNum;
                break;
            case "debug":
                log(channel, message);
                Message msg = new Message("debug");
                msg.stringData = "This is response from server";
                sendMessage(channel, msg);
                break;
            default:
                break;
        }

    }

    /**
     * Writes to a channel with a given message.
     * @param channel The channel to write to.
     * @param message The message to write.
     */
    private void sendMessage(SocketChannel channel, Message message) {
        ByteBuffer bytes = ByteBuffer.wrap(SerializationUtils.serialize(message));
        try {
            log(channel, "Sending: " + message);
            channel.write(bytes);
        } catch (IOException e) {
            error(channel, "Failed to send message: " + message);
//            count_failed++;
            e.printStackTrace();
//            if(count_failed > 4) {
//                for (SocketChannel socketChannel : channelArr) {
//                    if (socketChannel == null)
//                        continue;
//
//                    try {
//                        socketChannel.close();
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//
//                setupGame();
//            }
        }
    }

    /**
     * Output to the server's console. Takes a channel to add the address before the message.
     * Calls .getRemoteAddress() on the channel and .toString() on the provided object.
     * <p> EG:
     * <p>{@code [/192.168.0.15:2131] This is an example message}
     * @param channel The channel.
     * @param message The message to write.
     */
    public static void log(SocketChannel channel, Object message) {
        if (message != null) {
            try {
                System.out.println("[" + channel.getRemoteAddress() + "] " + message.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Output to the server's console. Takes a name to add the address before the message. Calls .toString() on the provided objects.
     * <p> EG:
     * <p>{@code [XYZ] This is an example message}
     * @param name The name.
     * @param message The message to write.
     */
    public static void log(Object name, Object message) {
        if (message != null && name != null)
            System.out.println("[" + name.toString() + "] " + message.toString());
    }

    /**
     * Output to the server's console. Calls .toString() on the provided object.
     * <p> EG:
     * <p>{@code This is an example message}
     * @param message The message to write.
     */
    public static void log(Object message) {
        if (message != null)
            System.out.println(message.toString());
    }

    /**
     * Output to the server's console as an error. Takes a channel to add the address before the message.
     * Calls .getRemoteAddress() on the channel and .toString() on the provided object.
     * <p> EG:
     * <p>{@code [/192.168.0.15:2131] This is an example message}
     * @param channel The channel.
     * @param message The message to write.
     */
    public static void error(SocketChannel channel, Object message) {
        if (message != null) {
            try {
                System.err.println("[" + channel.getRemoteAddress() + "] " + message.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Output to the server's console as an error. Takes a name to add the address before the message. Calls .toString() on the provided objects.
     * <p> EG:
     * <p>{@code [XYZ] This is an example message}
     * @param name The name.
     * @param message The message to write.
     */
    public static void error(Object name, Object message) {
        if (message != null && name != null)
            System.err.println("[" + name.toString() + "] " + message.toString());
    }

    /**
     * Output to the server's console as an error. Calls .toString() on the provided object.
     * <p> EG:
     * <p>{@code This is an example message}
     * @param message The message to write.
     */
    public static void error(Object message) {
        if (message != null)
            System.err.println(message.toString());
    }

    @Override
    public void run() {
        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        closed = true;
    }
}
