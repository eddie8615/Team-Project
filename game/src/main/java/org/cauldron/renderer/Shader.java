package org.cauldron.renderer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.lwjgl.opengl.GL30.*;

/**
 * Handles shader functionality in OpenGL, and creates and binds shader programs.
 */
public class Shader {
    private int rendererId;
    private int vsId = 0;
    private int fsId = 0;
    private HashMap<String, Integer> uniformIds = new HashMap<String, Integer>();
    // Create a FloatBuffer with the proper size to store our matrices later
    private FloatBuffer matrix44Buffer = BufferUtils.createFloatBuffer(16);

    /**
     * @param vsPath          The path of the vertex shader
     * @param fsPath          The path of the fragment shader
     * @param attribLocations The ID's of the shader attributes in OpenGL
     */
    public Shader(String vsPath, String fsPath, HashMap<String, Integer> attribLocations) {
        String vsSource = parseShader(vsPath);
        String fsSource = parseShader(fsPath);
        // Load the vertex and fragment shaders
        vsId = compileShader(vsSource, GL_VERTEX_SHADER);
        fsId = compileShader(fsSource, GL_FRAGMENT_SHADER);

        createProgram(attribLocations);
    }

    /**
     * Deletes the shader program from the OpenGL context.
     */
    public void close() {
        GL20.glDeleteProgram(rendererId);
    }

    /**
     * Sets the shader program used by OpenGL to the program at the ID,
     * binding the current shader program to the OpenGL context.
     */
    public void bind() {
        glUseProgram(rendererId);
    }

    /**
     * Sets the shader program used by OpenGL to 0, unbinding the current shader program.
     */
    public void unbind() {
        glUseProgram(0);
    }

    /**
     * Binds a matrix uniform of a given name to its location in the shader.
     * The name passed must be the same as the corresponding name in the shader.
     *
     * @param name   The name of the uniform in the shader
     * @param matrix The matrix to set the uniform to
     */
    public void setMatrix4f(String name, Matrix4f matrix) {
        glUniformMatrix4fv(getUniformLocation(name), false, matrix.get(matrix44Buffer));
    }

    /**
     * Binds a singleton uniform of a given name to its location in the shader.
     * The name passed must be the same as the corresponding name in the shader.
     *
     * @param name    The name of the uniform in the shader
     * @param uniform The uniform1 to set the uniform to
     */
    public void setUniform1i(String name, int uniform) {
        glUniform1i(getUniformLocation(name), uniform);
    }

    /**
     * Gets the location of a given uniform within the shader.
     * If it already exists in the cache (HashMap of Strings and location IDs),
     * return it from there without calling glGetUniformLocation.
     *
     * @param name The name of the uniform to get the location of
     * @return The ID in OpenGL of the corresponding uniform
     */
    private int getUniformLocation(String name) {
        // Get matrices uniform locations if not already retrieved
        if (!uniformIds.containsKey(name)) {
            int uniformId = glGetUniformLocation(rendererId, name);
            uniformIds.put(name, uniformId);
            return uniformId;
        } else {
            return uniformIds.get(name);
        }
    }

    /**
     * @param sPath Path of the shader to parse
     * @return String of the shader file contents
     */
    private String parseShader(String sPath) {
        StringBuilder shaderSource = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream(sPath), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not read file.");
            e.printStackTrace();
            System.exit(-1);
        }
        return shaderSource.toString();
    }

    /**
     * Creates a shader program in OpenGL, setting the shader's ID as its program ID in OpenGL,
     * and attaches the vertex and fragment shaders to this new program.
     * Then the attributes and their corresponding integer locations in OpenGL are bound to the shader location.
     * The shader program is then linked and validated by OpenGL
     *
     * @param attribLocations Attribute names and their corresponding integer addresses
     */
    private void createProgram(HashMap<String, Integer> attribLocations) {
        // Link the shaders into an OpenGL program
        rendererId = glCreateProgram();
        glAttachShader(rendererId, vsId);
        glAttachShader(rendererId, fsId);

        for (HashMap.Entry<String, Integer> entry : attribLocations.entrySet()) {
            glBindAttribLocation(rendererId, entry.getValue(), entry.getKey());
        }

        glLinkProgram(rendererId);
        glValidateProgram(rendererId);

        int status = glGetProgrami(rendererId, GL_LINK_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(rendererId));
        }
    }

    /**
     * @param shaderSource A String of shader source code to compile into a shader
     * @param type         The type of shader to create
     * @return
     */
    private int compileShader(String shaderSource, int type) {
        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);

        int status = glGetShaderi(shaderID, GL_COMPILE_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException(glGetShaderInfoLog(shaderID));
        }
        return shaderID;
    }

    /**
     * @return The ID of the shader (its program's address in the OpenGL context
     */
    public int getId() {
        return rendererId;
    }
}
