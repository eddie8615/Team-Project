package org.cauldron.renderer.layers;

import org.cauldron.renderer.IndexBuffer;
import org.cauldron.renderer.Shader;
import org.cauldron.renderer.VertexArray;
import org.cauldron.renderer.VertexBuffer;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Layer implements Serializable {
    // The fixed coordinate system
    public static final int WIDTH = 1600;
    public static final int HEIGHT = 900;

    protected long window;
    protected VertexArray vao;
    protected VertexBuffer vbo;
    protected IndexBuffer ibo;
    protected Shader shader;
    protected String vertexShaderPath;
    protected String fragmentShaderPath;
    protected HashMap<String, Integer> attribLocations = new HashMap<>();

    public Layer() {
    }

    public VertexArray getVertexArray() {
        return vao;
    }

    public IndexBuffer getIndexBuffer() {
        return ibo;
    }

    public Shader getShader() {
        return shader;
    }

    // Prepare VAO, VBO, IBO and shader
    public abstract void setup();

    // Scenes may use this function to apply actions
    // before a draw (like moving vertices into the buffer)
    public abstract void preDraw();

    public abstract void draw();

    // Scenes may use this function to apply actions
    // after a draw (like setting gl flags back to normal)
    public abstract void postDraw();

    public abstract void close();
}
