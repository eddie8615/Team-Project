package org.cauldron.renderer;

import org.cauldron.renderer.layers.BackdropLayer;
import org.cauldron.renderer.layers.Layer;

import java.util.Arrays;
import java.util.LinkedList;

public class BackdropScroller {
    BackdropLayer bl;
    float scale = 1;
    int timeInterval = 500;
    private long lastUpdated = System.currentTimeMillis();

    /**
     * Create an instance of BackdropScroller with a given BackdropLayer, scale and timeInterval
     *
     * @param bl           The layer you wish to scroll (must contain 3 images)
     * @param scale        The amount you'd like the images to scroll every tick
     * @param timeInterval The time between ticks
     */
    public BackdropScroller(BackdropLayer bl, float scale, int timeInterval) {
        bl.backdrops = cloneMovers(bl.backdrops);
        this.bl = bl;
        this.scale = scale;
        this.timeInterval = timeInterval;
    }

    private Backdrop[] cloneMovers(Backdrop[] backdrops) {
        Backdrop bg = backdrops[backdrops.length - 2];
        Backdrop bgClone = new Backdrop(bg);
        bgClone.x = Layer.WIDTH + 100;
        Backdrop fg = backdrops[backdrops.length - 3];
        Backdrop fgClone = new Backdrop(fg);
        fgClone.x = Layer.WIDTH + 100;
        LinkedList<Backdrop> newList = new LinkedList<>(Arrays.asList(backdrops));
        newList.add(backdrops.length - 2, bgClone);
        newList.add(backdrops.length - 3, fgClone);
        Backdrop[] newArray = new Backdrop[newList.size()];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = newList.get(i);
        }
        return newArray;
    }

    /**
     * Set the scale to modify the amount the layers move every backdrop tick
     *
     * @param scale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Set the time interval for a backdrop tick
     *
     * @param timeInterval
     */
    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    /**
     * Cycle a 3 layer backdrop where the furthest away image remains static,
     * the middle image moves slowly and the front-most image moves fastest.
     *
     * @return the BackdropLayer held in this BackdropScroller
     */
    public BackdropLayer cycle() {
        long curTime = System.currentTimeMillis();
        if (curTime - lastUpdated < timeInterval)
            return bl;
        for (int i = bl.backdrops.length - 5; i < bl.backdrops.length - 1; i++) {
            if (bl.backdrops[i].x < -Layer.WIDTH)
                bl.backdrops[i].x = Layer.WIDTH + 200;
        }
        Backdrop bg = bl.backdrops[bl.backdrops.length - 2];
        Backdrop bgClone = bl.backdrops[bl.backdrops.length - 3];
        Backdrop fg = bl.backdrops[bl.backdrops.length - 4];
        Backdrop fgClone = bl.backdrops[bl.backdrops.length - 5];
        bg.x -= scale / 3f;
        fg.x -= scale;
        bgClone.x -= scale / 3f;
        fgClone.x -= scale;
        lastUpdated = curTime;
        return bl;
    }
}
