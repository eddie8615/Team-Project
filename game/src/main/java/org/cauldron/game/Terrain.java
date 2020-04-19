package org.cauldron.game;

import org.cauldron.renderer.Vertex;
import org.cauldron.util.Bezier;
import org.joml.Vector2d;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds and processes all terrain info, and generates a Bezier curve for smooth terrain
 */
public class Terrain implements Serializable {
    private static final long serialVersionUID = -423318671895788071L;
    public int increment;
    public ArrayList<Vector2d> vertices;
    public ArrayList<Vector2d> controlPoints;

    public Terrain(int increment) {
        this.increment = increment;
    }

    public void loadMap(ArrayList<Vector2d> points) {
        controlPoints = points;
        genCurve();
    }

    /**
     * Generates the curve using Bezier algorithm, polling it at increments to lower
     * its resolution and make it drawable.
     */
    private void genCurve() {
        Bezier curve = Bezier.create(controlPoints, 2000 * controlPoints.size(), 0.5);
        List<Vector2d> interpolatedPoints = curve.getInterpolatedPoints();
        vertices = new ArrayList<Vector2d>();

        int i = -1;
        int lastX = -1;
        for (Vector2d v : interpolatedPoints) {
            if (i >= interpolatedPoints.size())
                break;
            if (v.x > 1f && (v.x % increment <= 5f)) {
                int x = (int) (Math.round(v.x * (1f / increment)) / (1f / increment));
                int y = (int) Math.round(v.y);
                if (x == lastX && i != -1) {
                    vertices.set(i, new Vector2d(x, y));
                } else {
                    vertices.add(++i, new Vector2d(x, y));
                }
                lastX = x;
            }
        }
    }

    /**
     * Interpolates between points to find a y value along the terrain, given an x.
     *
     * @param x
     * @return y value or -1 if failed to find
     */
    public float getYVal(float x) {
        double xStart = (int) (Math.floor((double) x * (1f / (double) increment)) / (1f / (double) increment));
        double xEnd = (int) (Math.ceil((double) x * (1f / (double) increment)) / (1f / (double) increment));

        for (int i = 0; i < vertices.size(); i++) {
            if ((int) x == (int) vertices.get(i).x)
                return (float) vertices.get(i).y;

            if (i + 1 >= vertices.size())
                break;

            if (vertices.get(i).x <= x && vertices.get(i + 1).x > x) {
                double yStart = vertices.get(i).y;
                double yEnd = vertices.get(i + 1).y;
                double gradient = (yEnd - yStart) / (xEnd - xStart);
                return (float) (gradient * (double) x + (yStart - gradient * xStart));
            }
        }

        return -1f;
    }
}
