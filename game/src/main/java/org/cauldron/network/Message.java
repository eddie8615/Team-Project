package org.cauldron.network;

import org.cauldron.entity.EntityHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * This class holds all data that may need to be sent across the network. Easy to package up the data.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -3201786932426092210L;
    public String type;                                      // The message type.
    public EntityHandler entityHandler = null;               // An EntityHandler.
    public Integer[] keysPressed = null;                     // The keyboard inputs.
    public boolean shotFired = false;                        // Has the user fired.

    public double mouseHeldTime = -1;                        // Time the mouse has held down for a shot.
    public double currentMouseHeldTime = -1;                 // Time the mouse has held down for.
    public Map<String, Double> tankToHeldLength = null;      // How long the users have held down for.
    public long timeRemaining = -1;
    public double packetNum = -1;                            // The packet number. Used to let the other side know what order things have been sent.

    public String name = null;                               // Name of the user.
    public int teamID = -1;                                  // Holds the team number of the user.
    public Map<String, String> tankToNameMap = null;         // Map of tanks to names.
    public ArrayList<ArrayList<String>> teamsList = null;    // List of players in each team.
    public LobbySettings lobbySettings = null;               // The settings of the initial lobby.
    public String stringData = null;                         // String data can be sent here like tankID.

    /**
     * Create a new message. Relies on the user to assign necessary fields.
     *
     * @param type The message type.
     */
    public Message(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", entityHandler=" + entityHandler +
                ", keysPressed=" + Arrays.toString(keysPressed) +
                ", shotFired=" + shotFired +
                ", mouseHeldTime=" + mouseHeldTime +
                ", packetNum=" + packetNum +
                ", name='" + name + '\'' +
                ", teamID=" + teamID +
                ", tankToNameMap=" + tankToNameMap +
                ", teamsList=" + teamsList +
                ", lobbySettings=" + lobbySettings +
                ", stringData='" + stringData + '\'' +
                '}';
    }
}
