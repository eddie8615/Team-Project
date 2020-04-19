package org.cauldron.entity.entities;

import org.cauldron.entity.Entity;
import org.cauldron.entity.PowerUpType;
import org.cauldron.entity.components.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.cauldron.entity.EntityType.TANK;

public class Tank extends Entity {
    private static final long serialVersionUID = 6245845074448880898L;
    public int turret;
    public long lastFired = System.currentTimeMillis();
    public double firingLimit = 0.5; //0.5 seconds
    public Stats stats = new Stats();
    public int maxHealth = 100;
    public int health = maxHealth;
    public ArrayList<AppliedPowerUp> appliedPowerUps = new ArrayList<AppliedPowerUp>();
    private int powerUpDuration = 15000;
    public int lives = 8;

    public Tank(String id) {
        super(id);
        position.x = 200;
        position.y = 450;
        rotation = 0;
        initTank();
    }

    public Tank(String id, float x, float y, float rotation) {
        super(id);
        position.x = x;
        position.y = y;
        super.rotation = rotation;
        initTank();
    }

    private void initTank() {
        shape = new Shape(77, 40);
        depth = -0.001f;
        material = new Material.TankMetal();
        mass = new Mass(50);
        type = TANK;
    }

    /**
     * @return true if the tank has not fired too recently already
     */
    public boolean checkIfCanFire() {
        return ((System.currentTimeMillis() - lastFired) / 1000f > firingLimit);
    }

    /**
     * Applies a power up to a tank.
     * <p>
     * Will check if it is a power up with instant effect such as HEAL or DAMAGE. Else it will remove duplicates of the power up using a lambda expressions before adding this to the list of appliedPowerUps.
     *
     * @param powerUpType the power up to apply to this tank.
     */
    public void applyPowerUp(PowerUpType powerUpType) {
        System.out.println("applying power up: " + powerUpType);
        if (powerUpType == PowerUpType.HEAL) {
            health = maxHealth;
            System.out.println(name + " was healed!");
        } else if (powerUpType == PowerUpType.DAMAGE) {
            health = health / 2;
        } else {
            //removed duplicate powerup
            appliedPowerUps.removeIf(appliedPowerUp -> appliedPowerUp.powerUpType == powerUpType);
            appliedPowerUps.add(new AppliedPowerUp(powerUpType));
        }
    }

    /**
     * uses a Lambda expression to remove applied power ups from the list if they have ran out of time
     */
    private void removePastPowerUps() {
        long currentTime = System.currentTimeMillis();
        appliedPowerUps.removeIf(appliedPowerUp -> currentTime - appliedPowerUp.timeApplied > powerUpDuration);
    }

    /**
     * mainly for rendering
     *
     * @return a copy of the list of current power ups with their remaining times set. Sorted in order of time applied.
     */
    public ArrayList<AppliedPowerUp> getAppliedPowerUps() {
        removePastPowerUps();
        ArrayList<AppliedPowerUp> copyList = new ArrayList<AppliedPowerUp>(appliedPowerUps);
        copyList = setPowerUpTimesLeft(copyList);
        //sorts returned list by time applied
        Collections.sort(copyList, Comparator.comparing((AppliedPowerUp appliedPowerUp) -> appliedPowerUp.timeApplied));
        return copyList;
    }

    private ArrayList<AppliedPowerUp> setPowerUpTimesLeft(ArrayList<AppliedPowerUp> list) {
        float timeDiff;
        for (AppliedPowerUp appliedPowerUp : list) {
            timeDiff = System.currentTimeMillis() - appliedPowerUp.timeApplied;
            appliedPowerUp.timeLeft = formatPowerUpTime(powerUpDuration - timeDiff);
        }
        return list;
    }

    /**
     * formats the float milliseconds left of the power up into a string of the number of seconds with one decimal place.
     * <p>
     * used for rendering the time.
     *
     * @param time
     * @return the string showing how long is left
     */
    private static String formatPowerUpTime(float time) {
        return String.format("%.1f", time / 1000);
    }

    /**
     * checks if a tank currently has a certain power up active.
     *
     * @param powerUpType the power up type to check for
     * @return true if the tank has the power up in question
     */
    public Boolean hasPowerUp(PowerUpType powerUpType) {
        removePastPowerUps();
        for (AppliedPowerUp appliedPowerUp : appliedPowerUps) {
            if (appliedPowerUp.powerUpType == powerUpType) {
                return true;
            }
        }
        return false;
    }

    public float getPowerUpTimeLeft(PowerUpType powerUpType) {
        float timeDiff;
        float timeLeft = -100000;
        float tempTimeLeft;
        for (AppliedPowerUp appliedPowerUp : appliedPowerUps) {
            if (appliedPowerUp.powerUpType == powerUpType) {
                timeDiff = System.currentTimeMillis() - appliedPowerUp.timeApplied;
                tempTimeLeft = powerUpDuration - timeDiff;
                if (tempTimeLeft > timeLeft) {
                    timeLeft = tempTimeLeft;
                }
            }
        }
        return timeLeft;
    }
}
