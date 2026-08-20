package piko.gui.components;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import piko.font.FontManager;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.ColorSetting;
import piko.util.MathUtil;

/**
 * Colour picker with a saturation/brightness field, a hue strip and an alpha strip.
 *
 * <p>Collapsed it is a single swatch row; expanded it edits the bound {@link ColorSetting}
 * live, so the change is visible on the HUD while the picker is still open.</p>
 */
public class ColorPickerComponent extends Component {

    private static final float FIELD_SIZE = 46.0F;
    private static final float STRIP_WIDTH = 8.0F;

    private final ColorSetting setting;
    private boolean expanded;
    private int dragTarget = -1;

    private float hue;
    private float saturation;
    private float brightness;
    private float alpha;

    public ColorPickerComponent(ColorSetting setting) {
        this.setting = setting;
        this.height = 20.0F;
        readFromSetting();
    }

    private void readFromSetting() {
        float[] hsb = ColorUtil.toHsb(setting.get());
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = setting.getAlpha() / 255.0F;
    }

    private void writeToSetting() {
        setting.set(ColorUtil.fromHsb(hue, saturation, brightness, (int) (alpha * 255)));
    }

    @Override
    public float getHeight() {
        return expanded ? 20.0F + FIELD_SIZE + 8.0F : 20.0F;
    }

    @Override
    public void draw(float mouseX, float mouseY) {
        if (!visible) {
            return;
        }
        FontManager.regular().drawString(setting.getName(), x, y + 3.0F, Theme.TEXT);

        float swatchWidth = 34.0F;
        float swatchX = x + width - swatchWidth;
        RenderUtil.drawRoundedRect(swatchX, y, swatchWidth, 13.0F, 3.0F, 0xFF000000);
        RenderUtil.drawRoundedRect(swatchX + 1, y + 1, swatchWidth - 2, 11.0F, 2.5F, setting.get());
        RenderUtil.drawRoundedBorder(swatchX, y, swatchWidth, 13.0F, 3.0F, 1.0F, ColorUtil.alpha(Theme.accent(), 0.4F));

        if (!expanded) {
            return;
        }

        float fieldY = y + 18.0F;
        drawSaturationField(x, fieldY);

        float hueX = x + FIELD_SIZE + 6.0F;
        drawHueStrip(hueX, fieldY);

        float alphaX = hueX + STRIP_WIDTH + 5.0F;
        drawAlphaStrip(alphaX, fieldY);

        String hex = setting.displayValue();
        FontManager.small().drawString(hex, alphaX + STRIP_WIDTH + 8.0F, fieldY + 2.0F, Theme.TEXT_SECONDARY);
        FontManager.small().drawString("A " + (int) (alpha * 100) + "%", alphaX + STRIP_WIDTH + 8.0F,
                fieldY + 12.0F, Theme.TEXT_SECONDARY);
    }

    private void drawSaturationField(float fieldX, float fieldY) {
        int pure = ColorUtil.fromHsb(hue, 1.0F, 1.0F, 255);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertex(worldRenderer, fieldX, fieldY, 0xFFFFFFFF);
        vertex(worldRenderer, fieldX + FIELD_SIZE, fieldY, pure);
        vertex(worldRenderer, fieldX + FIELD_SIZE, fieldY + FIELD_SIZE, pure);
        vertex(worldRenderer, fieldX, fieldY + FIELD_SIZE, 0xFFFFFFFF);
        tessellator.draw();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertex(worldRenderer, fieldX, fieldY, 0x00000000);
        vertex(worldRenderer, fieldX + FIELD_SIZE, fieldY, 0x00000000);
        vertex(worldRenderer, fieldX + FIELD_SIZE, fieldY + FIELD_SIZE, 0xFF000000);
        vertex(worldRenderer, fieldX, fieldY + FIELD_SIZE, 0xFF000000);
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        float cursorX = fieldX + saturation * FIELD_SIZE;
        float cursorY = fieldY + (1.0F - brightness) * FIELD_SIZE;
        RenderUtil.drawCircle(cursorX, cursorY, 2.5F, 0xFFFFFFFF);
        RenderUtil.drawCircle(cursorX, cursorY, 1.5F, setting.get() | 0xFF000000);
    }

    private void drawHueStrip(float stripX, float stripY) {
        int steps = 12;
        float stepHeight = FIELD_SIZE / steps;
        for (int i = 0; i < steps; i++) {
            int top = ColorUtil.fromHsb(i / (float) steps, 1.0F, 1.0F, 255);
            int bottom = ColorUtil.fromHsb((i + 1) / (float) steps, 1.0F, 1.0F, 255);
            RenderUtil.drawGradientRect(stripX, stripY + i * stepHeight, STRIP_WIDTH, stepHeight + 0.5F, top, bottom);
        }
        float markerY = stripY + hue * FIELD_SIZE;
        RenderUtil.drawRect(stripX - 1.0F, markerY - 1.0F, STRIP_WIDTH + 2.0F, 2.0F, 0xFFFFFFFF);
    }

    private void drawAlphaStrip(float stripX, float stripY) {
        int solid = setting.get() | 0xFF000000;
        RenderUtil.drawGradientRect(stripX, stripY, STRIP_WIDTH, FIELD_SIZE, solid, solid & 0x00FFFFFF);
        RenderUtil.drawRoundedBorder(stripX, stripY, STRIP_WIDTH, FIELD_SIZE, 1.0F, 1.0F,
                ColorUtil.alpha(Theme.accent(), 0.35F));
        float markerY = stripY + (1.0F - alpha) * FIELD_SIZE;
        RenderUtil.drawRect(stripX - 1.0F, markerY - 1.0F, STRIP_WIDTH + 2.0F, 2.0F, 0xFFFFFFFF);
    }

    private static void vertex(WorldRenderer worldRenderer, float vertexX, float vertexY, int color) {
        worldRenderer.pos(vertexX, vertexY, 0)
                .color((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F,
                        (color & 0xFF) / 255.0F, (color >>> 24) / 255.0F)
                .endVertex();
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!visible || button != 0) {
            return false;
        }
        float swatchX = x + width - 34.0F;
        if (mouseX >= swatchX && mouseX <= x + width && mouseY >= y && mouseY <= y + 13.0F) {
            expanded = !expanded;
            if (expanded) {
                readFromSetting();
            }
            return true;
        }
        if (!expanded) {
            return false;
        }

        float fieldY = y + 18.0F;
        if (inside(mouseX, mouseY, x, fieldY, FIELD_SIZE, FIELD_SIZE)) {
            dragTarget = 0;
            applyField(mouseX, mouseY, fieldY);
            return true;
        }
        float hueX = x + FIELD_SIZE + 6.0F;
        if (inside(mouseX, mouseY, hueX, fieldY, STRIP_WIDTH, FIELD_SIZE)) {
            dragTarget = 1;
            applyHue(mouseY, fieldY);
            return true;
        }
        float alphaX = hueX + STRIP_WIDTH + 5.0F;
        if (inside(mouseX, mouseY, alphaX, fieldY, STRIP_WIDTH, FIELD_SIZE)) {
            dragTarget = 2;
            applyAlpha(mouseY, fieldY);
            return true;
        }
        return false;
    }

    private static boolean inside(float mouseX, float mouseY, float boxX, float boxY, float boxWidth, float boxHeight) {
        return mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + boxHeight;
    }

    @Override
    public void update(float mouseX, float mouseY) {
        if (dragTarget < 0) {
            return;
        }
        float fieldY = y + 18.0F;
        switch (dragTarget) {
            case 0:
                applyField(mouseX, mouseY, fieldY);
                break;
            case 1:
                applyHue(mouseY, fieldY);
                break;
            default:
                applyAlpha(mouseY, fieldY);
                break;
        }
    }

    private void applyField(float mouseX, float mouseY, float fieldY) {
        saturation = MathUtil.clamp((mouseX - x) / FIELD_SIZE, 0.0F, 1.0F);
        brightness = 1.0F - MathUtil.clamp((mouseY - fieldY) / FIELD_SIZE, 0.0F, 1.0F);
        writeToSetting();
    }

    private void applyHue(float mouseY, float fieldY) {
        hue = MathUtil.clamp((mouseY - fieldY) / FIELD_SIZE, 0.0F, 1.0F);
        writeToSetting();
    }

    private void applyAlpha(float mouseY, float fieldY) {
        alpha = 1.0F - MathUtil.clamp((mouseY - fieldY) / FIELD_SIZE, 0.0F, 1.0F);
        writeToSetting();
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        dragTarget = -1;
    }
}
