package org.cauldron.renderer.layers;

import org.cauldron.renderer.*;
import org.cauldron.ui.TexUIElement;
import org.cauldron.ui.UIElement;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.*;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.cauldron.util.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14C.glBlendEquation;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * GUILayer holds all Nuklear GUI information draws it using OpenGL calls.
 */
public class GUILayer extends Layer {
    private static final int BUFFER_INITIAL_SIZE = 4 * 1024;
    private static final int MAX_VERTEX_BUFFER = 512 * 1024;
    private static final int MAX_ELEMENT_BUFFER = 128 * 1024;
    private static final NkAllocator ALLOCATOR;
    private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
    private static Matrix4f modelMatrix = new Matrix4f();
    private static VertexBufferLayout layout;
    private static ByteBuffer vertices;
    private static ByteBuffer elements;

    static {
        ALLOCATOR = NkAllocator.create()
                .alloc((handle, old, size) -> nmemAllocChecked(size))
                .mfree((handle, ptr) -> nmemFree(ptr));

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
                .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                .position(1).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(8)
                .position(2).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(12)
                .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                .flip();
    }

    private ByteBuffer ttf;

    private NkContext ctx = NkContext.create();
    private NkUserFont default_font = NkUserFont.create();

    private NkBuffer cmds = NkBuffer.create();
    private NkDrawNullTexture null_texture = NkDrawNullTexture.create();

    private Map<Integer, UIElement> uiElements = new HashMap<>();
    private Map<String, TexUIElement> texUiElements = new HashMap<>();
    private Vertex[] texVertices;
    private VertexBufferLayout texLayout;
    private byte[] texIndices;
    private Matrix4f viewMatrix;
    private Matrix4f texViewMatrix = new Matrix4f().translate(0f, 0f, -1f);

    public GUILayer() {
        vertexShaderPath = "game/shaders/vertex.glsl";
        fragmentShaderPath = "ui/shaders/fragment.glsl";
        attribLocations.put("in_Position", 0);
        attribLocations.put("in_Color", 1);
        attribLocations.put("in_TextureCoord", 2);
    }

    @Override
    public void setup() {

        shader = new Shader(vertexShaderPath, fragmentShaderPath, attribLocations);

        nk_init(ctx, ALLOCATOR, null);

        nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);

        {
            // buffer setup
            vao = new VertexArray();
            vbo = new VertexBuffer();
            ibo = new IndexBuffer();

            // Must push in order: position, colour, texture coords
            layout = new VertexBufferLayout();
            layout.push(2, GL_FLOAT, false);
            layout.push(4, GL_UNSIGNED_BYTE, true);
            layout.push(2, GL_FLOAT, false);

            vao.addBuffer(vbo, layout);
        }

        {
            // null texture setup
            int nullTexID = glGenTextures();

            null_texture.texture().id(nullTexID);
            null_texture.uv().set(0.5f, 0.5f);

            glBindTexture(GL_TEXTURE_2D, nullTexID);
            try (MemoryStack stack = stackPush()) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
            }
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        }

        glBindTexture(GL_TEXTURE_2D, 0);

        // Augment Nuklear's top-left coord system to match our bottom-left one
        // As such, mouse callbacks to Nuklear need to be negative and inverted,
        // and normal mouse callbacks need to be inverted
        Matrix4f projectionMatrix = new Matrix4f().frustum(0f, 1600f, 0, 900f,
                1f, 100f);
        viewMatrix = new Matrix4f().mul(new Matrix4f(
                1f, 0f, 0f, 0f,
                0f, -1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f)).translate(0f, 0f, -1f);
        modelMatrix = new Matrix4f();


        // setup program
        shader.bind();
        shader.setUniform1i("Texture", 0);
        shader.setMatrix4f("projectionMatrix", projectionMatrix);
        shader.setMatrix4f("viewMatrix", viewMatrix);
        shader.setMatrix4f("modelMatrix", modelMatrix);


        defineUI();

        // TEXTURED UI ELEMS
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

        texVertices = new Vertex[]{v0, v1, v2, v3};

        // OpenGL expects to draw vertices in counter clockwise order by default
        texIndices = new byte[]{
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
        vbo = new VertexBuffer(texVertices);
        texLayout = new VertexBufferLayout();
        // Put the positions in attribute list 0
        texLayout.push(4, GL_FLOAT, false);
        // Put the colors in attribute list 1
        texLayout.push(4, GL_FLOAT, false);
        // Put the texture coordinates in attribute list 2
        texLayout.push(2, GL_FLOAT, false);

        vao.addBuffer(vbo, layout);

        // New index VBO and bind it
        ibo = new IndexBuffer(texIndices);
    }

    private void defineUI() {
        try {
            this.ttf = ioResourceToByteBuffer("ui/fonts/FiraSans.ttf", 512 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int BITMAP_W = 1024;
        int BITMAP_H = 1024;

        int FONT_HEIGHT = 18;
        int fontTexID = glGenTextures();

        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(95);

        float scale;
        float descent;

        try (MemoryStack stack = stackPush()) {
            stbtt_InitFont(fontInfo, ttf);
            scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);

            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0) * scale;

            ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);

            STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
            stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
            stbtt_PackSetOversampling(pc, 4, 4);
            stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
            stbtt_PackEnd(pc);

            // Convert R8 to RGBA8
            ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < bitmap.capacity(); i++) {
                texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
            }
            texture.flip();

            glBindTexture(GL_TEXTURE_2D, fontTexID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            memFree(texture);
            memFree(bitmap);
        }

        default_font
                .width((handle, h, text, len) -> {
                    float text_width = 0;
                    try (MemoryStack stack = stackPush()) {
                        IntBuffer unicode = stack.mallocInt(1);

                        int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
                        int text_len = glyph_len;

                        if (glyph_len == 0) {
                            return 0;
                        }

                        IntBuffer advance = stack.mallocInt(1);
                        while (text_len <= len && glyph_len != 0) {
                            if (unicode.get(0) == NK_UTF_INVALID) {
                                break;
                            }

                            /* query currently drawn glyph information */
                            stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
                            text_width += advance.get(0) * scale;

                            /* offset next glyph */
                            glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
                            text_len += glyph_len;
                        }
                    }
                    return text_width;
                })
                .height(FONT_HEIGHT)
                .query((handle, font_height, glyph, codepoint, next_codepoint) -> {
                    try (MemoryStack stack = stackPush()) {
                        FloatBuffer x = stack.floats(0.0f);
                        FloatBuffer y = stack.floats(0.0f);

                        STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);
                        IntBuffer advance = stack.mallocInt(1);

                        stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
                        stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);

                        NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);

                        ufg.width(q.x1() - q.x0());
                        ufg.height(q.y1() - q.y0());
                        ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
                        ufg.xadvance(advance.get(0) * scale);
                        ufg.uv(0).set(q.s0(), q.t0());
                        ufg.uv(1).set(q.s1(), q.t1());
                    }
                })
                .texture(it -> it
                        .id(fontTexID));

        nk_style_set_font(ctx, default_font);
    }

    public void setUIElements(Map<String, UIElement> elements) {
        uiElements = new HashMap<>();
        for (Map.Entry<String, UIElement> entry : elements.entrySet()) {
            uiElements.put(entry.getValue().id, entry.getValue());
        }
    }

    public void setTexUIElements(Map<String, TexUIElement> texElements) {
        texUiElements = texElements;
    }

    //add getUIElement()
    public Map<String, UIElement> getUIElements() {
        Map<String, UIElement> newMap = new HashMap<>();
        for (Map.Entry<Integer, UIElement> entry : uiElements.entrySet()) {
            newMap.put(entry.getValue().getName(), entry.getValue());
        }
        return newMap;
    }

    public Map<String, TexUIElement> getTexUIElements() {
        return texUiElements;
    }

    @Override
    public void preDraw() {
        modelMatrix = new Matrix4f();
        for (UIElement element : uiElements.values()) {
            if (!element.isEnabled()) {
                Vector2f pos = new Vector2f(element.getPosition());
                element.setPosition(-500, -500);
                element.layout();
                element.setPosition(pos);
            } else {
                element.layout();
            }
        }

        shader.bind();

        try (MemoryStack stack = stackPush()) {
            // setup global state
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glActiveTexture(GL_TEXTURE0);
        }

        {
            // convert from command queue into draw list and draw to screen

            // allocate vertex and element buffer
            vao.bind();
            vbo.bind();
            ibo.bind();

            // load draw vertices & elements directly into vertex + element buffer
            vertices = BufferUtils.createByteBuffer(MAX_VERTEX_BUFFER);
            elements = BufferUtils.createByteBuffer(MAX_ELEMENT_BUFFER);
            try (MemoryStack stack = stackPush()) {
                // fill convert configuration
                NkConvertConfig config = NkConvertConfig.callocStack(stack)
                        .vertex_layout(VERTEX_LAYOUT)
                        .vertex_size(20)
                        .vertex_alignment(4)
                        .null_texture(null_texture)
                        .circle_segment_count(22)
                        .curve_segment_count(22)
                        .arc_segment_count(22)
                        .global_alpha(1.0f)
                        .shape_AA(NK_ANTI_ALIASING_ON)
                        .line_AA(NK_ANTI_ALIASING_ON);

                // setup buffers to load vertices and elements
                NkBuffer vbuf = NkBuffer.mallocStack(stack);
                NkBuffer ebuf = NkBuffer.mallocStack(stack);

                nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
                nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
                nk_convert(ctx, cmds, vbuf, ebuf, config);
            }
        }
    }

    @Override
    public void draw() {

        vbo.updateVertices(texVertices);
        vbo.bind();
        ibo.updateByteBuffer(texIndices);
        ibo.bind();
        vao.addBuffer(vbo, texLayout);
        shader.setMatrix4f("viewMatrix", texViewMatrix);
        for (Map.Entry<String, TexUIElement> e : texUiElements.entrySet()) {
            if (!e.getValue().isEnabled())
                continue;
            shader.setMatrix4f("modelMatrix", Renderer.getModelMatrix(e.getValue()));
            glBindTexture(GL_TEXTURE_2D, e.getValue().texID);
            glDrawElements(GL_TRIANGLES, ibo.getIndicesCount(), GL_UNSIGNED_BYTE, 0);
        }

        long offset = NULL;
        vbo.updateByteBuffer(vertices);
        ibo.updateByteBuffer(elements);
        vao.addBuffer(vbo, layout);
        shader.setMatrix4f("viewMatrix", viewMatrix);
        shader.setMatrix4f("modelMatrix", new Matrix4f());
        int count = 0;
        for (NkDrawCommand cmd = nk__draw_begin(ctx, cmds); cmd != null; cmd = nk__draw_next(cmd, cmds, ctx)) {
            if (cmd.elem_count() == 0) {
                continue;
            }
            Matrix4f newMatrix = new Matrix4f();
            UIElement e = uiElements.get(cmd.userdata().id());
            if (e != null) {
                Vector2f pos = e.getPosition();
                float rot = e.getRotation();
                float scale = e.getScale();
                newMatrix.translate(pos.x, -pos.y, 0f);
                newMatrix.rotate((float) Math.toRadians(rot), 0f, 0f, 1f);
                newMatrix.scale(scale, scale, 1);
                newMatrix.translate(-pos.x, pos.y, 0f);
            }
            shader.setMatrix4f("modelMatrix", newMatrix);
            glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
            glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
            offset += cmd.elem_count() * 2;
        }
    }

    @Override
    public void postDraw() {
        nk_clear(ctx);

        // default OpenGL state
        glEnable(GL_DEPTH_TEST);
    }

    public NkContext getContext() {
        return ctx;
    }

    public void close() {
        vao.bind();
        vbo.bind();
        ibo.bind();
        Objects.requireNonNull(ctx.clip().copy()).free();
        Objects.requireNonNull(ctx.clip().paste()).free();
        nk_free(ctx);
        shader.bind();
        glDeleteTextures(default_font.texture().id());
        glDeleteTextures(null_texture.texture().id());
        shader.close();
        vbo.close();
        ibo.close();
        vao.close();
        nk_buffer_free(cmds);

        Objects.requireNonNull(default_font.query()).free();
        Objects.requireNonNull(default_font.width()).free();

        Objects.requireNonNull(ALLOCATOR.alloc()).free();
        Objects.requireNonNull(ALLOCATOR.mfree()).free();
    }
}
