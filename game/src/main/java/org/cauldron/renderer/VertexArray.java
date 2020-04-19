package org.cauldron.renderer;

import java.util.LinkedList;

import static org.lwjgl.opengl.GL30.*;

/**
 * Describes a Vertex Array Object (VAO) and its links to Vertex Buffers and their attributes.
 **/
public class VertexArray {

    private int rendererId;

    /**
     * Creates and registers a new VAO, passing its GL location to rendererID.
     */
    public VertexArray() {
        rendererId = glGenVertexArrays();
    }

    /**
     * Deletes the VAO.
     */
    public void close() {
        glDeleteVertexArrays(rendererId);
    }

    /**
     * Binds the VAO to the OpenGL context.
     */
    public void bind() {
        glBindVertexArray(rendererId);
    }

    /**
     * Unbinds the VAO from the OpenGL context.
     */
    public void unbind() {
        glBindVertexArray(0);
    }

    /**
     * Binds Vertex Buffer Object attributes to the VAO according to a Vertex Buffer Layout.
     *
     * @param vb  The VBO to bind the attributes from
     * @param vbl The layout of the VBO
     */
    public void addBuffer(VertexBuffer vb, VertexBufferLayout vbl) {
        bind();
        vb.bind();

        LinkedList<VertexBufferElement> elements = vbl.getElements();
        int offset = 0;

        for (int i = 0; i < elements.size(); i++) {
            VertexBufferElement vbe = elements.get(i);
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, vbe.count, vbe.type, vbe.normalised, vbl.getStride(), offset);
            offset += vbe.count * VertexBufferElement.getTypeSize(vbe.type);
        }
    }
}
