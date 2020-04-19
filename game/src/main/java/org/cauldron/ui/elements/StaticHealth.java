package org.cauldron.ui.elements;

import org.cauldron.game.Settings;
import org.cauldron.ui.UIElement;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.nuklear.Nuklear.*;

public class StaticHealth extends UIElement {
    public BigInteger power;
    public String clientName = "";
    public Map<String, BigInteger> nameToHealth = new HashMap<>();

    private ByteBuffer buffer = BufferUtils.createByteBuffer(120);
    private PointerBuffer cur = PointerBuffer.create(buffer);

    public StaticHealth(NkContext ctx, String name, int x, int y) {
        super(ctx, name, x, y);
    }

    public void layout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);
            ctx.style().window().fixed_background().data().color().set((byte) 0xDD, (byte) 0xDD, (byte) 0xDD, (byte) 0xAA);
            ctx.style().window().rounding(5f);
            if (nk_begin(
                    ctx,
                    name,
                    nk_rect(x, y, 280, 75, rect),
                    NK_WINDOW_NO_INPUT
            )) {
                List<String> names = new ArrayList<>();
                names.addAll(nameToHealth.keySet());
                Collections.sort(names);

                for (int i = 0; i + 1 < names.size(); i += 2) {
                    nk_layout_row_dynamic(ctx, 18, 2);
                    nk_label(ctx, names.get(i), NK_TEXT_ALIGN_CENTERED);
                    nk_label(ctx, names.get(i + 1), NK_TEXT_ALIGN_CENTERED);
                    nk_layout_row_dynamic(ctx, 8, 2);
                    layoutHealth(names.get(i));
                    layoutHealth(names.get(i + 1));
                    if (names.get(i).equals(clientName) && !Settings.powerIndicator.equals("arrow"))
                        layoutPower();
                    if (names.get(i + 1).equals(clientName) && !Settings.powerIndicator.equals("arrow")) {
                        nk_label(ctx, "", NK_TEXT_ALIGN_CENTERED);
                        layoutPower();
                    }
                }
            }
            nk_end(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void layoutPower() {
        ctx.style().progress().rounding(3f);
        ctx.style().progress().normal().data().color().set((byte) 0x66, (byte) 0xCC, (byte) 0xFF, (byte) 0x66);
        ctx.style().progress().cursor_normal().data().color().set((byte) 0x66, (byte) 0xCC, (byte) 0xFF, (byte) 0xFF);
        ctx.style().progress().border(1f);
        //nk_layout_row_static(ctx, 8, 100, 1);
        buffer.put(0, power.byteValueExact());
        nk_progress(ctx, cur, 100, false);
    }

    private void layoutHealth(String name) {
        ctx.style().progress().rounding(3f);
        ctx.style().progress().normal().data().color().set((byte) 0x33, (byte) 0xCC, (byte) 0x33, (byte) 0x66);
        ctx.style().progress().cursor_normal().data().color().set((byte) 0x33, (byte) 0xCC, (byte) 0x33, (byte) 0xFF);
        ctx.style().progress().border(1f);
        //nk_layout_row_static(ctx, 8, 100, 1);
        buffer.put(0, nameToHealth.get(name).byteValueExact());
        nk_progress(ctx, cur, 100, false);
    }

    public void resetBars() {
        nameToHealth.clear();
    }

}
