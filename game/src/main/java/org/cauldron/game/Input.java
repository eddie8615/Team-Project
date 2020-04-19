package org.cauldron.game;

import org.cauldron.renderer.layers.GUILayer;
import org.cauldron.scenes.OfflineScene;
import org.cauldron.scenes.OnlineScene;
import org.cauldron.scenes.Scene;
import org.cauldron.ui.UIHandler;
import org.cauldron.ui.elements.Scoreboard;
import org.joml.Vector2f;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkMouse;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.cauldron.game.Game.SceneType.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class Input {

    private static long window;

    private static NkContext nkCtx = null;
    public static Set<Integer> keysPressed = new HashSet<Integer>();
    public static Vector2f cursorPos = new Vector2f(0, 0);
    public static double mouseHeldTime = -1;
    public static double currentMouseHeldTime = 0;
    public static double lastMouseButtonPress = System.currentTimeMillis();
    public static boolean isMouseDown = false;
    public static String lastActiveWindow = null;

    public static void init(long w) {
        window = w;
    }

    /**
     * Enable copy and paste within the Nuklear context
     */
    private static void setupClipboard() {
        nkCtx.clip()
                .copy((handle, text, len) -> {
                    if (len == 0) {
                        return;
                    }

                    try (MemoryStack stack = stackPush()) {
                        ByteBuffer str = stack.malloc(len + 1);
                        memCopy(text, memAddress(str), len);
                        str.put(len, (byte) 0);

                        glfwSetClipboardString(window, str);
                    }
                })
                .paste((handle, edit) -> {
                    long text = nglfwGetClipboardString(window);
                    if (text != NULL) {
                        nnk_textedit_paste(edit, text, nnk_strlen(text));
                    }
                });
    }

    /**
     * @return the currently active UI window
     */
    private static String getActiveNkWindow() {
        for (String name : UIHandler.getUIElements().keySet()) {
            if (nk_window_is_active(nkCtx, name)) {
                return name;
            }
        }
        return "";
    }

    /**
     * Sets up all callbacks for GLFW. Defines all base input behaviour.
     * Any keys pressed are held in keysPressed.
     */
    private static void setupCallbacks() {
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            try (MemoryStack stack = stackPush()) {
                NkVec2 scroll = NkVec2.mallocStack(stack)
                        .x((float) xoffset)
                        .y((float) yoffset);
                nk_input_scroll(nkCtx, scroll);
            }
        });
        glfwSetCharCallback(window, (window, codepoint) -> nk_input_unicode(nkCtx, codepoint));
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            boolean press = action == GLFW_PRESS;
            // Ensure key presses over clickable UI within OfflineScene or OnlineScene don't get sent to the game
            if ((Game.currentScene.getType() == OFFLINE || Game.currentScene.getType() == ONLINE)
                    && !UIHandler.getUIElements().get("Settings").isEnabled()
                    && !UIHandler.getUIElements().get("InGameMenu").isEnabled() && press) {
                keysPressed.add(key);
            }
            if (action == GLFW_ANY_RELEASE_BEHAVIOR)
                keysPressed.remove(key);
            switch (key) {
                case GLFW_KEY_ESCAPE:
                    if ((Game.currentScene.getType() == OFFLINE || Game.currentScene.getType() == ONLINE) && action == GLFW_RELEASE) {
                        UIHandler.toggle("InGameMenu");
                        if (UIHandler.getUIElements().get("Settings").isEnabled()) {
                            UIHandler.disable("Settings");
                            UIHandler.toggle("InGameMenu");
                        }
                        if (Game.currentScene.getType() == OFFLINE) {
                            OfflineScene offline = (OfflineScene) Game.currentScene;
                            offline.paused = !offline.paused;
                        }
                    }
                    if (UIHandler.getUIElements().get("OnlineWaiting").isEnabled()) {
                        Game.changeSceneFlag = true;
                        Game.destScene = MAINMENU;
                        UIHandler.enable("Title");
                        UIHandler.enable("MainMenu");
                    }
                    break;
                case GLFW_KEY_TAB:
                    if ((Game.currentScene.getType() == OFFLINE || Game.currentScene.getType() == ONLINE)
                            && press && !UIHandler.getUIElements().get("Settings").isEnabled()
                            && !UIHandler.getUIElements().get("InGameMenu").isEnabled()
                            && !UIHandler.getUIElements().get("MainMenu").isEnabled()
                            && !Game.currentScene.gameOver) {
                        Scene scene = Game.currentScene;
                        UIHandler.enable("Scoreboard");
                        Scoreboard sb = (Scoreboard) UIHandler.getUIElements().get("Scoreboard");
                        sb.teamsList = scene.teamsList;
                        sb.scores = scene.scores;
                        if (Game.currentScene.getType() == ONLINE) {
                            sb.tankToUsernameMap = ((OnlineScene) scene).getClient().tankToUsernameMap;
                        }
                    } else {
                        nk_input_key(nkCtx, NK_KEY_TAB, press);
                    }
                    if (action == GLFW_RELEASE
                            && !Game.currentScene.gameOver) {
                        UIHandler.disable("Scoreboard");
                        System.out.println("TAB LET GO");
                    }
                    break;
                case GLFW_KEY_DELETE:
                    nk_input_key(nkCtx, NK_KEY_DEL, press);
                    break;
                case GLFW_KEY_ENTER:
                    nk_input_key(nkCtx, NK_KEY_ENTER, press);
                    break;
                case GLFW_KEY_BACKSPACE:
                    nk_input_key(nkCtx, NK_KEY_BACKSPACE, press);
                    break;
                case GLFW_KEY_UP:
                    nk_input_key(nkCtx, NK_KEY_UP, press);
                    break;
                case GLFW_KEY_DOWN:
                    nk_input_key(nkCtx, NK_KEY_DOWN, press);
                    break;
                case GLFW_KEY_LEFT:
                    nk_input_key(nkCtx, NK_KEY_LEFT, press);
                    break;
                case GLFW_KEY_RIGHT:
                    nk_input_key(nkCtx, NK_KEY_RIGHT, press);
                    break;
                case GLFW_KEY_HOME:
                    nk_input_key(nkCtx, NK_KEY_TEXT_START, press);
                    nk_input_key(nkCtx, NK_KEY_SCROLL_START, press);

                    break;
                case GLFW_KEY_END:
                    nk_input_key(nkCtx, NK_KEY_TEXT_END, press);
                    nk_input_key(nkCtx, NK_KEY_SCROLL_END, press);
                    break;
                case GLFW_KEY_PAGE_DOWN:
                    nk_input_key(nkCtx, NK_KEY_SCROLL_DOWN, press);
                    break;
                case GLFW_KEY_PAGE_UP:
                    nk_input_key(nkCtx, NK_KEY_SCROLL_UP, press);
                    break;
                case GLFW_KEY_LEFT_SHIFT:
                case GLFW_KEY_RIGHT_SHIFT:
                    nk_input_key(nkCtx, NK_KEY_SHIFT, press);
                    break;
                case GLFW_KEY_LEFT_CONTROL:
                case GLFW_KEY_RIGHT_CONTROL:
                    if (press) {
                        nk_input_key(nkCtx, NK_KEY_COPY, glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_PASTE, glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_CUT, glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_TEXT_UNDO, glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_TEXT_REDO, glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_TEXT_WORD_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_TEXT_WORD_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_TEXT_LINE_START, glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_TEXT_LINE_END, glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS);
                    } else {
                        nk_input_key(nkCtx, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
                        nk_input_key(nkCtx, NK_KEY_COPY, false);
                        nk_input_key(nkCtx, NK_KEY_PASTE, false);
                        nk_input_key(nkCtx, NK_KEY_CUT, false);
                        nk_input_key(nkCtx, NK_KEY_SHIFT, false);
                    }
                    break;
            }
        });

        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            try (MemoryStack stack = stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);

                glfwGetFramebufferSize(window, width, height);
                float x = (float) xpos * (1600f / width.get(0));
                float y = 900f - ((float) ypos * (900f / height.get(0)));
                // Ensure that nothing happens in the game when the cursor moves over a widget
                nk_input_motion(nkCtx, (int) x, (int) -y);

                if ((Game.currentScene.getType() == OFFLINE || Game.currentScene.getType() == ONLINE)
                        && !UIHandler.getUIElements().get("Settings").isEnabled()
                        && !UIHandler.getUIElements().get("InGameMenu").isEnabled()) {
                    cursorPos = new Vector2f(x, y);
                } else {
                    cursorPos = new Vector2f(-1, -1);
                }
            }
        });
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            try (MemoryStack stack = stackPush()) {
                DoubleBuffer cx = stack.mallocDouble(1);
                DoubleBuffer cy = stack.mallocDouble(1);

                glfwGetCursorPos(window, cx, cy);

                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);

                glfwGetFramebufferSize(window, width, height);

                int x = (int) (cx.get(0) * (1600f / width.get(0)));
                int y = -(900 - (int) (cy.get(0) * (900f / height.get(0))));

                int nkButton;
                switch (button) {
                    case GLFW_MOUSE_BUTTON_RIGHT:
                        nkButton = NK_BUTTON_RIGHT;
                        break;
                    case GLFW_MOUSE_BUTTON_MIDDLE:
                        nkButton = NK_BUTTON_MIDDLE;
                        break;
                    default:
                        nkButton = NK_BUTTON_LEFT;
                }

                if (lastActiveWindow == null)
                    lastActiveWindow = getActiveNkWindow();

                if ((Game.currentScene.getType() == OFFLINE || Game.currentScene.getType() == ONLINE)
                        && !Game.currentScene.gameOver
                        && !UIHandler.getUIElements().get("Settings").isEnabled()
                        && !UIHandler.getUIElements().get("InGameMenu").isEnabled() && action == GLFW_PRESS) {
                    isMouseDown = true;
                    mouseHeldTime = -1;
                    lastMouseButtonPress = System.currentTimeMillis();
                } else {
                    nk_input_button(nkCtx, nkButton, x, y, action == GLFW_PRESS);
                }

                if (action == GLFW_RELEASE) {
                    isMouseDown = false;
                    mouseHeldTime = (System.currentTimeMillis() - lastMouseButtonPress) / 1000f;
                    System.out.println("Mouse held for " + mouseHeldTime + " seconds");
                }
            }
        });
    }

    /**
     * Switch out the GUILayer in use, to maintain the Nuklear context.
     *
     * @param guiLayer
     */
    public static void setGUILayer(GUILayer guiLayer) {
        nkCtx = guiLayer.getContext();
        setupClipboard();
        setupCallbacks();
    }

    /**
     * Cycle input; poll input events and handle mouse grabbing within the window
     */
    public static void cycle() {
        nk_input_begin(nkCtx);
        //Game notices that the key is down
        glfwPollEvents();

        NkMouse mouse = nkCtx.input().mouse();
        if (mouse.grab()) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        } else if (mouse.grabbed()) {
            float prevX = mouse.prev().x();
            float prevY = mouse.prev().y();
            glfwSetCursorPos(window, prevX, prevY);
            mouse.pos().x(prevX);
            mouse.pos().y(prevY);

        } else if (mouse.ungrab()) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }

        if (isMouseDown) {
            currentMouseHeldTime = (System.currentTimeMillis() - lastMouseButtonPress) / 1000f;
        } else {
            currentMouseHeldTime = 0;
        }

        nk_input_end(nkCtx);
    }
}
