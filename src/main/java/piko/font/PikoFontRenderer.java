package piko.font;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Glyph atlas font renderer.
 *
 * <p>Every glyph of the Latin-1 range is rasterised once into a single OpenGL texture when
 * the font is first used. Drawing a string is then a handful of textured quads submitted in
 * one batch, which is considerably cheaper than the per character texture binds the vanilla
 * renderer performs and keeps HUD text nearly free at high frame rates.</p>
 *
 * <p>Glyphs are rasterised at twice the requested size and drawn at half scale so text stays
 * sharp on high GUI scales.</p>
 */
public final class PikoFontRenderer implements IFont {

    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 255;
    private static final int CHAR_COUNT = LAST_CHAR - FIRST_CHAR + 1;
    private static final int PADDING = 2;
    private static final float SUPERSAMPLE = 2.0F;

    /** Vanilla colour codes so Piko text honours the usual formatting characters. */
    private static final int[] COLOR_CODES = new int[32];

    static {
        for (int i = 0; i < 32; i++) {
            int base = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + base;
            int green = (i >> 1 & 1) * 170 + base;
            int blue = (i & 1) * 170 + base;
            if (i == 6) {
                red += 85;
            }
            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            COLOR_CODES[i] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
        }
    }

    private final Font awtFont;
    private final boolean antiAlias;

    private DynamicTexture texture;
    private int textureSize;
    private final float[] glyphU = new float[CHAR_COUNT];
    private final float[] glyphV = new float[CHAR_COUNT];
    private final float[] glyphWidth = new float[CHAR_COUNT];
    private final float[] glyphHeight = new float[CHAR_COUNT];
    private final float[] advance = new float[CHAR_COUNT];
    private float height;
    private boolean built;
    private boolean failed;

    public PikoFontRenderer(Font font, boolean antiAlias) {
        this.awtFont = font.deriveFont(font.getSize2D() * SUPERSAMPLE);
        this.antiAlias = antiAlias;
    }

    private void build() {
        built = true;
        try {
            BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D probeGraphics = probe.createGraphics();
            probeGraphics.setFont(awtFont);
            FontMetrics metrics = probeGraphics.getFontMetrics();

            int maxWidth = 0;
            for (int i = 0; i < CHAR_COUNT; i++) {
                maxWidth = Math.max(maxWidth, metrics.charWidth((char) (FIRST_CHAR + i)));
            }
            int cellWidth = maxWidth + PADDING * 2;
            int cellHeight = metrics.getHeight() + PADDING * 2;
            probeGraphics.dispose();

            int columns = 16;
            int rows = (CHAR_COUNT + columns - 1) / columns;
            int required = Math.max(cellWidth * columns, cellHeight * rows);
            textureSize = nextPowerOfTwo(required);

            BufferedImage atlas = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = atlas.createGraphics();
            graphics.setFont(awtFont);
            graphics.setColor(new Color(255, 255, 255, 0));
            graphics.fillRect(0, 0, textureSize, textureSize);
            graphics.setColor(Color.WHITE);
            if (antiAlias) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            FontMetrics atlasMetrics = graphics.getFontMetrics();
            height = (atlasMetrics.getAscent() + atlasMetrics.getDescent()) / SUPERSAMPLE;

            for (int i = 0; i < CHAR_COUNT; i++) {
                char character = (char) (FIRST_CHAR + i);
                int column = i % columns;
                int row = i / columns;
                int x = column * cellWidth;
                int y = row * cellHeight;
                graphics.drawString(String.valueOf(character), x + PADDING, y + PADDING + atlasMetrics.getAscent());
                glyphU[i] = (float) x / textureSize;
                glyphV[i] = (float) y / textureSize;
                glyphWidth[i] = cellWidth;
                glyphHeight[i] = cellHeight;
                advance[i] = atlasMetrics.charWidth(character) / SUPERSAMPLE;
            }
            graphics.dispose();

            texture = new DynamicTexture(atlas);
        } catch (Throwable throwable) {
            // A missing or broken AWT environment must never take the client down; the
            // affected HUD simply falls back to the vanilla font.
            failed = true;
            System.err.println("[Piko] Font atlas creation failed, falling back to the Minecraft font: " + throwable);
        }
    }

    private static int nextPowerOfTwo(int value) {
        int result = 64;
        while (result < value) {
            result <<= 1;
        }
        return result;
    }

    private boolean ready() {
        if (!built) {
            build();
        }
        return !failed && texture != null;
    }

    @Override
    public float drawString(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) {
            return x;
        }
        if (!ready()) {
            return MinecraftFont.INSTANCE.drawString(text, x, y, color);
        }

        float alpha = (color >>> 24) / 255.0F;
        if (alpha <= 0.001F) {
            alpha = 1.0F;
        }
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.bindTexture(texture.getGlTextureId());
        GlStateManager.color(red, green, blue, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float cursor = x;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '\u00A7' && i + 1 < text.length()) {
                int code = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(text.charAt(i + 1)));
                if (code >= 0 && code < 16) {
                    int rgb = COLOR_CODES[code];
                    tessellator.draw();
                    GlStateManager.color((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, alpha);
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                } else if (code == 21) {
                    tessellator.draw();
                    GlStateManager.color(red, green, blue, alpha);
                    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                }
                i++;
                continue;
            }
            int index = character - FIRST_CHAR;
            if (index < 0 || index >= CHAR_COUNT) {
                continue;
            }
            drawGlyph(worldRenderer, index, cursor, y);
            cursor += advance[index];
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        return cursor;
    }

    private void drawGlyph(WorldRenderer worldRenderer, int index, float x, float y) {
        float width = glyphWidth[index] / SUPERSAMPLE;
        float glyphTall = glyphHeight[index] / SUPERSAMPLE;
        float u = glyphU[index];
        float v = glyphV[index];
        float u2 = u + glyphWidth[index] / textureSize;
        float v2 = v + glyphHeight[index] / textureSize;
        float top = y - PADDING / SUPERSAMPLE;

        worldRenderer.pos(x, top + glyphTall, 0).tex(u, v2).endVertex();
        worldRenderer.pos(x + width, top + glyphTall, 0).tex(u2, v2).endVertex();
        worldRenderer.pos(x + width, top, 0).tex(u2, v).endVertex();
        worldRenderer.pos(x, top, 0).tex(u, v).endVertex();
    }

    @Override
    public float drawStringWithShadow(String text, float x, float y, int color) {
        int shadow = (color & 0xFCFCFC) >> 2 | (color & 0xFF000000);
        drawString(text, x + 0.6F, y + 0.6F, shadow);
        return drawString(text, x, y, color);
    }

    @Override
    public float drawCenteredString(String text, float centerX, float y, int color) {
        return drawStringWithShadow(text, centerX - getStringWidth(text) / 2.0F, y, color);
    }

    @Override
    public float getStringWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        if (!ready()) {
            return MinecraftFont.INSTANCE.getStringWidth(text);
        }
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '\u00A7' && i + 1 < text.length()) {
                i++;
                continue;
            }
            int index = character - FIRST_CHAR;
            if (index >= 0 && index < CHAR_COUNT) {
                width += advance[index];
            }
        }
        return width;
    }

    @Override
    public float getHeight() {
        if (!ready()) {
            return MinecraftFont.INSTANCE.getHeight();
        }
        return height;
    }
}
