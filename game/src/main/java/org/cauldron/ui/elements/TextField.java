package org.cauldron.ui.elements;

import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.NkPluginFilterI;
import org.lwjgl.nuklear.Nuklear;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.nuklear.Nuklear.NK_EDIT_MULTILINE;

public class TextField {
    private int options;
    private final int maxLength;
    private ByteBuffer content; // Nuklear puts the data in here
    private ByteBuffer blindContents;
    private IntBuffer length; // Nuklear writes the length of the string in here
    private NkPluginFilterI filter; // Restrict what the user can type

    public TextField(int maxLength, boolean multiline) {
        this.maxLength = maxLength;
        options = 0;
        if (multiline) {
            options = NK_EDIT_MULTILINE;
        }
        // Since we're using ASCII, each character just takes one byte.
        // We use maxLength + 1 because Nuklear seems to omit the last character.
        content = BufferUtils.createByteBuffer(maxLength + 1);

        blindContents = BufferUtils.createByteBuffer(maxLength + 1);
        // The IntBuffer is size 1 because we only need one int
        length = BufferUtils.createIntBuffer(1); // BufferUtils from LWJGL

        // Setup a filter to restrict to ASCII
        filter = NkPluginFilter.create(Nuklear::nnk_filter_ascii);

    }

    /**
     * This method uses Nuklear to draw the text field
     */

    /**
     * This method returns the text that the user typed in
     */
    public String getValue() {
        // The way to get a string from a ByteBuffer is to pull out an array of
        // bytes and pass it into the String constructor.
        content.mark(); // Mark the buffer so that we can return the pointer here when we're done
        byte[] bytes = new byte[length.get(0)];
        content.get(bytes, 0, length.get(0));
        content.reset(); // Return to the previous marker so that Nuklear can write here again
        String out = new String(bytes, StandardCharsets.US_ASCII);
        return out;
    }

    public byte getLastByte() {
        blindContents.mark();
        byte lastByte;
        lastByte = blindContents.get(length.get(0) - 1);
        blindContents.reset();
        return lastByte;
    }

    public ByteBuffer getBlind() {
        return this.blindContents;
    }

    public void setBlind(ByteBuffer buffer) {
        this.blindContents = buffer;
    }

    public void setContent(ByteBuffer buffer) {
        this.content = buffer;
    }

    public ByteBuffer getContent() {
        return this.content;
    }

    public IntBuffer getLength() {
        return this.length;
    }

    public int maxLength() {
        return this.maxLength;
    }

    public NkPluginFilterI getPluginFilter() {
        return this.filter;
    }
}
