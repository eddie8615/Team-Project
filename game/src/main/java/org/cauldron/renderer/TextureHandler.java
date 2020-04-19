package org.cauldron.renderer;

import org.cauldron.entity.Entity;
import org.cauldron.entity.EntityHandler;
import org.cauldron.entity.PowerUpType;
import org.cauldron.entity.entities.Crate;
import org.cauldron.entity.entities.Projectile;
import org.cauldron.renderer.layers.GUILayer;
import org.lwjgl.nuklear.NkImage;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static java.lang.Math.round;
import static org.cauldron.util.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageResize.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Loads textures and gets their locations in OpenGL once loaded.
 */
public class TextureHandler {
    private static HashMap<Integer, Integer> entityTextures = new HashMap<>();
    private static HashMap<String, Integer> textures = new HashMap<>();
    private static HashMap<String, NkImage> texturesNk = new HashMap<>();
    private static EntityHandler entityHandler;
    static Map<Integer, Animation> animationCache = new HashMap<Integer, Animation>();
    static Map<String, Animation> uiAnimationCache = new HashMap<String, Animation>();

    public static void initEntityTextures(EntityHandler eh) {
        entityHandler = eh;
    }

    /**
     * Loads a folder of texture PNGs into the texture index in the OpenGL context.
     * The names in the returned HashMap have the same name as the PNG file, without the extension.
     *
     * @param texList The folder of texture PNGs to load in
     * @return A HashMap of all texture names and their corresponding OpenGL IDs
     */
    public static HashMap<String, Integer> loadTextures(String texList) {
        // Get all texture paths in texList
        List<String> texFiles = new ArrayList<>();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(texList);
        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = rdr.readLine()) != null) {
                texFiles.add(line);
            }
            rdr.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        for (String s : texFiles)
            System.out.println(s);

        String texName = "";
        int posSlash = texList.lastIndexOf("/");
        String texPath = texList.substring(0, posSlash + 1);
        glActiveTexture(GL_TEXTURE0);
        for (String file : texFiles) {
            texName = file;
            int pos = texName.lastIndexOf(".");
            if (pos == -1)
                continue;
            if (!texName.substring(texName.length() - 3).equals("png"))
                continue;
            texName = file.substring(0, pos);
            int texID = loadPNGTexture(texPath + file, GL_TEXTURE0);
            textures.put(texName, texID);
            NkImage nkImage = NkImage.create();
            texturesNk.put(texName, nkImage.handle(it -> it.id(texID)));
        }

        return textures;
    }

    /**
     * Loads a PNG file into OpenGL, after decoding it.
     *
     * @param path        The file to decode and load into the OpenGL context
     * @param textureUnit The texture attribute list to load the texture into
     * @return The location of the texture in OpenGL context
     */
    private static int loadPNGTexture(String path, int textureUnit) {
        ByteBuffer imageBuffer;
        try {
            imageBuffer = ioResourceToByteBuffer(path, 8 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int texID;
        ByteBuffer image;
        int width, height, comp;
        IntBuffer w, h, channel;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            w = stack.mallocInt(1);
            h = stack.mallocInt(1);
            channel = stack.mallocInt(1);
            image = stbi_load_from_memory(imageBuffer, w, h, channel, 0);
            if (image == null) {
                throw new RuntimeException("Failed to load texture: " + stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
            comp = channel.get();
        }

        texID = glGenTextures();
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_2D, texID);

        // Setup the ST coordinate system
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // Setup what to do when the texture has to be scaled
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        int format;
        if (comp == 3) {
            if ((width & 3) != 0) {
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (width & 1));
            }
            format = GL_RGB;
        } else {
            premultiplyAlpha(image, width, height);

            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            format = GL_RGBA;
        }

        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, image);

        ByteBuffer input_pixels = image;
        int input_w = width;
        int input_h = height;
        int mipmapLevel = 0;
        while (1 < input_w || 1 < input_h) {
            int output_w = Math.max(1, input_w >> 1);
            int output_h = Math.max(1, input_h >> 1);

            ByteBuffer output_pixels = memAlloc(output_w * output_h * comp);
            stbir_resize_uint8_generic(
                    input_pixels, input_w, input_h, input_w * comp,
                    output_pixels, output_w, output_h, output_w * comp,
                    comp, comp == 4 ? 3 : STBIR_ALPHA_CHANNEL_NONE, STBIR_FLAG_ALPHA_PREMULTIPLIED,
                    STBIR_EDGE_CLAMP,
                    STBIR_FILTER_MITCHELL,
                    STBIR_COLORSPACE_SRGB
            );

            if (mipmapLevel == 0) {
                stbi_image_free(image);
            } else {
                memFree(input_pixels);
            }

            glTexImage2D(GL_TEXTURE_2D, ++mipmapLevel, format, output_w, output_h, 0, format, GL_UNSIGNED_BYTE, output_pixels);

            input_pixels = output_pixels;
            input_w = output_w;
            input_h = output_h;
        }
        if (mipmapLevel == 0) {
            stbi_image_free(image);
        } else {
            memFree(input_pixels);
        }

        return texID;
    }


    private static void premultiplyAlpha(ByteBuffer image, int w, int h) {
        int stride = w * 4;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * stride + x * 4;

                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte) round(((image.get(i + 0) & 0xFF) * alpha)));
                image.put(i + 1, (byte) round(((image.get(i + 1) & 0xFF) * alpha)));
                image.put(i + 2, (byte) round(((image.get(i + 2) & 0xFF) * alpha)));
            }
        }
    }

    public static HashMap<String, Integer> getTextures() {
        return textures;
    }

    public static int getTextureID(String name) {
        return textures.get(name);
    }

    public static NkImage getTextureNk(String name) {
        return texturesNk.get(name);
    }

    /**
     * will return the correct texture (the ID of it) for the entity it is passed. e.g. it will check what type of powerup the crate holds and display the right crate image.
     *
     * @param e the entity
     * @return the texture ID, gotten using getTextureID()
     */
    public static int getEntityTexture(Entity e) {
        if (entityTextures.containsKey(e.loc))
            return entityTextures.get(e.loc);

        String color = e.playerColor;
        if (!(color.equals("green") || color.equals("blue") || color.equals("purple") || color.equals("red"))) {
            color = "green";
        }

        switch (e.type) {
            case TANK:
                return getTextureID("tank_" + color);
            case TURRET:
                return getTextureID("turret_" + color);
            case PROJECTILE:
                Projectile projectile = (Projectile) e;
                if (projectile.guided) {
                    return getTextureID("projectile_targeting");
                }
                return getTextureID("projectile");
            case CRATE:
                Crate crate = (Crate) e;
                String stage = "";
                if (crate.health <= 28) {
                    stage = "_2";
                } else if (crate.health <= 55) {
                    stage = "_1";
                }
                if (crate.powerUpType == PowerUpType.HEAL) {
                    return getTextureID("crate_health" + stage);
                }
                if (crate.powerUpType == PowerUpType.STRENGTH) {
                    return getTextureID("crate_strength" + stage);
                }
                if (crate.powerUpType == PowerUpType.SHIELD) {
                    return getTextureID("crate_shield" + stage);
                }
                if (crate.powerUpType == PowerUpType.CLUSTER) {
                    return getTextureID("crate_cluster" + stage);
                }
                if (crate.powerUpType == PowerUpType.RANDOM) {
                    // because I only made one texture for this powerup
                    if (stage == "") {
                        return getTextureID("crate_question_bold");
                    }
                }
                if (crate.powerUpType == PowerUpType.SPEED) {
                    // because I only made one texture for this powerup
                    if (stage == "") {
                        return getTextureID("crate_speed");
                    }
                }
                if (crate.powerUpType == PowerUpType.REVERSE) {
                    // because I only made one texture for this powerup
                    if (stage == "") {
                        return getTextureID("crate_reverse");
                    }
                }
                if (crate.powerUpType == PowerUpType.REVERSEOTHERS) {
                    // because I only made one texture for this powerup
                    if (stage == "") {
                        return getTextureID("crate_reverse_others");
                    }
                }
                if (crate.powerUpType == PowerUpType.REVERSEOTHERS) {
                    // because I only made one texture for this powerup
                    if (stage == "") {
                        return getTextureID("crate_extra_a");
                    }
                }
                if (crate.powerUpType == PowerUpType.DAMAGE) {
                    // because I only made one texture for this powerup
                    if (stage == "") {
                        return getTextureID("crate_damage");
                    }
                }
                if (crate.powerUpType == PowerUpType.TARGETING) {
                    // because I only made one texture for this powerup
                    if (stage == "") {
                        return getTextureID("crate_targeting");
                    }
                }
                return getTextureID("crate" + stage);
            default:
                return getTextureID("uvgrid1");
        }
    }

    /**
     * Cycle all held animations in the animationCache, including those within texUIElements.
     *
     * @param gl The GUILayer holding any animations you'd like to cycle
     */
    public static void cycleAnimations(GUILayer gl) {
        Iterator<Map.Entry<Integer, Animation>> it = animationCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Animation> entry = it.next();
            int texID = entry.getValue().cycle();
            if (texID == -1) {
                it.remove();
                entityTextures.remove(entry.getKey());
            } else {
                entityTextures.put(entry.getKey(), texID);
            }
        }
        Iterator<Map.Entry<String, Animation>> itUI = uiAnimationCache.entrySet().iterator();
        while (itUI.hasNext()) {
            Map.Entry<String, Animation> entry = itUI.next();
            int texID = entry.getValue().cycle();
            if (texID == -1) {
                itUI.remove();
                gl.getTexUIElements().get(entry.getKey()).resetTex();
            } else {
                gl.getTexUIElements().get(entry.getKey()).texID = texID;
            }
        }
    }

    /**
     * Set the current animation for an entity at the given index
     *
     * @param index The index of the animation within the cache. Use the index of the Entity in EntityHandler
     * @param anim  The animation to assign
     */
    public static void setAnimation(int index, Animation anim) {
        if (animationCache.get(index) != null) {
            if (animationCache.get(index).equals(anim)) {
                return;
            }
        }
        animationCache.put(index, anim);
    }

    /**
     * Set the current animation for a given entity
     *
     * @param e    The Entity to assign the animation to
     * @param anim The Animation to assign
     */
    public static void setAnimation(Entity e, Animation anim) {
        if (animationCache.get(e.loc) != null) {
            if (animationCache.get(e.loc).equals(anim)) {
                return;
            }
        }
        animationCache.put(e.loc, anim);
    }

    /**
     * Set the current animation for a given UI element
     *
     * @param name The name of the texUIElement to assign the animation to
     * @param anim The Animation to assign
     */
    public static void setUIAnimation(String name, Animation anim) {
        if (uiAnimationCache.get(name) != null) {
            if (uiAnimationCache.get(name).equals(anim)) {
                return;
            }
        }
        uiAnimationCache.put(name, anim);
    }

    /**
     * Clear all animations within TextureHandler's animationCache
     */
    public static void wipeAnimations() {
        animationCache.clear();
        entityTextures.clear();
    }

    /**
     * Clear the Animation at the given index
     */
    public static void wipeAnimation(int index) {
        animationCache.remove(index);
        entityTextures.remove(index);
    }

    /**
     * Clear the Animation for the given Entity
     */
    public static void wipeAnimation(Entity e) {
        animationCache.remove(e.loc);
        entityTextures.remove(e.loc);
    }

    /**
     * Clear the Animation for the given texUIElement
     */
    public static void wipeAnimation(String name) {
        uiAnimationCache.remove(name);
    }

    /**
     * Clean up the TextureHandler by deleting all textures held by OpenGL
     */
    public static void cleanup() {
        for (HashMap.Entry<String, Integer> entry : textures.entrySet()) {
            glDeleteTextures(entry.getValue());
        }
    }
}

