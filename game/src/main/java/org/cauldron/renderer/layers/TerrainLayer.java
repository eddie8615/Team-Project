package org.cauldron.renderer.layers;

import org.cauldron.game.Terrain;
import org.cauldron.renderer.*;
import org.joml.Matrix4f;
import org.joml.Vector2d;

import java.util.List;

import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;

/**
 * TerrainLayer holds all terrain information and draws it using OpenGL calls.
 */
public class TerrainLayer extends Layer {

    private VertexBufferLayout layout = new VertexBufferLayout();
    private Vertex[] verticesGround;
    private Vertex[] verticesSurface;
    private List<Vector2d> terrainPoints;
    private Terrain map;

    public TerrainLayer() {
        vertexShaderPath = "game/shaders/vertex.glsl";
        fragmentShaderPath = "game/shaders/fragment.glsl";
        attribLocations.put("in_Position", 0);
        attribLocations.put("in_Color", 1);
        attribLocations.put("in_TextureCoord", 2);
    }

    public void setTerrain(Terrain map) {
        this.map = map;
    }

    private byte[] integersToBytes(int[] data) {
        byte[] array = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            array[i] = (byte) (data[i] & 0xFF);
        }

        return array;
    }

    private int[] genIndices() {
        int[] indices = new int[terrainPoints.size() * 6];

        int quadCount = 0;
        for (int indicesCount = 0; indicesCount < indices.length - 6; indicesCount = indicesCount + 6) {
            indices[indicesCount] = quadCount;
            indices[indicesCount + 1] = quadCount + 1;
            indices[indicesCount + 2] = quadCount + 3;
            indices[indicesCount + 3] = quadCount + 3;
            indices[indicesCount + 4] = quadCount + 2;
            indices[indicesCount + 5] = quadCount;
            quadCount += 2;
        }

        return indices;
    }

    @Override
    public void setup() {
        shader = new Shader(vertexShaderPath, fragmentShaderPath, attribLocations);

        // New index VBO and bind it
        ibo = new IndexBuffer();

        layoutTerrain();

        // Create a new Vertex Array Object in memory and select it (bind)
        // A VAO can have up to 16 attributes (VBO's) assigned to it by default
        vao = new VertexArray();
        vao.bind();

        // New VBO and bind it
        vbo = new VertexBuffer(verticesSurface);
        // Put the positions in attribute list 0
        layout.push(4, GL_FLOAT, false);
        // Put the colors in attribute list 1
        layout.push(4, GL_FLOAT, false);
        // Put the texture coordinates in attribute list 2
        layout.push(2, GL_FLOAT, false);

        vao.addBuffer(vbo, layout);

    }

    private void layoutTerrain() {
        terrainPoints = map.vertices;
        int count = terrainPoints.size();
        verticesSurface = new Vertex[count * 2];
        verticesGround = new Vertex[count * 2];
        int[] indices = new int[(count - 1) * 6];

        int indicesCount = 0;

        for (Integer i = 0; i < count * 2; i = i + 2) {
            Vertex vertexBot = new Vertex();
            vertexBot.setXYZ(i / 2f * map.increment, map.getYVal(i / 2f * map.increment) - 10f, 0);
            vertexBot.setRGB(1f, 1f, 1f);
            vertexBot.setST(i / 4f, 1);

            Vertex vertexTop = new Vertex();
            vertexTop.setXYZ(i / 2f * map.increment, map.getYVal(i / 2f * map.increment) + 10f, 0);
            vertexTop.setRGB(1f, 1f, 1f);
            vertexTop.setST(i / 4f, 0);

            verticesSurface[i] = vertexTop;
            verticesSurface[i + 1] = vertexBot;

            if (indicesCount < indices.length - 5) {
                indices[indicesCount] = i;
                indices[indicesCount + 1] = i + 1;
                indices[indicesCount + 2] = i + 3;
                indices[indicesCount + 3] = i + 3;
                indices[indicesCount + 4] = i + 2;
                indices[indicesCount + 5] = i;
                indicesCount += 6;
            }
        }

        for (Integer i = 0; i < count * 2; i = i + 2) {
            Vertex vertexTop = new Vertex();
            vertexTop.setXYZ((float) terrainPoints.get(i / 2).x, (float) terrainPoints.get(i / 2).y, 0f);
            vertexTop.setRGB(1f, 1f, 1f);
            vertexTop.setST((float) terrainPoints.get(i / 2).x / Layer.WIDTH, (Layer.HEIGHT - (float) terrainPoints.get(i / 2).y) / Layer.HEIGHT);

            Vertex vertexBot = new Vertex();
            vertexBot.setXYZ((float) terrainPoints.get(i / 2).x, 0, 0f);
            vertexBot.setRGB(1f, 1f, 1f);
            vertexBot.setST((float) terrainPoints.get(i / 2).x / Layer.WIDTH, 1);

            verticesGround[i] = vertexTop;
            verticesGround[i + 1] = vertexBot;
        }
        ibo.updateIndices(integersToBytes(indices));
    }

    public void update() {
        layoutTerrain();
//        ibo.updateIndices(integersToBytes(genIndices()));
    }

    @Override
    public void preDraw() {
        shader.bind();
        shader.setMatrix4f("projectionMatrix", new Matrix4f().frustum(0f, 1600, 0f, 900,
                1f, 100f));
        shader.setMatrix4f("viewMatrix", new Matrix4f().translate(0f, 0f, -1f));
        shader.setMatrix4f("modelMatrix", new Matrix4f());

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void draw() {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);

        vbo.updateVertices(verticesGround);
        vao.addBuffer(vbo, layout);
        ibo.bind();
        shader.setMatrix4f("modelMatrix", new Matrix4f().translate(0, 0, -0.0001f));
        glBindTexture(GL_TEXTURE_2D, TextureHandler.getTextureID("ground"));
        glDrawElements(GL_TRIANGLE_STRIP, ibo.getIndicesCount(), GL_UNSIGNED_BYTE, 0);

        vbo.updateVertices(verticesSurface);
        vao.addBuffer(vbo, layout);
        ibo.bind();
        shader.setMatrix4f("modelMatrix", new Matrix4f());
        glBindTexture(GL_TEXTURE_2D, TextureHandler.getTextureID("terrain"));
        glDrawElements(GL_TRIANGLE_STRIP, ibo.getIndicesCount(), GL_UNSIGNED_BYTE, 0);
    }

    @Override
    public void postDraw() {
    }

    @Override
    public void close() {
        if (vao != null)
            vao.bind();
        if (vbo != null)
            vbo.close();
        if (ibo != null)
            ibo.close();
        if (vao != null)
            vao.close();
    }
}
