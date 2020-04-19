package org.cauldron.renderer.layers;

import org.cauldron.renderer.*;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11C.*;

/**
 * BackdropLayer holds all Backdrop information draws it using OpenGL calls.
 */
public class BackdropLayer extends Layer {

    public Backdrop[] backdrops = new Backdrop[]{new Backdrop(-1)};
    private VertexBufferLayout layout = new VertexBufferLayout();
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;

    public BackdropLayer() {
        vertexShaderPath = "game/shaders/vertex.glsl";
        fragmentShaderPath = "game/shaders/fragment.glsl";
        attribLocations.put("in_Position", 0);
        attribLocations.put("in_Color", 1);
        attribLocations.put("in_TextureCoord", 2);
    }

    @Override
    public void setup() {
        shader = new Shader(vertexShaderPath, fragmentShaderPath, attribLocations);

        Vertex[] vertices = new Vertex[]{new Vertex().setXYZ(0, HEIGHT, -0.0002f).setST(0, 0),
                new Vertex().setXYZ(0, 0, -0.0002f).setST(0, 1),
                new Vertex().setXYZ(WIDTH, 0, -0.0002f).setST(1, 1),
                new Vertex().setXYZ(WIDTH, HEIGHT, -0.0002f).setST(1, 0)
        };

        ibo = new IndexBuffer(new byte[]{
                0, 1, 2,
                2, 3, 0
        });

        // Create a new Vertex Array Object in memory and select it (bind)
        // A VAO can have up to 16 attributes (VBO's) assigned to it by default
        vao = new VertexArray();
        vao.bind();

        // New VBO and bind it
        vbo = new VertexBuffer(vertices);
        // Put the positions in attribute list 0
        layout.push(4, GL_FLOAT, false);
        // Put the colors in attribute list 1
        layout.push(4, GL_FLOAT, false);
        // Put the texture coordinates in attribute list 2
        layout.push(2, GL_FLOAT, false);

        vao.addBuffer(vbo, layout);

        projectionMatrix = new Matrix4f().frustum(0f, Layer.WIDTH, 0f, Layer.HEIGHT,
                1f, 100f);
        viewMatrix = new Matrix4f().translate(0f, 0f, -1f);
    }

    @Override
    public void preDraw() {
        vao.bind();
        ibo.bind();

        shader.bind();
        shader.setMatrix4f("projectionMatrix", projectionMatrix);
        shader.setMatrix4f("viewMatrix", viewMatrix);
    }

    @Override
    public void draw() {
        for (int i = backdrops.length - 1; i > -1; i--) {
            shader.setMatrix4f("modelMatrix",
                    new Matrix4f().translate(backdrops[i].x, backdrops[i].y, -0.1f - (0.0001f * i)).scale(1.15f, 1.1f, 1f));
            if (backdrops[i].texID != -1)
                glBindTexture(GL_TEXTURE_2D, backdrops[i].texID);
            glDrawElements(GL_TRIANGLES, ibo.getIndicesCount(), GL_UNSIGNED_BYTE, 0);
        }
    }

    @Override
    public void postDraw() {

    }

    @Override
    public void close() {
        vao.bind();
        vbo.close();
        ibo.close();
        vao.close();
    }

    public void setBackdrop(int id) {
        backdrops = new Backdrop[]{new Backdrop(id)};
    }

    public void setBackdrops(Backdrop[] backdrops) {
        this.backdrops = backdrops;
    }
}
