package piko.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * All 2D drawing used by Piko.
 *
 * <p>Everything here is written for throughput: the corner geometry of rounded rectangles
 * comes from a sine table built once at class load, quads are pushed straight through the
 * shared {@link Tessellator} and no helper allocates objects per frame.</p>
 */
public final class RenderUtil {

    /** Number of segments used for a 90 degree corner. Eight is smooth at HUD sizes and cheap. */
    private static final int CORNER_SEGMENTS = 8;
    private static final float[] CORNER_SIN = new float[CORNER_SEGMENTS + 1];
    private static final float[] CORNER_COS = new float[CORNER_SEGMENTS + 1];

    static {
        for (int i = 0; i <= CORNER_SEGMENTS; i++) {
            double angle = (Math.PI / 2.0D) * i / CORNER_SEGMENTS;
            CORNER_SIN[i] = (float) Math.sin(angle);
            CORNER_COS[i] = (float) Math.cos(angle);
        }
    }

    private RenderUtil() {
    }

    private static void beginColored() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
    }

    private static void endColored() {
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void applyColor(int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void drawRect(float x, float y, float width, float height, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
            return;
        }
        beginColored();
        applyColor(color);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(x, y + height, 0).endVertex();
        worldRenderer.pos(x + width, y + height, 0).endVertex();
        worldRenderer.pos(x + width, y, 0).endVertex();
        worldRenderer.pos(x, y, 0).endVertex();
        tessellator.draw();
        endColored();
    }

    /** Vertical gradient, used for the Piko panels and the menu background. */
    public static void drawGradientRect(float x, float y, float width, float height, int topColor, int bottomColor) {
        if (width <= 0 || height <= 0) {
            return;
        }
        beginColored();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertexColored(worldRenderer, x, y, topColor);
        vertexColored(worldRenderer, x + width, y, topColor);
        vertexColored(worldRenderer, x + width, y + height, bottomColor);
        vertexColored(worldRenderer, x, y + height, bottomColor);
        tessellator.draw();
        endColored();
    }

    private static void vertexColored(WorldRenderer worldRenderer, float x, float y, int color) {
        worldRenderer.pos(x, y, 0)
                .color((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >>> 24) / 255.0F)
                .endVertex();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
            return;
        }
        float limit = Math.min(width, height) / 2.0F;
        float r = Math.max(0.0F, Math.min(radius, limit));
        if (r <= 0.05F) {
            drawRect(x, y, width, height, color);
            return;
        }

        beginColored();
        applyColor(color);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);

        float centerX = x + width / 2.0F;
        float centerY = y + height / 2.0F;
        worldRenderer.pos(centerX, centerY, 0).endVertex();

        // Top right, bottom right, bottom left, top left, then close the fan.
        arc(worldRenderer, x + width - r, y + r, r, 0);
        arc(worldRenderer, x + width - r, y + height - r, r, 1);
        arc(worldRenderer, x + r, y + height - r, r, 2);
        arc(worldRenderer, x + r, y + r, r, 3);
        worldRenderer.pos(x + width - r, y, 0).endVertex();

        tessellator.draw();
        endColored();
    }

    private static void arc(WorldRenderer worldRenderer, float centerX, float centerY, float radius, int quadrant) {
        for (int i = 0; i <= CORNER_SEGMENTS; i++) {
            float sin = CORNER_SIN[i];
            float cos = CORNER_COS[i];
            float offsetX;
            float offsetY;
            switch (quadrant) {
                case 0:
                    offsetX = sin;
                    offsetY = -cos;
                    break;
                case 1:
                    offsetX = cos;
                    offsetY = sin;
                    break;
                case 2:
                    offsetX = -sin;
                    offsetY = cos;
                    break;
                default:
                    offsetX = -cos;
                    offsetY = -sin;
                    break;
            }
            worldRenderer.pos(centerX + offsetX * radius, centerY + offsetY * radius, 0).endVertex();
        }
    }

    /**
     * Rounded panel with the Piko look: dark fill plus a light blue border.
     */
    public static void drawPanel(float x, float y, float width, float height, float radius, int fill, int outline) {
        drawRoundedRect(x, y, width, height, radius, fill);
        drawRoundedBorder(x, y, width, height, radius, 1.0F, outline);
    }

    /**
     * Draws a border by stacking four thin rounded rectangles, which avoids the depth or
     * stencil tricks that break when the HUD is scaled.
     */
    public static void drawRoundedBorder(float x, float y, float width, float height, float radius, float thickness, int color) {
        if (thickness <= 0 || (color >>> 24) == 0) {
            return;
        }
        float limit = Math.min(width, height) / 2.0F;
        float r = Math.max(0.0F, Math.min(radius, limit));

        beginColored();
        applyColor(color);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION);

        borderArc(worldRenderer, x + width - r, y + r, r, thickness, 0);
        borderArc(worldRenderer, x + width - r, y + height - r, r, thickness, 1);
        borderArc(worldRenderer, x + r, y + height - r, r, thickness, 2);
        borderArc(worldRenderer, x + r, y + r, r, thickness, 3);
        // Close the strip back onto the first corner.
        worldRenderer.pos(x + width - r, y, 0).endVertex();
        worldRenderer.pos(x + width - r, y + thickness, 0).endVertex();

        tessellator.draw();
        endColored();
    }

    private static void borderArc(WorldRenderer worldRenderer, float centerX, float centerY, float radius, float thickness, int quadrant) {
        for (int i = 0; i <= CORNER_SEGMENTS; i++) {
            float sin = CORNER_SIN[i];
            float cos = CORNER_COS[i];
            float offsetX;
            float offsetY;
            switch (quadrant) {
                case 0:
                    offsetX = sin;
                    offsetY = -cos;
                    break;
                case 1:
                    offsetX = cos;
                    offsetY = sin;
                    break;
                case 2:
                    offsetX = -sin;
                    offsetY = cos;
                    break;
                default:
                    offsetX = -cos;
                    offsetY = -sin;
                    break;
            }
            worldRenderer.pos(centerX + offsetX * radius, centerY + offsetY * radius, 0).endVertex();
            float inner = Math.max(0.0F, radius - thickness);
            worldRenderer.pos(centerX + offsetX * inner, centerY + offsetY * inner, 0).endVertex();
        }
    }

    public static void drawCircle(float centerX, float centerY, float radius, int color) {
        beginColored();
        applyColor(color);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldRenderer.pos(centerX, centerY, 0).endVertex();
        for (int i = 0; i <= 32; i++) {
            double angle = Math.PI * 2 * i / 32.0D;
            worldRenderer.pos(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, 0).endVertex();
        }
        tessellator.draw();
        endColored();
    }

    public static void drawLine(float startX, float startY, float endX, float endY, float thickness, int color) {
        beginColored();
        applyColor(color);
        GL11.glLineWidth(thickness);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldRenderer.pos(startX, startY, 0).endVertex();
        worldRenderer.pos(endX, endY, 0).endVertex();
        tessellator.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.0F);
        endColored();
    }

    /** Dashed helper line, used by the HUD editor alignment guides. */
    public static void drawDashedLine(float startX, float startY, float endX, float endY, float dash, int color) {
        float deltaX = endX - startX;
        float deltaY = endY - startY;
        float length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (length <= 0) {
            return;
        }
        float stepX = deltaX / length;
        float stepY = deltaY / length;
        for (float travelled = 0; travelled < length; travelled += dash * 2) {
            float segment = Math.min(dash, length - travelled);
            drawLine(startX + stepX * travelled, startY + stepY * travelled,
                    startX + stepX * (travelled + segment), startY + stepY * (travelled + segment), 1.0F, color);
        }
    }

    /**
     * Draws a region of the currently bound texture.
     *
     * @param u,v            top left corner inside the texture, in texture pixels
     * @param textureWidth   full size of the bound texture, used to normalise the coordinates
     */
    public static void drawTexturedRect(float x, float y, float width, float height,
                                        float u, float v, float regionWidth, float regionHeight,
                                        float textureWidth, float textureHeight) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + height, 0).tex(u / textureWidth, (v + regionHeight) / textureHeight).endVertex();
        worldRenderer.pos(x + width, y + height, 0)
                .tex((u + regionWidth) / textureWidth, (v + regionHeight) / textureHeight).endVertex();
        worldRenderer.pos(x + width, y, 0).tex((u + regionWidth) / textureWidth, v / textureHeight).endVertex();
        worldRenderer.pos(x, y, 0).tex(u / textureWidth, v / textureHeight).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
    }

    /** Draws the whole bound texture into the given rectangle. */
    public static void drawTexture(float x, float y, float width, float height) {
        drawTexturedRect(x, y, width, height, 0, 0, 1, 1, 1, 1);
    }

    /** Restricts drawing to a rectangle given in GUI coordinates. */
    public static void enableScissor(float x, float y, float width, float height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        int factor = resolution.getScaleFactor();
        int scissorY = (int) ((resolution.getScaledHeight() - (y + height)) * factor);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (x * factor), scissorY, (int) (width * factor), (int) (height * factor));
    }

    public static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /** Resets the pieces of GL state that HUD drawing is allowed to touch. */
    public static void resetState() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
