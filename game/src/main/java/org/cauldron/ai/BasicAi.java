package org.cauldron.ai;

import org.cauldron.entity.EntityHandler;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.renderer.animations.DriveAnim;
import org.joml.Vector2f;

public class BasicAi {

    public Vector2f ranPos = new Vector2f(20, 20);
    private float pl;
    private float pr;
    private String id;

    public BasicAi() {
        pl = 0.5f;
        pr = 0.5f;
    }

    public BasicAi(float pl, float pr) {
        this.pl = pl;
        this.pr = pr;
    }

    public char move() {
        double rand = Math.random();
        if (rand <= pl) {
            if (Math.random() < 0.5) {
                return 'l';
            }
            return 'r';
        } else {
            if (Math.random() < 0.5) {
                return 'r';
            }
            return 'l';
        }
    }

    public void shoot(EntityHandler entityhandler) {

        //entityhandler.playSound(0 + "_tank", 1);
    }

    public boolean isShot() {
        double rand = Math.random();
        return rand <= 1f;
    }

    public void update(float xpos, float ypos) {
        if (Math.random() > 0.9) {
            float x = (float) Math.random() * 1600f;
            float y = (float) Math.random() * (900f - 186f) + 186;
            this.ranPos.x = x;
            this.ranPos.y = y;
        }
    }

    public void updateProbabilities(float xpos, float ypos) {
        if (xpos < 300) {
            setProbabilities(0.4f, 0.6f);
        } else if (xpos < 400) {
            setProbabilities(0.43f, 0.57f);
        } else if (xpos < 500) {
            setProbabilities(0.45f, 0.55f);
        } else if (xpos < 600) {
            setProbabilities(0.47f, 0.53f);
        } else if (xpos < 800) {
            setProbabilities(0.49f, 0.51f);
        } else {
            setProbabilities(0.5f, 0.5f);
        }
    }

    public void setProbabilities(float pl, float pr) {
        this.pl = pl;
        this.pr = pr;
    }

    public float getAngle(float xpos) {

        float angle = (float) (Math.random() * ((85 - 5) + 1)) + 5;
        if (xpos > 800)
            return 90 - angle;

        return angle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static void processAi(char move, String tankID, EntityHandler eh) {
        switch (move) {
            case 'l':
                eh.setDrivingForce(tankID, -10000f);
                break;
            case 'r':
                eh.setDrivingForce(tankID, 10000f);
                break;
            default:
                break;
        }

        if (Math.abs(eh.getEntity(tankID).velocity.x) <= 5) {
            TextureHandler.wipeAnimation(eh.getEntity(tankID));
        } else {
            TextureHandler.setAnimation(eh.getEntity(tankID), new DriveAnim(eh.getEntity(tankID).playerColor));
        }
    }
}