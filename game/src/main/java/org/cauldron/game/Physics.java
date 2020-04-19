package org.cauldron.game;

import org.cauldron.entity.Entity;
import org.cauldron.entity.EntityHandler;
import org.cauldron.entity.PowerUpType;
import org.cauldron.entity.components.Collision;
import org.cauldron.entity.components.EntityPair;
import org.cauldron.entity.entities.Crate;
import org.cauldron.entity.entities.Projectile;
import org.cauldron.entity.entities.Tank;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.layers.Layer;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.cauldron.entity.EntityType.*;
import static org.cauldron.renderer.Renderer.COLOR.ORANGE;

public class Physics {
    public static final float GRAVITY = -9.81f;

    private static EntityHandler entityHandler;

    /**
     * @param eh EntityHandler to process
     * @param dt delta time or change in time
     */
    public static void updatePhysics(EntityHandler eh, double dt) { // dt being delta time or change in time
        entityHandler = eh;
        Entity[] entities = entityHandler.getEntities();

        // -- Deal with collisions
        // Borad phase
        Set<EntityPair> maybeCollidingPairs = getMaybeCollidingPairs(entities);
        // Narrow phase - comparing two objects at each time
        Entity a, b;
        for (EntityPair pair : maybeCollidingPairs) {
            a = pair.A;
            b = pair.B;
            Collision collision = colliding(a, b);
            if (collision.colliding) {
                Entity[] resolvedCol = resolveCollision(a, b, collision.colNormal);
                if (resolvedCol == null)
                    continue;
            }
        }

        // -- step objects
        //update object velocities and positions according to their force and mass
        for (Entity entity : entities) {
            // Simplistic Euler
            // acceleration is calculated by force divided my mass; or inverse force multiplied by mass
            if (entity == null || entity.type == GROUND || entity.type == TURRET)
                continue;
            if (entity.type != PROJECTILE && entity.type != PARTICLE) {
                entity.updateNormal();
                entity.updateFriction();
            }
            if (entity.type == PROJECTILE) {
                if (((Projectile) entity).guided) {
                    guideMissile((Projectile) entity, dt);
                }
            }
            Vector2d newPos = new Vector2d(entity.position).add(new Vector2d(entity.velocity).mul((float) (dt / (double) 1000f)));
            if (entity.type != PROJECTILE || !((Projectile) entity).guided) { // we don't want guided missiles to be effected by gravity
                entity.velocity.add(new Vector2d(entity.getResultant()).mul((float) ((double) 1f / entity.mass.mass * dt / (double) 1000f)));
            }
            if (!outOfPlay(newPos, entity) || entity.type != TANK)
                entity.position.set(newPos);
            else
                entity.velocity = new Vector2d(0, 0);
        }

        // -- game logic
        // move tanks on death and update time alive.
        for (Entity entity : entities) {
            if (entity != null && entity.type == TANK) {
                tankGameLogic((Tank) entity, eh);
            }
        }
    }

    private static void tankGameLogic(Tank tank, EntityHandler eh) {
        if (tank.health <= 0) {
            dealWithDeadTank(tank, eh);
        } else {
            if (tank.stats.currentTimeAlive() > tank.stats.longestTimeAlive) {
                tank.stats.longestTimeAlive = tank.stats.currentTimeAlive();
            }
        }
    }

    /**
     * deals with tanks that have a health of 0 or below.
     * it will clear their powerups, move it to a random x position, create an explosion of where the tank was and then update the score for the other team.
     *
     * @param tank the tank
     * @param eh   the EntityHandler
     */
    private static void dealWithDeadTank(Tank tank, EntityHandler eh) {
        tank.lives -= 1;
        tank.stats.deaths += 1;
        tank.stats.mostRecentDeath = System.currentTimeMillis();
        tank.stats.currentKillStreak = 0;
        tank.health = tank.maxHealth;
        //removes powerups from tank
        tank.appliedPowerUps.clear();
        //sets tank position to random place on screen not within 300 of current location
        int randX = 50 + (int) ((Layer.WIDTH - 100) * Math.random());
        while (Math.abs(randX - tank.position.x) < 300) {
            randX = 50 + (int) ((Layer.WIDTH - 100) * Math.random());
        }
        eh.createExplosion((float) tank.position.x + (float) tank.position.x / 2f, (float) tank.position.y + (float) tank.position.y / 4f, 10, 200, ORANGE);
        tank.position.x = randX;

        // teams scores held in EntityHandler
        for (int i = 0; i < eh.teamsList.size(); i++) {
            if (eh.teamsList.get(i).contains(tank.name)) {
                System.out.println(tank.name);
                if (i == 0) {
                    eh.scores[1]++;
                } else
                    eh.scores[0]++;
            }
        }
    }

    /**
     * for when a player has used the guided projectile powerup when firing a projectile, this is the method that will guide it towards the target.
     * In order for the guiding of the projectile to look natural, like a real guided missile you cannot simply have it travel towards the target. you must instead change the angle that it travelling at so that it appears to turn towards it's target.
     * I tried playing around with the rate of change of the angle and settled at using the modifier: (dt/300)
     * <p>
     * I tested having the missile lock onto the target entity but the game was more fun if you were still able to dodge the guided projectile by moving away from where you were when it fired.
     *
     * @param projectile the projectile to be guided
     * @param dt         change in time (in milliseconds)
     */
    private static void guideMissile(Projectile projectile, double dt) {
        Vector2d origTargetPos = projectile.target;
        Vector2d targetPos = new Vector2d(origTargetPos.x, origTargetPos.y);
        Vector2d currentPos = projectile.position;
        Vector2d currentVelocity = projectile.velocity;
        Vector2d relativeTargetPos = targetPos.sub(currentPos);
        Double cross = (currentVelocity.x * relativeTargetPos.y) - (currentVelocity.y * relativeTargetPos.x);
        if (cross < 0) {
            // turn clockwise
            projectile.velocity = rotate(currentVelocity, -1 * (dt / 300));
        } else {
            // turn anti-clockwise
            projectile.velocity = rotate(currentVelocity, 1 * (dt / 300));
        }
    }

    /**
     * rotates a vector a number of radians
     *
     * @param vec the original vector
     * @param n   the number of radians to rotate the vector
     * @return the new rotated vector
     */
    private static Vector2d rotate(Vector2d vec, double n) {
        double newx = (vec.x * Math.cos(n)) - (vec.y * Math.sin(n));
        double newy = (vec.x * Math.sin(n)) + (vec.y * Math.cos(n));
        return new Vector2d(newx, newy);
    }

    /**
     * tests if an entity is out of play, using Layer.HEIGHT and layer.WIDTH
     *
     * @param pos    the position to test for the entity
     * @param entity we need this entity to be able to get the width of it.
     * @return true if it is out of play, or false if it is still in play.
     */
    private static boolean outOfPlay(Vector2d pos, Entity entity) {
        return (int) (pos.x + entity.shape.width) > Layer.WIDTH || (int) pos.x < 0 || (int) (pos.y + entity.shape.height) > Layer.HEIGHT || (int) pos.y < 0;
    }

    /**
     * When a collision happens this method is ran, here is where we define the gameplay effects of collisions between certain entities
     * <p>
     * e.g. causing damage to a tank hit by a projectile or causing two projectiles to explode if they collide in mid air.
     * <p>
     * This method is ran twice, with a and b swapped the second time so that we can minimise code.
     *
     * @param a      the first entity
     * @param b      the second entity
     * @param normal the collision normal
     */
    private static void resolveSpecificCases(Entity a, Entity b, Vector2d normal) {
        Tank tank;
        //// handle collisions between tank and projectile
        //apply hit to tank if other was projectile
        if (a.type == TANK && b.type == PROJECTILE && !isFriendlyFire(a, b, entityHandler.teamsList)) {
            tank = (Tank) a;
            tank.stats.hitsTaken += 1;
            ((Tank) (entityHandler.getEntity(b.parent))).stats.shotsHitTarget += 1;
            if (tank.hasPowerUp(PowerUpType.SHIELD)) {
                tank.health -= ((Projectile) b).damage / 2;
            } else {
                tank.health -= ((Projectile) b).damage;
            }
            System.out.println(tank.name + " taken " + tank.stats.hitsTaken + " hits! and health: " + tank.health);
            a = tank;
            entityHandler.createExplosion((float) b.position.x, (float) b.position.y, (float) new Vector2d(normal.x, 0).angle(new Vector2d(0, normal.y)), 10, 200, ORANGE);
            b.position.x = -100; //removes the projectile
            ((Projectile) b).guided = false;
            if (tank.health <= 0) {
                ((Tank) (entityHandler.getEntity(b.parent))).stats.killedPlayerUpdate();
            }
        }

        //// handle collisions between ground and projectile
        if (a.type == GROUND && b.type == PROJECTILE) {
            entityHandler.createExplosion((float) b.position.x, (float) b.position.y + 5, 90, 10, 200, ORANGE);
            b.position.x = -100; //removes the projectile
            ((Projectile) b).guided = false;
        }

        if (a.type == GROUND && b.type == PARTICLE) {
            b.position.x = -100; //removes the projectile
        }

        if (a.type == PROJECTILE && b.type == PROJECTILE) {
            //check they have different parents so cluster shots work
            if (((Projectile) a).parent != ((Projectile) b).parent) {
                entityHandler.createExplosion((float) b.position.x, (float) b.position.y + 10, 10, 200, ORANGE);
                a.position.x = -100; //removes the projectile
                ((Projectile) a).guided = false;
                b.position.x = -100; //removes the projectile
                ((Projectile) b).guided = false;
            }
        }

        //// handle collisions between projectile and crate
        Crate crate;
        if (a.type == PROJECTILE && b.type == CRATE) {
            // we will give the tank a power-up. This powerup will apply for a certain duration
            tank = (Tank) entityHandler.getEntity(a.parent);
            tank.stats.shotsHitTarget += 1;
            crate = (Crate) b;
            TextureHandler.wipeAnimation(crate);
            crate.health -= ((Projectile) a).damage;
            if (crate.health <= 0) {
                try {
                    entityHandler.removeEntity(crate.name);//removes the crate
                    entityHandler.applyPowerUp(tank, crate.powerUpType);
                    tank.stats.crates += 1;
                } catch (java.lang.NullPointerException e) {
                    System.out.println(("looks like the crate has already been removed! So I didn't apply the powerup!"));
                }
            }
            entityHandler.createExplosion((float) a.position.x, (float) a.position.y, (float) new Vector2d(normal.x, 0).angle(new Vector2d(0, normal.y)), 10, 200, ORANGE);
            a.position.x = -100; //removes the projectile
            ((Projectile) a).guided = false;

        }
    }

    /**
     * resolves confirmed collisions. Will generally cause the objects to bounce away from each other.
     * <p>
     * Also runs the resolveSpecificCases() method so that game play events can happen
     *
     * @param a      the first entity
     * @param b      the second entity
     * @param normal the collision normal
     * @return the two entities it was passed
     */
    private static Entity[] resolveCollision(Entity a, Entity b, Vector2d normal) {

        //// handle collisions for specific cases
        resolveSpecificCases(a, b, normal);
        resolveSpecificCases(b, a, normal);


        // rv = resultant velocity
        Vector2d rv = new Vector2d(b.velocity).sub(a.velocity);
        //calculate the relative velocity along the normal
        float velAlongNormal = (float) new Vector2d(rv).dot(normal);

        //don't resolve if velocities are separating
        if (velAlongNormal > 0) {
            return null;
        }

        //calculate restitution
        float restitution = min(a.material.restitution, b.material.restitution);

        // calc impulse scalar
        float j = -(1 + restitution) * velAlongNormal;
        j = j / (a.mass.invMass + b.mass.invMass);

        //apply impulse
        Vector2d impulse = new Vector2d(normal).mul(j);
        a.velocity.sub(new Vector2d(impulse).mul(a.mass.invMass));
        b.velocity.add(new Vector2d(impulse).mul(b.mass.invMass));

        // apply correction to fix floating point errors
        Entity[] correctedPos = positionalCorrection(a, b, normal);
        a = correctedPos[0];
        b = correctedPos[1];

        // -- apply friction
        //solve for the tangent vector
        Vector2d tangent = new Vector2d(rv).sub(new Vector2d(normal).mul(new Vector2d(rv).dot(normal)));
        if (!tangent.equals(0f, 0f))
            tangent.normalize();
        //solve for magnitude to apply friction to vector
        float jt = (float) new Vector2d(rv).dot(tangent);
        jt = jt / (a.mass.invMass + b.mass.invMass);
        // use pythagoras to approximate mu given friction coefficients of each body
        float mu = pythagoreanSolve(a.frictionData.staticFriction, b.frictionData.staticFriction);
        // "clamp" magnitude of friction and create impulse vector
        Vector2d frictionImpulse;
        //System.out.println("friction calc for: " + a.name + " & " + b.name);
        if (Math.abs(jt) < j * mu) {
            frictionImpulse = new Vector2d(tangent).mul(jt);
            //System.out.println("friction was clamped at min of:" + frictionImpulse.toString());
        } else {
            float dynamicFriction = pythagoreanSolve(a.frictionData.dynamicFriction, b.frictionData.dynamicFriction);
            frictionImpulse = new Vector2d(tangent).mul(-j * dynamicFriction);
            //System.out.println("friction was dynamic at:" + frictionImpulse.toString());
        }
        //apply friction
        a.velocity.sub(new Vector2d(frictionImpulse).mul(a.mass.invMass));
        b.velocity.add(new Vector2d(frictionImpulse).mul(b.mass.invMass));

        return new Entity[]{a, b};
    }

    /**
     * @param a short side 1
     * @param b short side 2
     * @return the hypothesis of the right angle triangle
     */
    private static float pythagoreanSolve(float a, float b) {
        return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    /**
     * this stops objects sinking into each other and also reduces jittering effects
     * <p>
     * source: https://gamedevelopment.tutsplus.com/tutorials/how-to-create-a-custom-2d-physics-engine-the-basics-and-impulse-resolution--gamedev-6331
     *
     * @param a      first entity
     * @param b      second entity
     * @param normal collision normal
     * @return the entities it was passed.
     */
    private static Entity[] positionalCorrection(Entity a, Entity b, Vector2d normal) {
        final float percent = 0.2f; // usually 20% to 80%
        final float slop = 0.01f; // usually 0.01 to 0.1
        float collisionDepth = getCollisionDepth(a, b);
        Vector2d correction = new Vector2d(normal).mul(max(collisionDepth - slop, 0.0f) / (a.mass.invMass + b.mass.invMass) * percent);
        a.position.sub(new Vector2d(correction).mul(a.mass.invMass));
        b.position.add(new Vector2d(correction).mul(b.mass.invMass));
        return new Entity[]{a, b};
    }

    /**
     * Will check if two bodies are colliding
     *
     * @param a first body
     * @param b second body
     * @return a collision data instance with the collision normal if they are colliding
     */
    public static Collision colliding(Entity a, Entity b) {
        Vector2d[] myCoords;
        Vector2d[] othersCoords;

        if (a.type == TURRET || b.type == TURRET) // no collisions with the turret
            return new Collision(false);
        if (a.type == TANK && b.type == TANK) // No inter-tank collisions
            return new Collision(false);
        if (a.type == GROUND && b.type == TANK) // For now, no collisions with the ground
            return new Collision(false);
        if (a.type == TANK && b.type == GROUND) // For now, no collisions with the ground
            return new Collision(false);
        if (a.type != GROUND && b.type == PARTICLE) // Particles only collide with the ground
            return new Collision(false);
        if (a.type == PARTICLE && b.type != GROUND) // Particles only collide with the ground
            return new Collision(false);

        myCoords = a.getCornerCoords();
        othersCoords = b.getCornerCoords();

        // quick test to see if the entities are within each other
        if (pnpoly(myCoords, othersCoords[0]) || pnpoly(othersCoords, myCoords[0])) {
            return new Collision(true);
        }
        float x, y;
        //tests if the edges of the entities overlap
        for (int i = 0; i < myCoords.length; i++) {
            for (int j = 0; j < othersCoords.length; j++) {
                if (checkLineIntersection(myCoords[i], myCoords[(i + 1) % myCoords.length], othersCoords[j], othersCoords[(j + 1) % othersCoords.length])) {

                    x = (float) (myCoords[i].x - myCoords[(i + 1) % myCoords.length].x); // if this doesnt work then try using othersCoords
                    y = (float) (myCoords[i].y - myCoords[(i + 1) % myCoords.length].y);

                    //noinspection SuspiciousNameCombination
                    Vector2d colNormal = new Vector2d(y, -x); //swapped deliberately https://photos.app.goo.gl/2NxHBitzPghLYVMg7
                    colNormal.normalize();

                    return new Collision(true, colNormal);

                }
            }
        }
        return new Collision(false);
    }


    /**
     * algorithm from http://web.archive.org/web/20141127210836/http://content.gpwiki.org/index.php/Polygon_Collision
     * one edge is a-b, the other is c-d
     *
     * @param a start point on first edge
     * @param b end point on first edge
     * @param c start point on second edge
     * @param d end point on second edge
     * @return true if the lines intersect.
     */
    private static boolean checkLineIntersection(Vector2d a, Vector2d b, Vector2d c, Vector2d d) {
        double det = determinant(new Vector2d(b).sub(a), new Vector2d(c).sub(d));
        double t = determinant(new Vector2d(c).sub(a), new Vector2d(c).sub(d)) / det;
        double u = determinant(new Vector2d(b).sub(a), new Vector2d(c).sub(a)) / det;
        //noinspection RedundantIfStatement
        if ((t < 0) || (u < 0) || (t > 1) || (u > 1) || (det == 0)) {
            return false;
        } else {
            return true; // the intersection point is: a * (1 - t) + t * b
        }
    }

    /**
     * @param c1
     * @param c2
     * @return the determinant of the two vectors.
     */
    private static double determinant(Vector2d c1, Vector2d c2) {
        return c1.x * c2.y - c1.y * c2.x;
    }


    /**
     * code modified from https://stackoverflow.com/a/24365675
     * tests if a single coord is in a convex polygon made of multiple coordinates (verts)
     *
     * @param verts
     * @param testCoord
     * @return true if the vector testCoord is within the convex polygon made of the verts.
     */
    private static boolean pnpoly(Vector2d[] verts, Vector2d testCoord) {
        double testx = testCoord.x;
        double testy = testCoord.y;
        int nvert = verts.length;
        int i, j;
        boolean c = false;
        for (i = 0, j = nvert - 1; i < nvert; j = i++) {
            if (((verts[i].y > testy) != (verts[j].y > testy)) &&
                    (testx < (verts[j].x - verts[i].x) * (testy - verts[i].y) / (verts[j].y - verts[i].y) + verts[i].x))
                c = !c;
        }
        return c;
    }


    /**
     * @param a first body
     * @param b second body
     * @return collision depth for the two bodies
     */
    private static float getCollisionDepth(Entity a, Entity b) {
        //TODO make this return the actual collision depth
        return 10f;
    }

    /**
     * @param entities list of bodies to check
     * @return a bunch of possible collisions and store them in pair structures
     */
    private static Set<EntityPair> getMaybeCollidingPairs(Entity[] entities) {
        Set<EntityPair> maybePairs = new HashSet<EntityPair>();
        for (Entity a : entities) {
            if (a == null)
                continue;
            for (Entity b : entities) {
                if (b == null)
                    continue;
                if (a == b)
                    continue;
                if (checkIfNear(a, b) && !(a == b)) {
                    if (!(maybePairs.contains(new EntityPair(a, b))) && !(maybePairs.contains(new EntityPair(b, a)))) {
                        maybePairs.add(new EntityPair(a, b));
                    }
                }
            }
        }
        return maybePairs;
    }


    public static float getMagnitude(Vector2d v) {
        return (float) Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2));
    }

    public static float getDegrees(Vector2d v) {
        return (float) Math.toDegrees(Math.atan(v.y / v.x));
    }

    public static float getRadians(Vector2d v) {
        return (float) Math.atan(v.y / v.x);
    }

    /**
     * Uses a heuristic to check if two bodies could possibly be colliding based on how near they are to each other.
     * At this point we also exclude if the two objects colliding shouldn't count. e.g. if it is your own projectile.
     *
     * @param a first body
     * @param b second body
     * @return if they are near and could be colliding.
     */
    private static boolean checkIfNear(Entity a, Entity b) {
        //exclude if tank just fired this projectile
        // assume a is the tank and b is a projectile
        if (excludeIfOwnProjectile(a, b)) return false;
        // assume b is the projectile
        if (excludeIfOwnProjectile(b, a)) return false;
        if (excludeIfOwnTurret(a, b)) return false;
        if (excludeIfOwnTurret(b, a)) return false;


        float x1, y1, x2, y2, maxWidth1, maxWidth2, tMaxWidth;
        x1 = (float) a.position.x;
        y1 = (float) a.position.y;
        x2 = (float) b.position.x;
        y2 = (float) b.position.y;
        // max width of shape 1
        maxWidth1 = a.shape.maxWidth;
        // max width of shape 2
        maxWidth2 = b.shape.maxWidth;
        // total max length between 2 points if objects are colliding
        tMaxWidth = maxWidth1 + maxWidth2;
        // checks if objects are near enough to be possibly colliding
        if ((x1 - tMaxWidth <= x2) && (x2 <= x1 + tMaxWidth)) {
            //noinspection RedundantIfStatement
            if ((y1 - tMaxWidth <= y2) && (y2 <= y1 + tMaxWidth)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param tankEntity the entity that might be a tank
     * @param turret     the entity that might be the tank's turret
     * @return true if the turret is owned by the tank
     */
    private static boolean excludeIfOwnTurret(Entity tankEntity, Entity turret) {
        if (tankEntity.type == TANK && turret.type == TURRET) {
            Tank tank = (Tank) tankEntity;
            return tank.turret == Integer.parseInt(turret.name.split("_")[0]);
        }
        return false;
    }

    /**
     * @param tank
     * @param projectile
     * @return true if the projectile was fired by this tank in the last second
     */
    private static boolean excludeIfOwnProjectile(Entity tank, Entity projectile) {
        if (tank.type == TANK && projectile.type == PROJECTILE) {
            Projectile proj = (Projectile) projectile;
            if (proj.parent == entityHandler.getEntityID(tank.name)) {
                //System.out.println(proj.id + " was near own tank soon!");
                return proj.creationTime + 1000 > System.currentTimeMillis();
            }
        }
        return false;
    }

    /**
     * @param tank
     * @param projectile
     * @param teamsList  the list of lists of team members. Each list within this contains the team members of that team
     * @return true if the projectile was fired by a team mate
     */
    private static boolean isFriendlyFire(Entity tank, Entity projectile, ArrayList<ArrayList<String>> teamsList) {
        if (tank.type == TANK && projectile.type == PROJECTILE) {
            Projectile proj = (Projectile) projectile;
            for (ArrayList<String> teamMates : teamsList) {
                if (teamMates.contains(tank.name) && teamMates.contains(entityHandler.getEntity(proj.parent).name))
                    return true;
            }
        }
        return false;
    }
}
