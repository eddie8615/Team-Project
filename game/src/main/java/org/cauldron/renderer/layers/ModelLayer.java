package org.cauldron.renderer.layers;

//import org.cauldron.entity.Entity;

import org.cauldron.entity.Entity;
import org.cauldron.entity.EntityHandler;
import org.cauldron.renderer.*;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.cauldron.entity.EntityType.GROUND;
import static org.cauldron.entity.EntityType.PARTICLE;
import static org.lwjgl.opengl.GL11C.*;

/**
 * ModelLayer holds all Entity information the game wishes to draw and draws it using OpenGL calls.
 */
public class ModelLayer extends Layer {

    private EntityHandler entityHandler;
    private Vertex[] vertices;
    private Shader shaderColor;
    private String fragmentShaderColorPath;
    private VertexBufferLayout layout;

    public ModelLayer() {
        vertexShaderPath = "game/shaders/vertex.glsl";
        fragmentShaderPath = "game/shaders/fragment.glsl";
        fragmentShaderColorPath = "game/shaders/fragmentColor.glsl";
        attribLocations.put("in_Position", 0);
        attribLocations.put("in_Color", 1);
        attribLocations.put("in_TextureCoord", 2);
    }

    @Override
    public void setup() {
        shader = new Shader(vertexShaderPath, fragmentShaderPath, attribLocations);
        shaderColor = new Shader(vertexShaderPath, fragmentShaderColorPath, attribLocations);
        Vertex v0 = new Vertex();
        v0.setXYZ(0f, 1, 0f);
        v0.setRGB(1, 1, 1);
        v0.setST(0, 0);
        Vertex v1 = new Vertex();
        v1.setXYZ(0f, 0f, 0f);
        v1.setRGB(1, 1, 1);
        v1.setST(0, 1);
        Vertex v2 = new Vertex();
        v2.setXYZ(1, 0f, 0f);
        v2.setRGB(1, 1, 1);
        v2.setST(1, 1);
        Vertex v3 = new Vertex();
        v3.setXYZ(1, 1, 0f);
        v3.setRGB(1, 1, 1);
        v3.setST(1, 0);

        vertices = new Vertex[]{v0, v1, v2, v3};

        // OpenGL expects to draw vertices in counter clockwise order by default
        byte[] indices = {
                // Left bottom triangle
                0, 1, 2,
                // Right top triangle
                2, 3, 0
        };

        // Create a new Vertex Array Object in memory and select it (bind)
        // A VAO can have up to 16 attributes (VBO's) assigned to it by default
        vao = new VertexArray();
        vao.bind();

        // New VBO and bind it
        vbo = new VertexBuffer(vertices);
        layout = new VertexBufferLayout();
        // Put the positions in attribute list 0
        layout.push(4, GL_FLOAT, false);
        // Put the colors in attribute list 1
        layout.push(4, GL_FLOAT, false);
        // Put the texture coordinates in attribute list 2
        layout.push(2, GL_FLOAT, false);

        vao.addBuffer(vbo, layout);

        // New index VBO and bind it
        ibo = new IndexBuffer(indices);
    }

    public void setEntities(EntityHandler entityHandler) {
        this.entityHandler = entityHandler;
    }

    @Override
    public void preDraw() {
        vao.bind();
        ibo.bind();

        shader.bind();
        shader.setMatrix4f("projectionMatrix", new Matrix4f().frustum(0f, 1600, 0f, 900,
                1f, 100f));
        shader.setMatrix4f("viewMatrix", new Matrix4f().translate(0f, 0f, -1f));
        shaderColor.bind();
        shaderColor.setMatrix4f("projectionMatrix", new Matrix4f().frustum(0f, 1600, 0f, 900,
                1f, 100f));
        shaderColor.setMatrix4f("viewMatrix", new Matrix4f().translate(0f, 0f, -1f));

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void draw() {
        // Draw in order of depth
        List<Entity> entityList = new ArrayList<>(Arrays.asList(entityHandler.getEntities()));
        Collections.sort(entityList, (s1, s2) -> {
            if (s1 == null && s2 == null)
                return 0;
            if (s1 == null)
                return 1;
            if (s2 == null)
                return -1;
            return Double.compare(s1.depth, s2.depth);
        });
        for (Entity entity : entityList) {
            if (entity == null)
                continue;
            if (entity.type == GROUND)
                continue;


            if (entity.type == PARTICLE) {
                Renderer.setColor(vertices, entity.blockColor);
                vbo.updateVertices(vertices);
                shaderColor.bind();
                shaderColor.setMatrix4f("modelMatrix", Renderer.getModelMatrix(entity));
            } else {
                shader.bind();
                shader.setMatrix4f("modelMatrix", Renderer.getModelMatrix(entity));
                glBindTexture(GL_TEXTURE_2D, TextureHandler.getEntityTexture(entity));
            }

            // Draw the vertices
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
}
