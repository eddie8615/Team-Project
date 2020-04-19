package org.cauldron.ui.elements;
/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */

import org.cauldron.database.DBHandler;
import org.cauldron.database.Database;
import org.cauldron.scenes.Scene;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_messageBox;


/**
 * Java port of
 * <a href="https://github.com/vurtun/nuklear/blob/master/demo/glfw_opengl3/main.c">https://github.com/vurtun/nuklear/blob/master/demo/glfw_opengl3/main.c</a>.
 */
public class Login extends UIElement {

    //    private static final int EASY = 0;
//    private static final int HARD = 1;
//
    NkColor background = NkColor.create()
            .set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
    private final int uiHeight = 400;
    private final int uiWidth = 300;
    private TextField userID;
    private TextField password;
    private DBHandler dbClient;
    private int windowOptions = NK_WINDOW_TITLE;
    private int editOptions = NK_EDIT_FIELD | NK_EDIT_ALWAYS_INSERT_MODE | NK_EDIT_AUTO_SELECT
            | NK_EDIT_SELECTABLE | NK_EDIT_CLIPBOARD;
    private final float[] ratio = {0.1f, 0.8f, 0.1f};

    public Login(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);

        userID = new TextField(20, false);
        password = new TextField(20, false);
        dbClient = new DBHandler();

    }

    @Override
    public void layout() {
        try (MemoryStack stack = stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);

            setUserDataUUID();
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, uiWidth, uiHeight, rect),
                    NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_BORDER

                    //windowOptions
            )) {

                //ctx.style().window().fixed_background().data().color().set((byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xFF);
                nk_layout_row_dynamic(ctx, 30, 1);
                nk_label(ctx, "Username", NK_TEXT_ALIGN_LEFT);


                nk_layout_row_static(ctx, 30, 250, 1);
                nk_edit_string(ctx,
                        editOptions,
                        userID.getContent(),
                        userID.getLength(),
                        userID.maxLength(),
                        userID.getPluginFilter());

                nk_layout_row_dynamic(ctx, 30, 1);
                nk_label(ctx, "Password", NK_TEXT_ALIGN_LEFT);

                if (nk_input_is_key_pressed(ctx.input(), NK_KEY_TAB)) {
                    nk_edit_focus(ctx, NK_EDIT_DEFAULT);
                }

                nk_layout_row_static(ctx, 30, 250, 1);

                {
                    int old_len = password.getLength().get(0);
                    ByteBuffer buffer = BufferUtils.createByteBuffer(password.maxLength());

                    for (int i = 0; i < password.getLength().get(0); ++i) {
                        buffer.put(i, (byte) '*');
                    }
                    password.setBlind(buffer);

                    nk_edit_string(ctx,
                            editOptions,
                            password.getBlind(),
                            password.getLength(),
                            password.maxLength(),
                            password.getPluginFilter());


                    if (old_len < password.getLength().get(0)) {
                        byte lastByte = password.getLastByte();
                        password.getContent().put(old_len, lastByte);

                    }
                }

                nk_layout_row_static(ctx, 30, 80, 2);

                if (nk_button_label(ctx, "Sign in") || nk_input_is_key_down(ctx.input(), NK_KEY_ENTER)) {
                    Database user = new Database(userID.getValue(), password.getValue());
                    if (dbClient.verify(user)) {
                        System.out.println("Login success");
                        Scene.userInfo = user;
                        UIHandler.disable(name);
                        UIHandler.enable("LobbySystem");
//                        UIHandler.game.setScene(LOBBY);
                    } else {
                        String msg = "Invalid password or cannot find the user";
                        tinyfd_messageBox("Error!", msg, "Cancel", "", true);
                    }
                }

                if (nk_button_label(ctx, "Register")) {
                    UIHandler.toggle(name);
                    System.out.println(UIHandler.getUIElements().get("Register").isEnabled());
                    UIHandler.toggle("Register");
                    System.out.println(UIHandler.getUIElements().get("Register").isEnabled());
                }

                nk_layout_row_static(ctx, 30, 80, 1);
                nk_spacing(ctx, 1);
//                nk_layout_row(ctx, NK_DYNAMIC, 40, ratio);
//                nk_layout_row_static(ctx, uiHeight *0.4f, 150, 1);
                if (nk_button_label(ctx, "Previous")) {
//                    Game.changeSceneFlag = true;
//                    Game.destScene = MAINMENU;
                    UIHandler.disable(name);
                    UIHandler.enable("MainMenu");
                    UIHandler.enable("Title");
                }

                nk_spacing(ctx, 1);
                nk_layout_row_end(ctx);

            }
            nk_end(ctx);
            resetUserDataUUID();
        }
    }

}

