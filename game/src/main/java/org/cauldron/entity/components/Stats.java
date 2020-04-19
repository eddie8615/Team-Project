package org.cauldron.entity.components;

import org.cauldron.entity.PowerUpType;

import java.io.Serializable;

public class Stats implements Serializable {
    private static final long serialVersionUID = 3021456839454040881L;
    public int shotsFired = 0;
    public int shotsHitTarget = 0;
    public int kills = 0;
    public int longestKillStreak = 0;
    public int currentKillStreak = 0;
    public int deaths = 0;
    public int crates = 0;
    public int hitsTaken = 0;
    public long mostRecentDeath = System.currentTimeMillis();
    public long longestTimeAlive = 0; // in milliseconds

    /**
     * @return max 100, min 0. Percentage shots fired that hit the target.
     */
    public int getAccuracy() {
        if (shotsFired == 0) {
            return 0;
        }
        return (100 * shotsHitTarget / shotsFired);
    }

    /**
     * @return current time alive
     */
    public long currentTimeAlive() {
        return System.currentTimeMillis() - mostRecentDeath;
    }

    public void killedPlayerUpdate() {
        kills += 1;
        updateKillStreak();
    }

    private void updateKillStreak() {
        currentKillStreak += 1;
        if (currentKillStreak > longestKillStreak) {
            longestKillStreak = currentKillStreak;
        }
    }
}
