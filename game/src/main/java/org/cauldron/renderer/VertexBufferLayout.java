package org.cauldron.renderer;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Used to describe the layout of different attributes in the Vertex Buffer Object
 */
public class VertexBufferLayout implements Serializable {
    private LinkedList<VertexBufferElement> elements = new LinkedList<>();
    private int stride = 0;

    /**
     * @param count      Number of items of the given GL type (4 GL_FLOAT colour values means 4 is the count)
     * @param type       GL type of the items being stored in the buffer object
     * @param normalised Whether the data values should be normalised
     */
    public void push(int count, int type, boolean normalised) {
        VertexBufferElement vbe = new VertexBufferElement();
        vbe.type = type;
        vbe.count = count;
        vbe.normalised = normalised;
        elements.add(vbe);
        stride += count * VertexBufferElement.getTypeSize(type);
    }

    /**
     * @return The stride (cumulative size in bytes) of the layout content
     */
    public int getStride() {
        return stride;
    }

    /**
     * @return The list of all elements in the layout
     */
    public LinkedList<VertexBufferElement> getElements() {
        return elements;
    }
}
