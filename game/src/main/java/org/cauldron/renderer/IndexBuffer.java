package org.cauldron.renderer;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

/**
 * Describes a VBO of indices.
 * These indices correspond to the drawing order of triangles used in glDrawElements.
 */
public class IndexBuffer {
    private int rendererId;
    private int indicesCount;

    /**
     * Create a new index buffer - holding indices for glDrawElements - register it in OpenGL and bind it.
     *
     * @param indices The byte array of indices to load into the buffer
     */
    public IndexBuffer(byte[] indices) {
        indicesCount = indices.length;
        ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
        indicesBuffer.put(indices);
        indicesBuffer.flip();

        rendererId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, rendererId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STREAM_DRAW);
    }

    /**
     * Create a new, empty, unbound index buffer.
     */
    public IndexBuffer() {
        rendererId = glGenBuffers();
    }

    /**
     * Deletes the index buffer.
     */
    public void close() {
        this.unbind();
        glDeleteBuffers(rendererId);
    }

    /**
     * Binds the index buffer to the OpenGL context.
     */
    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, rendererId);
    }

    /**
     * Unbinds the index buffer from the OpenGL context.
     */
    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int getIndicesCount() {
        return indicesCount;
    }

    /**
     * Update the ByteBuffer in the element array buffer with a new ByteBuffer of indices.
     *
     * @param indices The new ByteBuffer of indices
     */
    public void updateByteBuffer(ByteBuffer indices) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, rendererId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STREAM_DRAW);
    }

    public void updateByteBuffer(byte[] indices) {
        indicesCount = indices.length;
        ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
        indicesBuffer.put(indices);
        indicesBuffer.flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, rendererId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STREAM_DRAW);
    }

    /**
     * Update the indices in the element array buffer with a new byte array of indices.
     *
     * @param indices The new ByteBuffer of indices
     */
    public void updateIndices(byte[] indices) {
        indicesCount = indices.length;
        ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
        indicesBuffer.put(indices);
        indicesBuffer.flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, rendererId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STREAM_DRAW);
    }

    /**
     * Opens an element array buffer of a particular size to be written to directly.
     *
     * @param size Size of the buffer opened
     */
    public void openBuffer(int size) {
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, size, GL_STREAM_DRAW);
    }
}
