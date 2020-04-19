package org.cauldron.renderer;

import static org.lwjgl.opengl.GL30.*;

public class VertexBufferElement {
    int type;
    int count;
    boolean normalised;

    public static int getTypeSize(int type) {
        switch (type) {
            case GL_UNSIGNED_BYTE:
                return 1;
            case GL_FLOAT:
            case GL_UNSIGNED_INT:
                return 4;
        }
        assert false;
        return 0;
    }
}
