package piko.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/** Adapter around the vanilla 1.8.9 font renderer. */
public final class MinecraftFont implements IFont {

    public static final MinecraftFont INSTANCE = new MinecraftFont();

    private MinecraftFont() {
    }

    private FontRenderer font() {
        return Minecraft.getMinecraft().fontRendererObj;
    }

    @Override
    public float drawString(String text, float x, float y, int color) {
        return font().drawString(text, x, y, color, false);
    }

    @Override
    public float drawStringWithShadow(String text, float x, float y, int color) {
        return font().drawString(text, x, y, color, true);
    }

    @Override
    public float drawCenteredString(String text, float centerX, float y, int color) {
        return font().drawString(text, centerX - font().getStringWidth(text) / 2.0F, y, color, true);
    }

    @Override
    public float getStringWidth(String text) {
        return font().getStringWidth(text);
    }

    @Override
    public float getHeight() {
        return font().FONT_HEIGHT;
    }
}
