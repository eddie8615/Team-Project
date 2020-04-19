package org.cauldron.entity.components;

import org.cauldron.entity.PowerUpType;

import java.io.Serializable;

/**
 * A data structure that holds a power up type, and the time that it was applied to the tank.
 * <p>
 * A tank has a list of these.
 */
public class AppliedPowerUp implements Serializable {
    private static final long serialVersionUID = 4070835812993663266L;
    public long timeApplied;
    // time left only used for rendering
    public String timeLeft;
    public PowerUpType powerUpType;

    public AppliedPowerUp(PowerUpType type) {
        powerUpType = type;
        timeApplied = System.currentTimeMillis();
    }
}
