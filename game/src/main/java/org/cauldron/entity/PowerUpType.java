package org.cauldron.entity;

/**
 * <p>Power ups in the game.</p>
 * <h2>If adding more:</h2>
 * <ul>
 *   <li>They should have a texture uploaded for when they are in a crate (although a default one will be used if nothing is added to TextureHandler)</li>
 *   <li>Bars and PowerUpInfo classes should be updated to include their icon as an nk_image (again, default one will be used if this isn't done)</li>
 *   <li>The power up should do something, you can check if it is applied to a tank with tank.hasPowerUp(powerUpType)</li>
 * </ul>
 */
public enum PowerUpType {
    STRENGTH, HEAL, SHIELD, CLUSTER, RANDOM, SPEED, REVERSE, REVERSEOTHERS, DAMAGE, EXTRA, TARGETING
}
