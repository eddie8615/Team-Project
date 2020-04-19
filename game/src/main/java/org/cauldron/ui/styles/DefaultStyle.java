package org.cauldron.ui.styles;

import org.cauldron.renderer.TextureHandler;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public class DefaultStyle {
    static int skin;
    public NkImage check;
    public NkImage checkCursor;
    public NkImage option;
    public NkImage optionCursor;
    public NkImage header;
    public NkImage window;
    public NkImage scrollbarIncButton;
    public NkImage scrollbarIncButtonHover;
    public NkImage scrollbarDecButton;
    public NkImage scrollbarDecButtonHover;
    public NkImage button;
    public NkImage buttonHover;
    public NkImage buttonActive;
    public NkImage tabMini;
    public NkImage tabMax;
    public NkImage sliderBar;
    public NkImage sliderKnob;
    public NkImage edit;

    public DefaultStyle() {
        short width = 512;
        short height = 512;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);
            skin = TextureHandler.getTextureID("gwen");
            check = nk_subimage_id(skin, width, height, nk_rect(464, 32, 15, 15, rect), NkImage.create());
            checkCursor = nk_subimage_id(skin, width, height, nk_rect(464, 34, 11, 11, rect), NkImage.create());
            option = nk_subimage_id(skin, width, height, nk_rect(464, 64, 15, 15, rect), NkImage.create());
            optionCursor = nk_subimage_id(skin, width, height, nk_rect(451, 67, 9, 9, rect), NkImage.create());
            header = nk_subimage_id(skin, width, height, nk_rect(128, 0, 127, 24, rect), NkImage.create());
            window = nk_subimage_id(skin, width, height, nk_rect(128, 23, 127, 104, rect), NkImage.create());
            scrollbarIncButton = nk_subimage_id(skin, width, height, nk_rect(464, 256, 15, 15, rect), NkImage.create());
            scrollbarIncButtonHover = nk_subimage_id(skin, width, height, nk_rect(464, 320, 15, 15, rect), NkImage.create());
            scrollbarDecButton = nk_subimage_id(skin, width, height, nk_rect(464, 224, 15, 15, rect), NkImage.create());
            scrollbarDecButtonHover = nk_subimage_id(skin, width, height, nk_rect(464, 288, 15, 15, rect), NkImage.create());
            button = TextureHandler.getTextureNk("button_long");
            buttonHover = TextureHandler.getTextureNk("button_long_hover");
            buttonActive = TextureHandler.getTextureNk("button_long_disabled");
            tabMini = nk_subimage_id(skin, width, height, nk_rect(451, 99, 9, 9, rect), NkImage.create());
            tabMax = nk_subimage_id(skin, width, height, nk_rect(467, 99, 9, 9, rect), NkImage.create());
            sliderBar = TextureHandler.getTextureNk("slider.9");
            sliderKnob = TextureHandler.getTextureNk("slider-knob.9");
            edit = TextureHandler.getTextureNk("text-field.9");
        }
    }

    public void setStyle(NkContext ctx, boolean transparent) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //window
            ctx.style().window().background().set((byte) 0xCC, (byte) 0xCC, (byte) 0xCC, (byte) 0x00);
            if (transparent)
                ctx.style().window().fixed_background().data().color().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00);
            else
                ctx.style().window().fixed_background().data().color().set((byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xFF);
            //ctx.style().window().fixed_background().data().image(window);
            ctx.style().window().border_color().set((byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0xFF);
            ctx.style().window().combo_border_color().set((byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0xFF);
            ctx.style().window().contextual_border_color().set((byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0xFF);
            ctx.style().window().menu_border_color().set((byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0xFF);
            ctx.style().window().group_border_color().set((byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0xFF);
            ctx.style().window().tooltip_border_color().set((byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0xFF);
            ctx.style().window().scrollbar_size().set(16, 16);
            ctx.style().window().border_color().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
            ctx.style().window().padding().set(8, 4);
            ctx.style().window().border(3);

            //window header
            ctx.style().window().header().normal().data().image(header);
            ctx.style().window().header().hover().data().image(header);
            ctx.style().window().header().active().data().image(header);
            ctx.style().window().header().label_normal().set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0x5F);
            ctx.style().window().header().label_hover().set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0x5F);
            ctx.style().window().header().label_active().set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0x5F);

            //scrollbar
            NkColor color = NkColor.create()
                    .r((byte) 0xB8)
                    .g((byte) 0xB8)
                    .b((byte) 0xB8)
                    .a((byte) 0xFF);

            ctx.style().scrollv().normal().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().scrollv().hover().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().scrollv().active().set(nk_style_item_color(color, NkStyleItem.create()));

            color = color.set((byte) 0xDC, (byte) 0xDC, (byte) 0xDC, (byte) 0xFF);
            ctx.style().scrollv().cursor_normal().set(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0xEB, (byte) 0xEB, (byte) 0xEB, (byte) 0xFF);
            ctx.style().scrollv().cursor_hover().set(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x63, (byte) 0xC5, (byte) 0xFF, (byte) 0xFF);
            ctx.style().scrollv().cursor_active().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().scrollv().dec_symbol(NK_SYMBOL_NONE);
            ctx.style().scrollv().inc_symbol(NK_SYMBOL_NONE);
            ctx.style().scrollv().show_buttons(nk_true);
            color = color.set((byte) 0x51, (byte) 0x51, (byte) 0x51, (byte) 0xFF);
            ctx.style().scrollv().border_color().set(color);
            ctx.style().scrollv().cursor_border_color().set(color);
            ctx.style().scrollv().border(1);
            ctx.style().scrollv().rounding(0);
            ctx.style().scrollv().border_cursor(1);
            ctx.style().scrollv().rounding_cursor(2);

            //scrollbar buttons
            ctx.style().scrollv().inc_button().normal().set(nk_style_item_image(scrollbarIncButton, NkStyleItem.create()));
            ctx.style().scrollv().inc_button().hover().set(nk_style_item_image(scrollbarIncButtonHover, NkStyleItem.create()));
            ctx.style().scrollv().inc_button().active().set(nk_style_item_image(scrollbarIncButtonHover, NkStyleItem.create()));
            color = color.set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF);
            ctx.style().scrollv().inc_button().border_color(color);
            ctx.style().scrollv().inc_button().text_background(color);
            ctx.style().scrollv().inc_button().text_normal(color);
            ctx.style().scrollv().inc_button().text_hover(color);
            ctx.style().scrollv().inc_button().text_active(color);
            ctx.style().scrollv().inc_button().border(0.0f);


            ctx.style().scrollv().dec_button().normal().set(nk_style_item_image(scrollbarDecButton, NkStyleItem.create()));
            ctx.style().scrollv().dec_button().hover().set(nk_style_item_image(scrollbarDecButtonHover, NkStyleItem.create()));
            ctx.style().scrollv().dec_button().active().set(nk_style_item_image(scrollbarDecButtonHover, NkStyleItem.create()));
            ctx.style().scrollv().dec_button().border_color(color);
            ctx.style().scrollv().dec_button().text_background(color);
            ctx.style().scrollv().dec_button().text_normal(color);
            ctx.style().scrollv().dec_button().text_hover(color);
            ctx.style().scrollv().dec_button().text_active(color);
            ctx.style().scrollv().dec_button().border(0.0f);

            //default button
            ctx.style().button().normal().set(nk_style_item_image(button, NkStyleItem.create()));
            ctx.style().button().hover().set(nk_style_item_image(buttonHover, NkStyleItem.create()));
            ctx.style().button().active().set(nk_style_item_image(buttonActive, NkStyleItem.create()));
            ctx.style().button().border_color(color);
            ctx.style().button().text_background(color);
            color = color.set((byte) 0x1F, (byte) 0x1F, (byte) 0x1F, (byte) 0xFF);
            ctx.style().button().text_normal(color);
            ctx.style().button().text_hover(color);
            ctx.style().button().text_active(color);

            ctx.style().text().color(color);

            //contextual button
            color = color.set((byte) 0xC7, (byte) 0xC7, (byte) 0xC7, (byte) 0xFF);
            ctx.style().contextual_button().normal().set(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0xE5, (byte) 0xE5, (byte) 0xE5, (byte) 0xFF);
            ctx.style().contextual_button().hover().set(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x63, (byte) 0xC5, (byte) 0xFF, (byte) 0xFF);
            ctx.style().contextual_button().active().set(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
            ctx.style().contextual_button().border_color(color);
            ctx.style().contextual_button().text_background(color);
            color = color.set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0xFF);
            ctx.style().contextual_button().text_normal(color);
            ctx.style().contextual_button().text_hover(color);
            ctx.style().contextual_button().text_active(color);

            //menu button
            color = color.set((byte) 0xC7, (byte) 0xC7, (byte) 0xC7, (byte) 0xFF);
            ctx.style().menu_button().normal(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0xE5, (byte) 0xE5, (byte) 0xE5, (byte) 0xFF);
            ctx.style().menu_button().hover(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x63, (byte) 0xC5, (byte) 0xFF, (byte) 0xFF);
            ctx.style().menu_button().active(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
            ctx.style().menu_button().border_color(color);
            ctx.style().menu_button().text_background(color);
            color = color.set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0xFF);
            ctx.style().menu_button().text_normal(color);
            ctx.style().menu_button().text_hover(color);
            ctx.style().menu_button().text_active(color);

            //tree
            ctx.style().tab().text(color);
            ctx.style().tab().tab_minimize_button().normal(nk_style_item_image(tabMini, NkStyleItem.create()));
            ctx.style().tab().tab_minimize_button().hover(nk_style_item_image(tabMini, NkStyleItem.create()));
            ctx.style().tab().tab_minimize_button().active(nk_style_item_image(tabMini, NkStyleItem.create()));

            color = color.set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
            ctx.style().tab().tab_minimize_button().text_background(color);
            ctx.style().tab().tab_minimize_button().text_normal(color);
            ctx.style().tab().tab_minimize_button().text_hover(color);
            ctx.style().tab().tab_minimize_button().text_active(color);

            ctx.style().tab().tab_maximize_button().normal(nk_style_item_image(tabMax, NkStyleItem.create()));
            ctx.style().tab().tab_maximize_button().hover(nk_style_item_image(tabMax, NkStyleItem.create()));
            ctx.style().tab().tab_maximize_button().active(nk_style_item_image(tabMax, NkStyleItem.create()));
            ctx.style().tab().tab_maximize_button().text_background(color);
            ctx.style().tab().tab_maximize_button().text_normal(color);
            ctx.style().tab().tab_maximize_button().text_hover(color);
            ctx.style().tab().tab_maximize_button().text_active(color);

            ctx.style().tab().node_minimize_button().normal(nk_style_item_image(tabMini, NkStyleItem.create()));
            ctx.style().tab().node_minimize_button().hover(nk_style_item_image(tabMini, NkStyleItem.create()));
            ctx.style().tab().node_minimize_button().active(nk_style_item_image(tabMini, NkStyleItem.create()));
            ctx.style().tab().node_minimize_button().text_background(color);
            ctx.style().tab().node_minimize_button().text_normal(color);
            ctx.style().tab().node_minimize_button().text_hover(color);
            ctx.style().tab().node_minimize_button().text_active(color);

            ctx.style().tab().node_maximize_button().normal(nk_style_item_image(tabMax, NkStyleItem.create()));
            ctx.style().tab().node_maximize_button().hover(nk_style_item_image(tabMax, NkStyleItem.create()));
            ctx.style().tab().node_maximize_button().active(nk_style_item_image(tabMax, NkStyleItem.create()));
            ctx.style().tab().node_maximize_button().text_background(color);
            ctx.style().tab().node_maximize_button().text_normal(color);
            ctx.style().tab().node_maximize_button().text_hover(color);
            ctx.style().tab().node_maximize_button().text_active(color);

            //selectable
            color = color.set((byte) 0xC7, (byte) 0xC7, (byte) 0xC7, (byte) 0xFF);
            ctx.style().selectable().normal(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().selectable().hover(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().selectable().pressed(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0xB9, (byte) 0xCD, (byte) 0xF1, (byte) 0xFF);
            ctx.style().selectable().normal_active(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().selectable().hover_active(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().selectable().pressed_active(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0xFF);
            ctx.style().selectable().text_normal(color);
            ctx.style().selectable().text_hover(color);
            ctx.style().selectable().text_pressed(color);
            ctx.style().selectable().text_normal_active(color);
            ctx.style().selectable().text_hover_active(color);
            ctx.style().selectable().text_pressed_active(color);

            //slider
            ctx.style().slider().normal(nk_style_item_image(sliderBar, NkStyleItem.create()));
            ctx.style().slider().hover(nk_style_item_image(sliderBar, NkStyleItem.create()));
            ctx.style().slider().active(nk_style_item_image(sliderBar, NkStyleItem.create()));
            color = color.set((byte) 0x93, (byte) 0x93, (byte) 0x93, (byte) 0xFF);
            //ctx.style().slider().bar_normal(color);
            //ctx.style().slider().bar_hover(color);
            //ctx.style().slider().bar_active(color);
            //ctx.style().slider().bar_filled(color);
            ctx.style().slider().cursor_normal(nk_style_item_image(sliderKnob, NkStyleItem.create()));
            ctx.style().slider().cursor_hover(nk_style_item_image(sliderKnob, NkStyleItem.create()));
            ctx.style().slider().cursor_active(nk_style_item_image(sliderKnob, NkStyleItem.create()));
            ctx.style().slider().cursor_size(NkVec2.create().set(16.5f, 25));
            ctx.style().slider().bar_height(1);

            //progressbar
            color = color.set((byte) 0xD7, (byte) 0xD7, (byte) 0xD7, (byte) 0xFF);
            ctx.style().progress().normal().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().progress().hover().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().progress().active().set(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x3F, (byte) 0xF1, (byte) 0x5D, (byte) 0xFF);
            ctx.style().progress().cursor_normal().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().progress().cursor_hover().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().progress().cursor_active().set(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x71, (byte) 0x73, (byte) 0x72, (byte) 0xFF);
            ctx.style().progress().border_color(color);
            ctx.style().progress().padding().set(0, 0);
            ctx.style().progress().border(2);
            ctx.style().progress().rounding(1);

            //combo
            color = color.set((byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xFF);
            ctx.style().combo().normal().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().combo().hover().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().combo().active().set(nk_style_item_color(color, NkStyleItem.create()));
            color = color.set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0xFF);
            ctx.style().combo().border_color(color);
            ctx.style().combo().label_normal(color);
            ctx.style().combo().label_hover(color);
            ctx.style().combo().label_active(color);
            ctx.style().combo().border(1);
            ctx.style().combo().rounding(1);

            //combo button
            color = color.set((byte) 0xD1, (byte) 0xD1, (byte) 0xD1, (byte) 0xFF);
            ctx.style().combo().button().normal().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().combo().button().hover().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().combo().button().active().set(nk_style_item_color(color, NkStyleItem.create()));
            ctx.style().combo().button().text_background(color);
            color = color.set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0xFF);
            ctx.style().combo().button().text_normal(color);
            ctx.style().combo().button().text_hover(color);
            ctx.style().combo().button().text_active(color);

            //edit
            ctx.style().edit().normal().set(nk_style_item_image(edit, NkStyleItem.create()));
            ctx.style().edit().hover().set(nk_style_item_image(edit, NkStyleItem.create()));
            ctx.style().edit().active().set(nk_style_item_image(edit, NkStyleItem.create()));
        }
    }

    public void unsetStyle(NkContext ctx) {
        NkContext freshCtx = NkContext.create();
        ctx.style().set(freshCtx.style());
    }
}
