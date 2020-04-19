package org.cauldron.game;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;


public class Window {

    private int width = 1280;
    private int height = 720;
    // The window handle
    private long window;
    private Callback debugProc;

    private long frameCount = 0;

    /**
     * The root method of the whole game, called only by main() in class Main.
     * Initialises OpenGL, runs the loop and cleans up.
     */
    public void run() {
        init();

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        System.out.println("LWJGL version: " + Version.getVersion());
        System.out.println("OpenGL version: " + glGetString(GL_VERSION));
        //debugProc = GLUtil.setupDebugMessageCallback();
        glEnable(GL_DEPTH_TEST);

        int[] maxRes = getMaxSupportedRes();
        Settings.maxSupportedRes = maxRes[0] + "x" + maxRes[1];
        Settings.read();

        // INIT GAME
        Game.init(window);

        Game.startFPSTime = System.nanoTime();

        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Delete VAO and all VBOs
        //Game.cleanup();
        Game.cleanup();

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Initialise GLFW and create a window
     */
    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_STENCIL_BITS, 4);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(width, height, "BLAST", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Recalculate the viewport upon window resize
        glfwSetFramebufferSizeCallback(window, (window, w, h) -> {
            System.out.println("Resized to: " + w + "x" + h);
            if (w > 0 && h > 0) {
                glViewport(0, 0, w, h);
            }
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
            glfwSetWindowAspectRatio(window, 16, 9);
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Make the window visible
        glfwShowWindow(window);
    }

    /**
     * Changes GLFW flags according to settings enabled in Settings.
     */
    private void processSettings() {
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (Settings.vsync) {
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }
        if (Settings.fullscreen && glfwGetWindowMonitor(window) == NULL) {
            glfwSetWindowMonitor(window, glfwGetPrimaryMonitor(), 0, 0, mode.width(), mode.height(), mode.refreshRate());
        }
        if (!Settings.fullscreen && glfwGetWindowMonitor(window) != NULL) {
            glfwSetWindowMonitor(window, NULL, 0, 0, mode.width(), mode.height(), mode.refreshRate());
            width = Settings.resInt[0];
            height = Settings.resInt[1];
            glfwSetWindowSize(window, width, height);
            glfwSetWindowPos(
                    window,
                    (mode.width() - width) / 2,
                    (mode.height() - height) / 2
            );
        }
        if (Settings.resInt[0] != width && Settings.resInt[1] != height && glfwGetWindowMonitor(window) == NULL) {
            width = Settings.resInt[0];
            height = Settings.resInt[1];
            glfwSetWindowSize(window, width, height);
            glfwSetWindowPos(
                    window,
                    (mode.width() - width) / 2,
                    (mode.height() - height) / 2
            );
        }
    }

    /**
     * Root loop of the graphics instance
     */
    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            processSettings();

            Game.cycle();

            glfwSwapBuffers(window); // swap the buffers
        }
    }

    /**
     * @return the maximum supported resolution of the primary monitor, in the format int[]{x, y}
     */
    public int[] getMaxSupportedRes() {
        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return new int[]{vid.width(), vid.height()};
    }

    public long getWindow() {
        return window;
    }
}
