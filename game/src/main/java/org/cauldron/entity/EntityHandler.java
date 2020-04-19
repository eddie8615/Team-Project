package org.cauldron.entity;

import org.cauldron.entity.entities.*;
import org.cauldron.game.Physics;
import org.cauldron.game.Terrain;
import org.cauldron.renderer.Renderer;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.animations.ExtraCrateAnim;
import org.cauldron.renderer.animations.RandomCrateAnim;
import org.cauldron.renderer.layers.Layer;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.cauldron.entity.EntityType.*;
import static org.cauldron.renderer.Renderer.COLOR.ORANGE;
import static org.cauldron.renderer.Renderer.COLOR.RED;

public class EntityHandler implements Serializable {

    private static final long serialVersionUID = 7526472295622776127L;
    public int MAX_ENTITIES = 16000;
    public ArrayList<ArrayList<String>> teamsList = new ArrayList<>();
    private HashMap<String, Integer> entityIndex = new HashMap<>();
    private Entity[] entities = new Entity[MAX_ENTITIES];
    public int count = 0;
    public int[] scores = {0, 0};
    public final static float maxPowerUpTime = 1.5f;
    private boolean debugCreation = false;

    /**
     * calls the main physics method and the method that deals with tanks that have a health below 0.
     *
     * @param dt time passed, in milliseconds, since the last call to this method.
     */
    public void updatePhysics(long dt) {
        Physics.updatePhysics(this, dt);
    }

    private int getNextID() {
        for (int i = 0; i < entities.length; i++) {
            if (entities[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public Entity createGround(Terrain map) {
        int num = getNextID();
        if (num == -1)
            return null;
        Entity entity;
        entity = new Ground(num + "_ground", map);
        entities[num] = entity;
        entityIndex.put(entity.name, num);
        return entity;
    }

    /**
     * method used to create a crate. This will contain a power up and then give it to the player that deals the final bit of damage to the crate causing it to break.
     *
     * @return
     */
    public Entity createCrate() {
        System.out.println("creating crate");
        int num = getNextID();
        if (num == -1)
            return null;
        String name = num + "_crate";
        Crate entity = new Crate(name);
        entity.depth = -0.0005f * (num + 1) - 0.0001f; // add 1 because indexing starts at 0
        entities[num] = entity;
        entityIndex.put(name, num);
        applyForce(name, "gravity", 0, Physics.GRAVITY * entity.mass.mass);

        if (debugCreation) {
            System.out.println("created crate");
        }
        if (debugCreation) {
            System.out.println("created crate");
        }

        if (entity.powerUpType == PowerUpType.RANDOM) {
            TextureHandler.setAnimation(entity, new RandomCrateAnim());
        } else if (entity.powerUpType == PowerUpType.EXTRA) {
            TextureHandler.setAnimation(entity, new ExtraCrateAnim());
        }

        return entity;
    }

    public Entity createTank() {
        int num = getNextID();
        if (num == -1)
            return null;
        String name = num + "_tank";
        Entity entity = new Tank(name);
        entity.depth = -0.0005f * (num + 1); // add 1 because indexing starts at 0
        entities[num] = entity;
        entityIndex.put(name, num);
        createTurret(name);

        return entity;
    }

    public Entity createTank(String color) {
        int num = getNextID();
        if (num == -1)
            return null;
        String name = num + "_tank";
        Entity entity = new Tank(name);
        entity.depth = -0.0005f * (num + 1); // add 1 because indexing starts at 0
        entity.playerColor = color;
        entities[num] = entity;
        entityIndex.put(name, num);
        createTurret(name);

        return entity;
    }

    /**
     * creates a turret
     *
     * @param parentId the ID of the tank that this turret should belong to
     * @return the new turret entity, or null if something went wrong
     */
    public Entity createTurret(String parentId) {
        if (getEntity(parentId).type != TANK)
            return null;
        int num = getNextID();
        if (num == -1)
            return null;
        String name = num + "_turret";
        Entity entity = new Turret(name);
        Tank parent = (Tank) getEntity(parentId);
        entity.position.x = 200;
        entity.position.y = 450;
        entity.depth = parent.depth - 0.0002f;
        parent.turret = num;
        ((Turret) entity).playerColor = parent.playerColor;
        entities[num] = entity;
        entityIndex.put(name, num);
        return entity;
    }

    /**
     * creates a projectile
     * <p>
     * checks if the tank it belongs to has any relevant power ups and make the appropriate changes to the projectile.
     *
     * @param tank_id the string ID of the tank that fired the projectile
     * @return the projectile
     */
    private Entity createProjectile(String tank_id) {
        int num = getNextID();
        if (num == -1)
            return null;
        String name = num + "_projectile";
        Projectile projectile = new Projectile(name);
        projectile.parent = entityIndex.get(tank_id);
        Tank tank = (Tank) getEntity(tank_id);
        if (debugCreation) {
            System.out.println("created projectile:" + name);
        }
        projectile.depth = tank.depth - 0.00051f;
        if (tank.hasPowerUp(PowerUpType.STRENGTH)) {
            projectile.damage = 40;
            System.out.println(tank_id + " fired projectile with STRENGTH!");
        }
        if (tank.hasPowerUp(PowerUpType.TARGETING)) {
            projectile.guided = true;
            Vector2d tempPos = getDifferentTank(tank).position;
            projectile.target = new Vector2d(tempPos.x + 35, tempPos.y + 30);
            System.out.println("created guided projectile: target:" + projectile.target);
        }
        entities[num] = projectile;
        entityIndex.put(name, num);
        return projectile;
    }

    /**
     * returns a tank for the projectile to target. This will be different to the tank that fired the projectile and should be on the other team.
     *
     * @param tank the tank that fired the projectile
     * @return a different tank
     */
    private Tank getDifferentTank(Tank tank) {
        int team;
        if (teamsList.get(0).contains(tank.name)) {
            team = 1;
        } else {
            team = 0;
        }
        String name = teamsList.get(team).get(new Random().nextInt(teamsList.get(team).size()));
        return (Tank) getEntity(name);
    }

    /**
     * Creates a particle explosion
     *
     * @param x        x position for the explosion
     * @param y        y position for the explosion
     * @param normal   The angle at which the explosion should direct toward
     * @param size     The number of particles in the explosion - keep this low please,
     *                 there's nothing to stop it tanking performance
     * @param maxSpeed The maximum speed of each particle
     * @param color    The dominant colour of the particles
     */
    public void createExplosion(float x, float y, float normal, int size, float maxSpeed, Renderer.COLOR color) {
        for (int i = 0; i < size; i++) {
            int num = getNextID();
            if (num == -1)
                return;
            String name = num + "_particle";
            Particle particle = new Particle(name, 1000f, i % 2 == 0 ? color : RED);
            particle.position.x = x;
            particle.position.y = y;
            if (debugCreation) {
                System.out.println("created particle at " + x + ", " + y);
            }

            Random random = new Random();
            double speed = random.nextFloat() * maxSpeed;
            double angle = normal + (random.nextFloat() * (random.nextBoolean() ? 1 : -1) * 45f);
            particle.velocity = new Vector2d(speed * Math.cos(Math.toRadians(angle)),
                    speed * Math.sin(Math.toRadians(angle)));

            entities[num] = particle;
            entityIndex.put(name, num);

            applyForce(particle.name, "gravity", 0, 50 * Physics.GRAVITY * particle.mass.mass);
        }
    }

    /**
     * Creates a particle explosion at a point with random directions
     *
     * @param x        x position for the explosion
     * @param y        y position for the explosion
     * @param size     The number of particles in the explosion - keep this low please,
     *                 there's nothing to stop it tanking performance
     * @param maxSpeed The maximum speed of each particle
     * @param color    The dominant colour of the particles
     */
    public void createExplosion(float x, float y, int size, float maxSpeed, Renderer.COLOR color) {
        for (int i = 0; i < size; i++) {
            int num = getNextID();
            if (num == -1)
                return;
            String name = num + "_particle";
            Particle particle = new Particle(name, 1000f, i % 2 == 0 ? color : RED);
            particle.position.x = x;
            particle.position.y = y;
            if (debugCreation) {
                System.out.println("created particle at " + x + ", " + y);
            }

            Random random = new Random();
            double speed = random.nextFloat() * maxSpeed;
            double angle = random.nextFloat() * 360f;
            particle.velocity = new Vector2d(speed * Math.cos(Math.toRadians(angle)),
                    speed * Math.sin(Math.toRadians(angle)));

            entities[num] = particle;
            entityIndex.put(name, num);

            applyForce(particle.name, "gravity", 0, 50 * Physics.GRAVITY * particle.mass.mass);
        }
    }

    public void setEntity(String name, Entity entity) {
        entities[entityIndex.get(name)] = entity;
    }

    public void setEntity(int index, Entity entity) {
        entities[index] = entity;
    }

    public Entity[] getEntities() {
        return entities;
    }

    public Entity getEntity(String name) {
        return entities[entityIndex.get(name)];
    }

    public Entity getEntity(int index) {
        return entities[index];
    }

    public int getEntityID(String name) {
        return entityIndex.get(name);
    }

    public void removeEntity(String name) {
        entities[entityIndex.get(name)] = null;
        entityIndex.remove(name);
    }

    public Entity setPos(String entityID, float x, float y, float z) {
        Entity entity = getEntity(entityID);
        entity.position.x = x;
        entity.position.y = y;
        entity.depth = z;
        return entity;
    }

    public Entity setPos(String entityID, float x, float y) {
        Entity entity = getEntity(entityID);
        entity.position.x = x;
        entity.position.y = y;
        return entity;
    }

    public Entity setRotation(String entityID, float r) {
        Entity entity = getEntity(entityID);
        entity.rotation = r;
        return entity;
    }

    public Entity setScale(String entityID, float x, float y) {
        Entity entity = getEntity(entityID);
        entity.shape.width = x;
        entity.shape.height = y;
        return entity;
    }

    public Entity applyForce(String entityID, String name, float x, float y) {
        Entity entity = getEntity(entityID);
        entity.forces.put(name, new Vector2d(x, y));
        setEntity(entityID, entity);
        return entity;
    }

    /**
     * used to set the driving force of a tank, mainly for player movement.
     *
     * @param entityID  the tank entityID
     * @param magnitude the force magnitude
     * @return the tank entity
     */
    public Entity setDrivingForce(String entityID, float magnitude) {
        Entity entity = getEntity(entityID);
        entity.drivingForce = magnitude;
        setEntity(entityID, entity);
        return entity;
    }

    public Entity removeForce(String entityID, String name) {
        Entity entity = getEntity(entityID);
        entity.forces.remove(name);
        setEntity(entityID, entity);
        return entity;
    }

    public Entity setPos(int index, float x, float y, float z) {
        Entity entity = getEntity(index);
        entity.position.x = x;
        entity.position.y = y;
        entity.depth = z;
        setEntity(index, entity);
        return entity;
    }

    public Entity setRotation(int index, float r) {
        Entity entity = getEntity(index);
        entity.rotation = r;
        setEntity(index, entity);
        return entity;
    }

    public Entity setScale(int index, float x, float y) {
        Entity entity = getEntity(index);
        entity.shape.width = x;
        entity.shape.height = y;
        setEntity(index, entity);
        return entity;
    }

    public Entity applyForce(int index, String name, float x, float y) {
        Entity entity = getEntity(index);
        entity.forces.put(name, new Vector2d(x, y));
        setEntity(index, entity);
        return entity;
    }

    public Entity setDrivingForce(int index, float magnitude) {
        Entity entity = getEntity(index);
        entity.drivingForce = magnitude;
        setEntity(index, entity);
        return entity;
    }

    public Entity removeForce(int index, String name) {
        Entity entity = getEntity(index);
        entity.forces.remove(name);
        setEntity(index, entity);
        return entity;
    }

    public void updateData(EntityHandler data) {
        // TODO, take the data and update the bodies and such
        for (Entity b : data.entities) {
            if (b == null)
                continue;

            System.out.println("id:" + b.name);
            setEntity(b.name, b);
        }

        this.entityIndex = data.entityIndex;
        this.count = data.count;
        this.MAX_ENTITIES = data.MAX_ENTITIES;
    }

    /**
     * @param name the String ID of the entity we want to sit on ground level
     * @param map  the map, so we can get the Y value
     */
    public void constrainEntityToGround(String name, Terrain map) {
        if (entityIndex.get(name) == null)
            return;

        if (getEntity(name).type != EntityType.TANK && getEntity(name).type != EntityType.CRATE)
            return;
        Entity tank = getEntity(name);
        float xPosLeft = (float) tank.position.x;
        if (xPosLeft + tank.shape.width > 1600)
            xPosLeft = 1600 - tank.shape.width;
        if (xPosLeft < 0)
            xPosLeft = 0;
        float xPosRight = xPosLeft + tank.shape.width;
        float newYPosLeft = map.getYVal(xPosLeft);
        float newYPosRight = map.getYVal(xPosRight);
        double angle = Math.atan((double) (newYPosRight - newYPosLeft) / (double) (xPosRight - xPosLeft));
        setPos(name, xPosLeft, newYPosLeft + 5, tank.depth);
        setRotation(name, (float) Math.toDegrees(angle));
    }

    public void constrainTurret(String parent, Vector2f cursorPos) {
        // TODO move constrain alternate turret into here
        if (getEntity(parent).type != TANK)
            return;
        Tank tank = (Tank) getEntity(parent);
        tank.shape.height -= tank.shape.height / 6f; // Change the tank to be lower, so turret is recessed
        Vector2d[] corners = tank.getCornerCoords();
        tank.shape.height = tank.shape.height * (6f / 5f);
        float turretX = (float) (corners[1].x + (corners[2].x - corners[1].x) / 2f);
        float turretY = (float) (corners[1].y + (corners[2].y - corners[1].y) / 2f);
        setPos(tank.turret, turretX, turretY, getEntity(tank.turret).depth);
        // Avoid div by 0
        if ((int) (cursorPos.x - turretX) == 0)
            return;
        // Avoid moving the turret when hovering over Nuklear windows
        if (cursorPos.equals(new Vector2f(-1, -1)))
            return;
        float turretAngle = (float) Math.toDegrees(Math.atan((cursorPos.y - turretY) / (cursorPos.x - turretX))) - 90;
        if (cursorPos.x < turretX)
            turretAngle = 180 + turretAngle;
        if (tank.rotation - turretAngle + 90 < -5)
            setRotation(tank.turret, tank.rotation + 95);
        else if (tank.rotation - turretAngle + 90 > 185)
            setRotation(tank.turret, tank.rotation - 95);
        else
            setRotation(tank.turret, turretAngle);
    }

    /**
     * this function will constrain all turrets to the tank bodies, although will only update the rotation of the tankID passed
     *
     * @param clientTankID the tank id to rotate
     * @param cursorPos    the curson pos to rotate the tankID's turret to
     */
    public void constrainTurrets(String clientTankID, Vector2f cursorPos) {
        Entity[] entities = getEntities();
        for (Entity e : entities) {
            if (e == null)
                continue;
            if (e.type != EntityType.TANK)
                continue;

            if (e.name.equals(clientTankID))
                constrainTurret(clientTankID, cursorPos);
            else
                constrainAlternateTurret(e.name);
        }
    }

    /**
     * takes a tankID and constrains its turret, no rotation change made
     *
     * @param parent the turret's tank
     */
    public void constrainAlternateTurret(String parent) {
        if (getEntity(parent).type != EntityType.TANK)
            return;
        Tank tank = (Tank) getEntity(parent);
        tank.shape.height -= tank.shape.height / 6f; // Change the tank to be lower, so turret is recessed
        Vector2d[] corners = tank.getCornerCoords();
        tank.shape.height = tank.shape.height * (6f / 5f);
        float turretX = (float) (corners[1].x + (corners[2].x - corners[1].x) / 2f);
        float turretY = (float) (corners[1].y + (corners[2].y - corners[1].y) / 2f);
        setPos(tank.turret, turretX, turretY, getEntity(tank.turret).depth);
    }

    public void constrainAiTurret(String parent, Vector2f cursorPos) {
        if (getEntity(parent).type != EntityType.TANK)
            return;
        Tank tank = (Tank) getEntity(parent);
        tank.shape.height -= tank.shape.height / 6f; // Change the tank to be lower, so turret is recessed
        Vector2d[] corners = tank.getCornerCoords();
        tank.shape.height = tank.shape.height * (6f / 5f);
        float turretX = (float) (corners[1].x + (corners[2].x - corners[1].x) / 2f);
        float turretY = (float) (corners[1].y + (corners[2].y - corners[1].y) / 2f);
        setPos(tank.turret, turretX, turretY, getEntity(tank.turret).depth);
        // Avoid div by 0
        if ((int) (cursorPos.x - turretX) == 0)
            return;
        // Avoid moving the turret when hovering over Nuklear windows
        if (cursorPos.equals(new Vector2f(-1, -1)))
            return;
        float turretAngle = (float) Math.toDegrees(Math.atan((cursorPos.y - turretY) / (cursorPos.x - turretX))) - 90;
        if (cursorPos.x < turretX)
            turretAngle = 180 + turretAngle;
        if (tank.rotation - turretAngle + 90 < -5)
            setRotation(tank.turret, tank.rotation - 50);
        else if (tank.rotation - turretAngle + 90 > 185)
            setRotation(tank.turret, tank.rotation + 50);
        else
            setRotation(tank.turret, turretAngle);
    }

    /**
     * the public method used to have a tank fire a projectile (not createProjectile, which is private).
     * <p>
     * Checks if the tank has the cluster power up and will fire multiple projectiles if this is the case.
     *
     * @param tankID   the tank that fired the projectile
     * @param timeHeld the time held of the mouse before it was released, aka the power that the projectile was fired with.
     * @return (one of, or) the new projectile(s) made
     */
    public Entity fireProjectile(String tankID, float timeHeld) {
        if (timeHeld < 0.5f)
            timeHeld = 0.5f;
        else if (timeHeld > maxPowerUpTime)
            timeHeld = maxPowerUpTime;
        timeHeld *= 2;
        Tank tankEntity = (Tank) getEntity(tankID);
        int shots = 1;
        if (tankEntity.hasPowerUp(PowerUpType.CLUSTER)) {
            shots = 25;
        }
        Projectile proj = null;
        Entity turret;
        double rand1 = 0;
        for (int i = 0; i < shots; i++) {
            proj = (Projectile) createProjectile(tankID);
            turret = getEntity(tankEntity.turret);
            if (i > 0) {
                rand1 = (Math.random() * 6 - 3);
            }
            proj.velocity = new Vector2d(500 * timeHeld * Math.cos(Math.toRadians(turret.rotation + 90 + rand1)),
                    500 * timeHeld * Math.sin(Math.toRadians(turret.rotation + 90 + rand1)));
            proj.position = turret.getCornerCoords()[1];
            if (i > 0) {
                proj.position.x = proj.position.x + (Math.random() * 6 - 3);
                proj.position.y = proj.position.y + (Math.random() * 6 - 3);
            }

            if (!proj.guided) {
                applyForce(proj.name, "gravity", 0, 100 * Physics.GRAVITY * proj.mass.mass);
            }

            tankEntity.lastFired = System.currentTimeMillis();
            tankEntity.stats.shotsFired += 1;
            setEntity(tankID, tankEntity);
        }
        return proj;
    }

    /**
     * removed particles from the particle effects that have timed out.
     */
    public void removeDeadParticles() {
        for (Entity e : entities) {
            if (e == null)
                break;
            if (e.type == PARTICLE && System.currentTimeMillis() >= e.creationTime + ((Particle) e).lifespan) {
                removeEntity(e.name);
                // System.out.println("removed entity: " + e.name);
            }
        }
    }

    /**
     * Removes projectiles that are outside of the screen or if they are a guided projectile 10 seconds after creation.
     */
    public void removeLostProjectiles() {
        for (Entity e : entities) {
            if (e != null) {
                if (e.type == PROJECTILE) {
                    if (((Projectile) e).guided) {
                        if (e.position.x < -50 || e.position.x > Layer.WIDTH + 50 || e.position.y < -50 || System.currentTimeMillis() - e.creationTime > 10000) {
                            removeEntity(e.name);
                            System.out.println("removed entity: " + e.name + " (timed out)");
                        }
                    } else {
                        if (e.position.x < 0 || e.position.x > Layer.WIDTH || e.position.y < 0) {
                            removeEntity(e.name);
                            System.out.println("removed entity: " + e.name);
                        }
                    }
                }
            }
        }
    }

    public void constrainTanks(Terrain map) {
        constrainEntityType(map, TANK);
    }

    public void constrainCrates(Terrain map) {
        constrainEntityType(map, CRATE);
    }

    /**
     * loops through all the entities and if they match enType, then it will change its Y position to be on ground level
     *
     * @param map    map, to be able to find ground level
     * @param enType the type of entity to constrain
     */
    public void constrainEntityType(Terrain map, EntityType enType) {
        for (Entity e : entities) {
            if (e != null) {
                if (e.type == enType) {
                    constrainEntityToGround(e.name, map);
                }
            }
        }
    }

    /**
     * @param tankID the String ID of the tank
     * @return the health of the tank, or -1 if that ID does not lead to a tank.
     */
    public int getTankHealth(String tankID) {
        Entity maybeTank = getEntity(tankID);

        if (maybeTank != null) {
            if (maybeTank.type == TANK) {
                Tank tank = (Tank) maybeTank;
                return tank.health;
            }
        }
        return -1;
    }

    /**
     * applies effect of power up that a tank has just got by breaking a crate.
     * <p>
     * For certain power ups this method applies the power up to all other tank. This also handles power ups such as the random power up or the one that spawns in three extra crates.
     *
     * @param tank        the tank that broke the crate to apply the power up
     * @param powerUpType the power up that was in the crate
     */
    public void applyPowerUp(Tank tank, PowerUpType powerUpType) {
        if (powerUpType == PowerUpType.REVERSEOTHERS) {
            applyPowerUpToOthers(tank, PowerUpType.REVERSE);
        } else if (powerUpType == PowerUpType.DAMAGE) {
            applyPowerUpToOthers(tank, PowerUpType.DAMAGE);
        } else if (powerUpType == PowerUpType.RANDOM) {
            // does have a risk of applying the random power up again but it's unlikely this will happen very often.
            applyPowerUp(tank, PowerUpType.values()[new Random().nextInt(PowerUpType.values().length)]);
        } else if (powerUpType == PowerUpType.EXTRA) {
            createCrate();
            createCrate();
            createCrate();
        } else {
            tank.applyPowerUp(powerUpType);
        }
    }

    /**
     * will apply the power up passed to all tanks apart from the tank in question.
     *
     * @param tank
     * @param powerUpType
     */
    private void applyPowerUpToOthers(Tank tank, PowerUpType powerUpType) {
        Tank other;
        for (Entity e : entities) {
            if (e != null) {
                if (e.type == TANK) {
                    other = (Tank) e;
                    if (tank != other) {
                        other.applyPowerUp(powerUpType);
                    }
                }
            }
        }
    }
}
