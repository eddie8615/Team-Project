package org.cauldron.renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

/**
 * Describes an OpenGL vertex buffer object (VBO).
 * Contains vertex information, held in an array buffer.
 */
public class VertexBuffer {
    private int rendererId;
    private ByteBuffer vertexByteBuffer = null;
    private ByteBuffer verticesByteBuffer = null;

    /**
     * Creates a new VBO using the vertices passed, and binds it.
     *
     * @param vertices The list of vertices to load into a new VBO
     */
    public VertexBuffer(Vertex[] vertices) {
        // Create a FloatBuffer of the appropriate size for one vertex
        vertexByteBuffer = BufferUtils.createByteBuffer(Vertex.stride);

        // Put each 'Vertex' in one FloatBuffer
        verticesByteBuffer = BufferUtils.createByteBuffer(vertices.length *
                Vertex.stride);
        FloatBuffer verticesFloatBuffer = verticesByteBuffer.asFloatBuffer();
        for (int i = 0; i < vertices.length; i++) {
            // Add position, color and texture floats to the buffer
            verticesFloatBuffer.put(vertices[i].getElements());
        }
        verticesFloatBuffer.flip();

        rendererId = glGenBuffers();
        // Create a new Vertex Buffer Object in memory and select it (bind) - VERTICES
        glBindBuffer(GL_ARRAY_BUFFER, rendererId);
        glBufferData(GL_ARRAY_BUFFER, verticesByteBuffer, GL_STREAM_DRAW);
    }

    /**
     * Register a new, empty, unbound VBO.
     */
    public VertexBuffer() {
        rendererId = glGenBuffers();
    }

    /**
     * Deletes the VBO.
     */
    public void close() {
        this.unbind();
        glDeleteBuffers(rendererId);
    }

    /**
     * Binds the VBO to the current VAO.
     */
    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, rendererId);
    }

    /**
     * Unbinds the VBO from the current VAO.
     */
    public void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Update a vertex held in the VBO.
     *
     * @param v      Number of the vertex to replace in the VBO
     * @param vertex New vertex to replace the old one with
     */
    public void updateVertex(int v, Vertex vertex) {
        FloatBuffer vertexFloatBuffer = vertexByteBuffer.asFloatBuffer();
        vertexFloatBuffer.rewind();
        vertexFloatBuffer.put(vertex.getElements());
        vertexFloatBuffer.flip();

        glBufferSubData(GL15.GL_ARRAY_BUFFER, v * Vertex.stride,
                vertexByteBuffer);
    }

    /**
     * Replaces the ByteBuffer of vertices currently held in the VBO with the given one.
     *
     * @param vertices A ByteBuffer of vertices to update the VBO with
     */
    public void updateByteBuffer(ByteBuffer vertices) {
        glBindBuffer(GL_ARRAY_BUFFER, rendererId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STREAM_DRAW);
    }

    /**
     * Replaces the ByteBuffer of vertices currently held in the VBO with vertices from a given Vertex list.
     *
     * @param vertices A list of vertices to update the VBO with
     */
    public void updateVertices(Vertex[] vertices) {
        // Create a FloatBuffer of the appropriate size for one vertex
        vertexByteBuffer = BufferUtils.createByteBuffer(Vertex.stride);

        // Put each 'Vertex' in one FloatBuffer
        verticesByteBuffer = BufferUtils.createByteBuffer(vertices.length *
                Vertex.stride);
        FloatBuffer verticesFloatBuffer = verticesByteBuffer.asFloatBuffer();
        for (int i = 0; i < vertices.length; i++) {
            // Add position, color and texture floats to the buffer
            verticesFloatBuffer.put(vertices[i].getElements());
        }
        verticesFloatBuffer.flip();

        // Create a new Vertex Buffer Object in memory and select it (bind) - VERTICES
        glBindBuffer(GL_ARRAY_BUFFER, rendererId);
        glBufferData(GL_ARRAY_BUFFER, verticesByteBuffer, GL_STREAM_DRAW);
    }
}
