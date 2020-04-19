package org.cauldron.ui.elements;

import org.cauldron.database.DBHandler;
import org.cauldron.database.Database;
import org.cauldron.ui.UIElement;
import org.cauldron.ui.UIHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_messageBox;

public class Register extends UIElement {
    private TextField userID;
    private TextField password;
    private TextField Cpassword;
    private TextField email;
    private DBHandler dbClient;
    private int action = 0;
    private int windowOptions = NK_WINDOW_TITLE;
    private int editOptions = NK_EDIT_FIELD | NK_EDIT_ALWAYS_INSERT_MODE | NK_EDIT_AUTO_SELECT
            | NK_EDIT_SELECTABLE | NK_EDIT_CLIPBOARD;

    public Register(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);

        userID = new TextField(20, false);
        password = new TextField(20, false);
        Cpassword = new TextField(20, false);
        email = new TextField(40, false);
        dbClient = new DBHandler();
    }

    public void layout() {
        try (MemoryStack stack = stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);
            boolean isTabPressed = false;
            setUserDataUUID();

            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, 300, 430, rect),
                    NK_WINDOW_NO_SCROLLBAR
                    //windowOptions
            )) {
                //ctx.style().window().fixed_background().data().color().set((byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xFF);
                nk_layout_row_dynamic(ctx, 30, 1);
                nk_label(ctx, "Username", NK_TEXT_ALIGN_LEFT);

                nk_layout_row_static(ctx, 30, 250, 1);

                if (nk_input_is_key_pressed(ctx.input(), NK_KEY_TAB) && action % 4 == 0) {
                    nk_edit_focus(ctx, NK_EDIT_DEFAULT);
                    isTabPressed = true;
                }
                //textbox 0
                nk_edit_string(ctx,
                        editOptions,
                        userID.getContent(),
                        userID.getLength(),
                        userID.maxLength(),
                        userID.getPluginFilter());


                nk_layout_row_dynamic(ctx, 30, 1);
                nk_label(ctx, "Password", NK_TEXT_ALIGN_LEFT);


                nk_layout_row_static(ctx, 30, 250, 1);

                //textbox1
                if (nk_input_is_key_pressed(ctx.input(), NK_KEY_TAB) && action % 4 == 1) {
                    nk_edit_focus(ctx, NK_EDIT_DEFAULT);
                    isTabPressed = true;
                }

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
                nk_layout_row_dynamic(ctx, 30, 1);
                nk_label(ctx, "Confirm password", NK_TEXT_ALIGN_LEFT);

                nk_layout_row_static(ctx, 30, 250, 1);

                if (nk_input_is_key_pressed(ctx.input(), NK_KEY_TAB) && action % 4 == 2) {
                    nk_edit_focus(ctx, NK_EDIT_DEFAULT);
                    isTabPressed = true;
                }

                {
                    int old_len = Cpassword.getLength().get(0);
                    ByteBuffer buffer = BufferUtils.createByteBuffer(Cpassword.maxLength());

                    for (int i = 0; i < Cpassword.getLength().get(0); ++i) {
                        buffer.put(i, (byte) '*');
                    }

                    Cpassword.setBlind(buffer);
                    nk_edit_string(ctx,
                            editOptions,
                            Cpassword.getBlind(),
                            Cpassword.getLength(),
                            Cpassword.maxLength(),
                            Cpassword.getPluginFilter());

                    if (old_len < Cpassword.getLength().get(0)) {
                        byte lastByte = Cpassword.getLastByte();
                        Cpassword.getContent().put(old_len, lastByte);
                    }
                }

                nk_layout_row_dynamic(ctx, 30, 1);
                nk_label(ctx, "Email Address", NK_TEXT_ALIGN_LEFT);


                nk_layout_row_static(ctx, 30, 250, 1);

                if (nk_input_is_key_pressed(ctx.input(), NK_KEY_TAB) && action % 4 == 3) {
                    nk_edit_focus(ctx, NK_EDIT_DEFAULT);
                    isTabPressed = true;
                }
                //textbox2
                nk_edit_string(ctx,
                        editOptions,
                        email.getContent(),
                        email.getLength(),
                        email.maxLength(),
                        email.getPluginFilter());

                nk_layout_row_static(ctx, 30, 80, 2);

                if (nk_button_label(ctx, "Sign up")) {
                    Database newDB;
                    if (userID.getValue().length() == 0
                            || password.getValue().length() == 0
                            || email.getValue().length() == 0) {

                        String msg = "You missed something";
                        tinyfd_messageBox("Error Msg", msg, "okcancel", "", true);
                    } else if (!password.getValue().equals(Cpassword.getValue())) {
                        String msg = "The passwords don't match each other";
                        tinyfd_messageBox("Error", msg, "ok", "", true);
                    } else if (!validEmail(email.getValue())) {
                        String msg = "The email is invalid";
                        tinyfd_messageBox("Error", msg, "ok", "", true);
                    } else {
                        newDB = new Database(userID.getValue(), email.getValue(), password.getValue());
                        if (!dbClient.isExist(newDB.getUserID())) {
                            dbClient.save(newDB);
                            System.out.println("Register successful");
//								 
//								 UIHandler.disable("Register");
//								 UIHandler.enable("Login");
                            UIHandler.uiProgress(name, "Login");
                        } else {
                            String msg = "The user ID already exists";
                            tinyfd_messageBox("Error Msg", msg, "okcancel", "", true);
                        }
                    }

                }

                if (nk_button_label(ctx, "Return")) {
//						 UIHandler.disable("Register");
//						 UIHandler.enable("Login");
                    UIHandler.uiProgress(name, "Login");

                }

                if (isTabPressed)
                    action++;
            }
            resetUserDataUUID();
            nk_end(ctx);
        }
    }

    private boolean validEmail(String s) {
        return s.matches("^([a-zA-Z0-9_\\-\\.\\+]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
    }
}
