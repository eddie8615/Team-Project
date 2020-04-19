package org.cauldron.network;

import org.joml.Vector2d;

import java.io.Serializable;
import java.util.ArrayList;

import static org.cauldron.network.LobbySettings.GameType.DEFAULT;

public class LobbySettings implements Serializable {
    /**
     * Enums to hold the game types. Used to set default values for each game mode.
     * <ul>
     *     <li><b>Default:</b></li>
     *     Normal free-for-all tank battle.
     *     <li><b>Team:</b></li>
     *     Team based tank battle.
     *     <li><b>Boss:</b></li>
     *     All VS 1 beefed up tank.
     * </ul>
     */
    public enum GameType implements Serializable {
        DEFAULT, TEAM, BOSS, LIVES
    }

    private static final long serialVersionUID = 5479261716496680681L;
    private GameType type = DEFAULT;
    private boolean teams; // are there teams
    private int numberOfTeams; // number of teams
    private boolean teamDamage; // is team damage on?
    private int numberOfPlayers;
    private int aiLevel;
    private int maxHealth;
    private boolean powerups;
    private long timeLimit; // milliseconds
    private ArrayList<Vector2d> map;

    public LobbySettings() {
        updateValues();
    }

    public LobbySettings(GameType type) {
        this.type = type;
        updateValues();
    }

    public void updateType(GameType type) {
        this.type = type;
        updateValues();
    }

    private void updateValues() {
        switch (type) {
            case DEFAULT:
                maxHealth = 100;
                teams = false;
                numberOfTeams = -1;
                numberOfPlayers = 2;
                teamDamage = false;
                powerups = false;
                aiLevel = 2;
                timeLimit = 90 * 1000;
                break;
            case TEAM:
                maxHealth = 100;
                teams = true;
                numberOfTeams = 2;
                numberOfPlayers = 4;
                teamDamage = true;
                powerups = false;
                aiLevel = 2;
                timeLimit = 90 * 1000;
                break;
            case BOSS:
                maxHealth = 100;
                teams = true;
                teamDamage = false;
                numberOfTeams = 2;
                numberOfPlayers = 4;
                powerups = true;
                aiLevel = 2;
                timeLimit = 150 * 1000;
                break;
            default:
                break;
        }
    }

    public ArrayList<Vector2d> getMap() {
        return map;
    }

    public void setMap(ArrayList<Vector2d> map) {
        this.map = map;
    }

    public GameType getType() {
        return type;
    }

    public void setType(GameType type) {
        this.type = type;
    }

    public boolean isTeams() {
        return teams;
    }

    public void setTeams(boolean teams) {
        this.teams = teams;
    }

    public int getNumberOfTeams() {
        return numberOfTeams;
    }

    public void setNumberOfTeams(int numberOfTeams) {
        this.numberOfTeams = numberOfTeams;
    }

    public int getAiLevel() {
        return aiLevel;
    }

    public void setAiLevel(int aiLevel) {
        this.aiLevel = aiLevel;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public boolean isPowerups() {
        return powerups;
    }

    public void setPowerups(boolean powerups) {
        this.powerups = powerups;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public boolean isTeamDamage() {
        return teamDamage;
    }

    public void setTeamDamage(boolean teamDamage) {
        this.teamDamage = teamDamage;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public String toString() {
        return "LobbySettings{" +
                "type=" + type +
                ", teams=" + teams +
                ", teamNumber=" + numberOfTeams +
                ", teamDamage=" + teamDamage +
                ", numberOfPlayers=" + numberOfPlayers +
                ", AILevel=" + aiLevel +
                ", health=" + maxHealth +
                ", powerups=" + powerups +
                ", timeLimit=" + timeLimit +
                '}';
    }
}
