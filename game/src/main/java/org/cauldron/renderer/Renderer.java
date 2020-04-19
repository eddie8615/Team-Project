package org.cauldron.renderer;

import org.cauldron.entity.Entity;
import org.cauldron.renderer.layers.Layer;
import org.cauldron.ui.TexUIElement;
import org.joml.Matrix4f;

import static org.cauldron.entity.EntityType.*;
import static org.lwjgl.opengl.GL32.*;

public class Renderer {

    public enum COLOR {
        BLUE, ORANGE, GREEN, RED, GREY, YELLOW, PURPLE, WHITE, BLACK
    }

    /**
     * Clears the framebuffer.
     */
    public static void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Run through the drawing routines of the given layer.
     *
     * @param layer Layer to draw
     */
    public static void draw(Layer layer) {
        layer.preDraw();
        layer.draw();
        layer.postDraw();
    }

    public static Matrix4f getModelMatrix(Entity e) {
        Matrix4f modelMatrix = new Matrix4f();
        if (e.type == PROJECTILE || e.type == PARTICLE) {
            float velDirection = 0;
            if ((int) e.velocity.x == 0 && (int) e.velocity.y < 0)
                velDirection = 180;
            else if ((int) e.velocity.x == 0)
                velDirection = 0;
            else
                velDirection = (float) Math.toDegrees(Math.atan(e.velocity.y / e.velocity.x)) - 90;
            if (e.velocity.x < 0)
                velDirection = 180 + velDirection;
            modelMatrix.translate((float) (e.position.x), (float) (e.position.y), e.depth);
            modelMatrix.rotate((float) Math.toRadians(velDirection), 0f, 0f, 1f);
            modelMatrix.translate(-e.shape.width / 2f, -e.shape.height / 2f, 0f);
            modelMatrix.scale(e.shape.width, e.shape.height, 1f);
        } else if (e.type == TURRET) {
            modelMatrix.translate((float) (e.position.x), (float) (e.position.y), e.depth);
            modelMatrix.rotate((float) Math.toRadians(e.rotation), 0f, 0f, 1f);
            modelMatrix.translate(-e.shape.width / 2f, 0f, 0f);
            modelMatrix.scale(e.shape.width, e.shape.height, 1f);
        } else {
            modelMatrix.scale(e.shape.width, e.shape.height, 1f);
            modelMatrix.translate((float) (e.position.x / e.shape.width), (float) (e.position.y / e.shape.height), e.depth);
            modelMatrix.scale(1f / e.shape.width, 1f / e.shape.height, 1f);
            modelMatrix.rotate((float) Math.toRadians(e.rotation), 0f, 0f, 1f);
            modelMatrix.scale(e.shape.width, e.shape.height, 1f);
        }
        return modelMatrix;
    }

    public static Matrix4f getModelMatrix(TexUIElement e) {
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.translate(e.getPosition().x, e.getPosition().y, 0f);
        modelMatrix.rotate((float) Math.toRadians(e.getRotation()), 0f, 0f, 1f);
        modelMatrix.translate(-e.getWidth() / 2f, 0f, 0f);
        modelMatrix.scale(e.getWidth(), e.getHeight(), 1f);
        return modelMatrix;
    }

    public static Vertex setColor(Vertex v, Renderer.COLOR color) {
        if (color == null)
            return null;
        switch (color) {
            case BLUE:
                v.setRGB(0f, 102f / 255f, 204f / 255f);
                break;
            case ORANGE:
                v.setRGB(1f, 153f / 255f, 51f / 255f);
                break;
            case RED:
                v.setRGB(1f, 0f, 0f);
                break;
            case GREEN:
                v.setRGB(0f, 204f / 255f, 0f / 255f);
                break;
            case WHITE:
                v.setRGB(1f, 1f, 1f);
                break;
            case BLACK:
                v.setRGB(0f, 0f, 0f);
                break;
            default:
                v.setRGB(1, 1, 1);
        }
        return v;
    }

    public static Vertex[] setColor(Vertex[] vertices, Renderer.COLOR color) {
        for (Vertex v : vertices) {
            setColor(v, color);
        }
        return vertices;
    }

    public static Vertex setOpacity(Vertex v, float opacity) {
        if (opacity > 1)
            opacity = 1;
        else if (opacity < 0)
            opacity = 0;
        v.setRGBA(v.getRGB()[0], v.getRGB()[1], v.getRGB()[2], opacity);
        return v;
    }

    public static Vertex[] setOpacity(Vertex[] vertices, float opacity) {
        for (Vertex v : vertices) {
            setOpacity(v, opacity);
        }
        return vertices;
    }
}
