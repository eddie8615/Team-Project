package org.cauldron.ui.elements;

import org.cauldron.entity.components.Stats;
import org.cauldron.game.Game;
import org.cauldron.renderer.TextureHandler;
import org.cauldron.ui.UIElement;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkCommandBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

import static org.cauldron.game.Game.SceneType.MAINMENU;
import static org.lwjgl.nuklear.Nuklear.*;

/**
 * Used for both the in game scoreboard that can be viewed by holding tab and also for the game over scoreboard which is displayed at the end of games.
 *
 * <img src="https://i.ibb.co/5hc2DFf/image.png" alt="screenshot of scoreboard in game over mode.>
 */
public class Scoreboard extends UIElement {
    public ArrayList<ArrayList<String>> teamsList;
    public int[] scores;
    public Map<String, String> tankToUsernameMap;
    public long timeRemaining;
    private NkColor white = NkColor.create().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);

    private ByteBuffer buffer = BufferUtils.createByteBuffer(120);
    private PointerBuffer cur = PointerBuffer.create(buffer);

    public boolean gameFinished;

    public Stats youStats;
    public Stats AIStats;

    public Scoreboard(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
        initSB();
    }

    public void initSB() {
        gameFinished = false;
        timeRemaining = 0;
        scores = new int[]{0, 0};
        teamsList = new ArrayList<>();
    }

    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);
            ctx.style().window().fixed_background().data().color().set((byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xAA);
            setUserDataUUID();
            int height = 280;
            int flags = NK_WINDOW_NO_INPUT | NK_WINDOW_NO_SCROLLBAR;
            if (gameFinished) {
                height = 500;
                flags = NK_WINDOW_NO_SCROLLBAR;
            }
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, 600, height, rect),
                    flags
            )) {

                nk_layout_row_dynamic(ctx, 10, 1);

                if (!gameFinished) {
                    nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);
                    nk_layout_row_dynamic(ctx, 30, 1);
                    nk_label_colored(ctx, "Scoreboard", NK_TEXT_ALIGN_CENTERED, white);
                } else {
                    nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);
                    nk_layout_row_static(ctx, 150, 100, 1);
                    NkRect picRect = NkRect.create();
                    NkCommandBuffer cmd = nk_window_get_canvas(ctx);
                    nk_draw_image(cmd, nk_rect(x + 30, y + 20, 540, 60, picRect), TextureHandler.getTextureNk("game_over"), white);
                    if (scores[0] > scores[1]) {
                        nk_draw_image(cmd, nk_rect(x + 30, y + 100, 540, 43, picRect), TextureHandler.getTextureNk("team_1_wins"), white);
                    } else if (scores[1] > scores[0]) {
                        nk_draw_image(cmd, nk_rect(x + 30, y + 100, 540, 43, picRect), TextureHandler.getTextureNk("team_2_wins"), white);
                    } else {
                        nk_draw_image(cmd, nk_rect(x + 30, y + 100, 540, 43, picRect), TextureHandler.getTextureNk("tie"), white);
                    }

                }
                if (Game.currentScene.getType() == Game.SceneType.OFFLINE | Game.currentScene.getType() == Game.SceneType.ONLINE) {
                    if (Game.currentScene.getType() == Game.SceneType.OFFLINE) {
                        layoutOffline();
                    } else if (!teamsList.isEmpty()) {
                        layoutOnline();
                    }
                    if (gameFinished) {
                        drawBackButton();
                    }
                }
            }
            resetUserDataUUID();
            nk_end(ctx);
        } catch (Exception e) {
            System.out.println("Scoreboard error");
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
        }
    }

    /**
     * draws the back button which will take the user back to the main menu after the game has finished.
     */
    private void drawBackButton() {
        nk_layout_row_dynamic(ctx, 50, 1);
        nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);
        nk_layout_row_dynamic(ctx, 50, 3);
        nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);
        if (nk_button_label(ctx, "RETURN TO MENU")) {
            Game.changeSceneFlag = true;
            Game.destScene = MAINMENU;
            System.out.println("return to menu button pressed");
        }
    }


    /**
     * the layout used for when the game is in online mode
     */
    private void layoutOnline() {
        nk_layout_row_dynamic(ctx, 40, 2);

        nk_label_colored(ctx, "Team 1", NK_TEXT_ALIGN_LEFT, white);
        nk_label_colored(ctx, Integer.toString(scores[0]), NK_TEXT_ALIGN_LEFT, white);

        nk_layout_row_dynamic(ctx, 40, teamsList.get(0).size());
        for (int i = 0; i < teamsList.get(0).size(); i++) {
            nk_label_colored(ctx, tankToUsernameMap.get(teamsList.get(0).get(i)), NK_TEXT_ALIGN_LEFT, white);
        }

        nk_layout_row_dynamic(ctx, 40, 2);
        nk_label_colored(ctx, "Team 2", NK_TEXT_ALIGN_LEFT, white);
        nk_label_colored(ctx, Integer.toString(scores[1]), NK_TEXT_ALIGN_LEFT, white);

        nk_layout_row_dynamic(ctx, 40, teamsList.get(1).size());
        for (int i = 0; i < teamsList.get(1).size(); i++) {
            nk_label_colored(ctx, tankToUsernameMap.get(teamsList.get(1).get(i)), NK_TEXT_ALIGN_LEFT, white);
        }
    }

    /**
     * <p>The layout used when the game is in offline mode</p>
     *
     * <img src="https://i.ibb.co/gTyPgHQ/image.png" alt="screenshot of the scoreboard in offline mode">
     */
    private void layoutOffline() {
        // header
        nk_layout_row_dynamic(ctx, 30, 4);
        nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);
        nk_label_colored(ctx, "YOU", NK_TEXT_ALIGN_CENTERED, white);
        nk_label_colored(ctx, "AI TANK", NK_TEXT_ALIGN_CENTERED, white);
        nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);

        // kills bar
        nk_layout_row_dynamic(ctx, 15, 1);
        nk_label_colored(ctx, "KILLS", NK_TEXT_ALIGN_CENTERED, white);
        nk_layout_row_dynamic(ctx, 15, 4);
        nk_label_colored(ctx, Integer.toString(AIStats.deaths), NK_TEXT_ALIGN_RIGHT, white);
        layoutLeftBar(BigInteger.valueOf(scaleTo100(AIStats.deaths, youStats.deaths + AIStats.deaths)));
        layoutRightBar(BigInteger.valueOf(scaleTo100(youStats.deaths, youStats.deaths + AIStats.deaths)));
        nk_label_colored(ctx, Integer.toString(youStats.deaths), NK_TEXT_ALIGN_LEFT, white);

        // shots bar
        nk_layout_row_dynamic(ctx, 15, 1);
        nk_label_colored(ctx, "SHOTS", NK_TEXT_ALIGN_CENTERED, white);
        nk_layout_row_dynamic(ctx, 15, 4);
        nk_label_colored(ctx, Integer.toString(youStats.shotsFired), NK_TEXT_ALIGN_RIGHT, white);
        layoutLeftBar(BigInteger.valueOf(scaleTo100(youStats.shotsFired, youStats.shotsFired + AIStats.shotsFired)));
        layoutRightBar(BigInteger.valueOf(scaleTo100(AIStats.shotsFired, youStats.shotsFired + AIStats.shotsFired)));
        nk_label_colored(ctx, Integer.toString(AIStats.shotsFired), NK_TEXT_ALIGN_LEFT, white);

        // on target bar
        nk_layout_row_dynamic(ctx, 15, 1);
        nk_label_colored(ctx, "ACCURACY", NK_TEXT_ALIGN_CENTERED, white);
        nk_layout_row_dynamic(ctx, 15, 4);
        nk_label_colored(ctx, youStats.getAccuracy() + "%", NK_TEXT_ALIGN_RIGHT, white);
        layoutLeftBar(BigInteger.valueOf(youStats.getAccuracy()));
        layoutRightBar(BigInteger.valueOf(AIStats.getAccuracy()));
        nk_label_colored(ctx, AIStats.getAccuracy() + "%", NK_TEXT_ALIGN_LEFT, white);

        // crates bar
        nk_layout_row_dynamic(ctx, 15, 1);
        nk_label_colored(ctx, "CRATES OPENED", NK_TEXT_ALIGN_CENTERED, white);
        nk_layout_row_dynamic(ctx, 15, 4);
        nk_label_colored(ctx, Integer.toString(youStats.crates), NK_TEXT_ALIGN_RIGHT, white);
        layoutLeftBar(BigInteger.valueOf(scaleTo100(youStats.crates, youStats.crates + AIStats.crates)));
        layoutRightBar(BigInteger.valueOf(scaleTo100(AIStats.crates, youStats.crates + AIStats.crates)));
        nk_label_colored(ctx, Integer.toString(AIStats.crates), NK_TEXT_ALIGN_LEFT, white);
    }

    /**
     * draws the bars that are on the left side of the screen.
     *
     * @param value value out of max 100
     */
    private void layoutRightBar(BigInteger value) {
        ctx.style().progress().rounding(3f);
        ctx.style().progress().normal().data().color().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x66);
        ctx.style().progress().cursor_normal().data().color().set((byte) 0x9A, (byte) 0x2F, (byte) 0xEF, (byte) 0xFF);
        ctx.style().progress().border(1f);
        buffer.put(0, value.byteValueExact());
        nk_progress(ctx, cur, 100, false);
    }

    /**
     * draws the bars that are on the right side of the screen.
     *
     * @param value value out of max 100
     */
    private void layoutLeftBar(BigInteger value) {
        ctx.style().progress().rounding(3f);
        ctx.style().progress().normal().data().color().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x66);
        ctx.style().progress().cursor_normal().data().color().set((byte) 0xEF, (byte) 0x9A, (byte) 0x2F, (byte) 0xFF);
        ctx.style().progress().border(1f);
        buffer.put(0, value.byteValueExact());
        nk_progress(ctx, cur, 100, false);
    }

    /**
     * will scale a number to be out of 100 when you pass in a number and what it was originally out of. Used in rendering so we dont get a BigInteger out of byte range error.
     *
     * @param s smaller number
     * @param b bigger number
     * @return
     */
    private int scaleTo100(int s, int b) {
        if (b == 0) {
            return 0;
        }
        return (100 * s / b);
    }
}
