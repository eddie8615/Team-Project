package org.cauldron.renderer;

import java.util.Arrays;

public class Animation {
    protected long lastUpdated;
    protected int interval;
    protected int[] textureQueue;
    protected int textureIndex = 0;
    protected boolean loops = true;
    protected boolean holdTexture = false;

    /**
     * Create an animation with a given time interval and queue of textures
     *
     * @param interval     The time between each frame
     * @param textureQueue The array of texture IDs to cycle through
     */
    public Animation(int interval, int[] textureQueue) {
        lastUpdated = System.currentTimeMillis();
        this.interval = interval;
        this.textureQueue = textureQueue;
    }

    /**
     * Create an animation with a given time interval and queue of textures
     *
     * @param holdTexture  Whether the end texture should be held if the animation doesn't loop
     * @param loops        Whether the animation should loop
     * @param interval     The time between each frame
     * @param textureQueue The array of texture IDs to cycle through
     */
    public Animation(int interval, int[] textureQueue, boolean loops, boolean holdTexture) {
        lastUpdated = System.currentTimeMillis();
        this.interval = interval;
        this.textureQueue = textureQueue;
        this.loops = loops;
        this.holdTexture = holdTexture;
    }

    /**
     * Create an animation with a given time interval.
     * A textureQueue must be set to allow the Animation to cycle.
     *
     * @param interval The time between each frame
     */
    public Animation(int interval) {
        lastUpdated = System.currentTimeMillis();
        this.interval = interval;
    }

    /**
     * Create an animation with a given time interval.
     * A textureQueue must be set to allow the Animation to cycle.
     *
     * @param holdTexture Whether the end texture should be held if the animation doesn't loop
     * @param loops       Whether the animation should loop
     * @param interval    The time between each frame
     */
    public Animation(int interval, boolean loops, boolean holdTexture) {
        lastUpdated = System.currentTimeMillis();
        this.interval = interval;
        this.loops = loops;
        this.holdTexture = holdTexture;
    }

    /**
     * Cycles an Animation based on its attributes
     *
     * @return the texture ID after cycling
     */
    public int cycle() {
        long now = System.currentTimeMillis();
        if (!loops && (textureIndex == textureQueue.length - 1)) {
            if (holdTexture)
                return textureQueue[textureIndex];
            else
                return -1;
        }
        if (now - lastUpdated >= interval) {
            textureIndex = (textureIndex + 1) % textureQueue.length;
            lastUpdated = now;
        }
        return textureQueue[textureIndex];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!Animation.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        final Animation other = (Animation) o;
        return (Arrays.equals(other.textureQueue, textureQueue) || other.textureQueue == textureQueue) && other.interval == interval;
    }

    @Override
    public int hashCode() {
        String str = "";
        for (int i : textureQueue) {
            str += String.valueOf(i);
        }
        str += String.valueOf(interval);
        return Integer.parseInt(str);
    }
}
