package piko.module;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import piko.event.events.Render2DEvent;
import piko.event.listener.Render2DListener;
import piko.font.FontManager;
import piko.font.IFont;
import piko.font.MinecraftFont;
import piko.gui.Theme;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.BooleanSetting;
import piko.setting.ColorSetting;
import piko.setting.ModeSetting;
import piko.setting.NumberSetting;
import piko.setting.Setting;
import piko.util.MathUtil;

/**
 * Base class for everything drawn on the in game HUD.
 *
 * <p>Positions are stored as a fraction of the screen so a HUD laid out on one resolution
 * stays where the player put it after switching to another. Subclasses only report their
 * size and draw themselves at the origin; scaling, placement and the shared background are
 * handled here.</p>
 */
public abstract class HudModule extends Module implements Render2DListener {

    private final float defaultX;
    private final float defaultY;

    private float x;
    private float y;

    private final NumberSetting scale = new NumberSetting("Scale", 1.0D, 0.5D, 3.0D, 0.05D)
            .suffix("x");

    private BooleanSetting background;
    private NumberSetting backgroundOpacity;
    private ColorSetting backgroundColor;
    private ColorSetting textColor;
    private ModeSetting fontSetting;

    protected HudModule(String name, String description, float defaultX, float defaultY) {
        this(name, description, defaultX, defaultY, false);
    }

    protected HudModule(String name, String description, float defaultX, float defaultY, boolean enabledByDefault) {
        this(name, description, ModuleCategory.HUD, defaultX, defaultY, enabledByDefault);
    }

    protected HudModule(String name, String description, ModuleCategory category,
                        float defaultX, float defaultY, boolean enabledByDefault) {
        super(name, description, category, enabledByDefault);
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.x = defaultX;
        this.y = defaultY;
    }

    /** Adds the shared background toggle, opacity slider and colour. */
    protected void enableBackground(boolean defaultOn) {
        background = settings.add(new BooleanSetting("Background", defaultOn));
        backgroundColor = settings.add(new ColorSetting("Background Color", 0xFF0B0C0F));
        backgroundOpacity = settings.add((NumberSetting) new NumberSetting("Background Opacity", 0.55D, 0.0D, 1.0D, 0.05D)
                .setVisibility(new Setting.VisibilityRule() {
                    @Override
                    public boolean isVisible() {
                        return background.get();
                    }
                }));
    }

    protected ColorSetting enableTextColor(int defaultColor) {
        textColor = settings.add(new ColorSetting("Text Color", defaultColor));
        return textColor;
    }

    protected ModeSetting enableFont() {
        fontSetting = settings.add(new ModeSetting("Font", "Minecraft", FontManager.options()));
        return fontSetting;
    }

    @Override
    public void finalizeSettings() {
        settings.add(scale);
    }

    public NumberSetting getScaleSetting() {
        return scale;
    }

    public float getScale() {
        return scale.getFloat();
    }

    public void setScale(double value) {
        scale.set(value);
    }

    public float getRelativeX() {
        return x;
    }

    public float getRelativeY() {
        return y;
    }

    public void setRelativePosition(float relativeX, float relativeY) {
        this.x = MathUtil.clamp(relativeX, -0.5F, 1.5F);
        this.y = MathUtil.clamp(relativeY, -0.5F, 1.5F);
    }

    public void resetPosition() {
        this.x = defaultX;
        this.y = defaultY;
    }

    public float getPixelX(ScaledResolution resolution) {
        return Math.round(x * resolution.getScaledWidth());
    }

    public float getPixelY(ScaledResolution resolution) {
        return Math.round(y * resolution.getScaledHeight());
    }

    public void setPixelPosition(ScaledResolution resolution, float pixelX, float pixelY) {
        setRelativePosition(pixelX / resolution.getScaledWidth(), pixelY / resolution.getScaledHeight());
    }

    /** Width of the element after scaling, used for hit testing in the HUD editor. */
    public float getScaledWidth() {
        return getWidth() * getScale();
    }

    public float getScaledHeight() {
        return getHeight() * getScale();
    }

    /** Unscaled width of the drawn content. */
    public abstract float getWidth();

    /** Unscaled height of the drawn content. */
    public abstract float getHeight();

    /**
     * Draws the element with its origin at 0,0.
     *
     * @param editing true while the HUD editor is open, which lets elements show sample
     *                values instead of live data that may not exist outside a world
     */
    protected abstract void render(boolean editing);

    @Override
    public void onRender2D(Render2DEvent event) {
        renderAt(event.getResolution(), false);
    }

    public void renderAt(ScaledResolution resolution, boolean editing) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(getPixelX(resolution), getPixelY(resolution), 0.0F);
        float factor = getScale();
        GlStateManager.scale(factor, factor, 1.0F);
        render(editing);
        GlStateManager.popMatrix();
        RenderUtil.resetState();
    }

    /** Draws the shared rounded background behind the element, if it is enabled. */
    protected void drawBackground(float width, float height) {
        if (background == null || !background.get()) {
            return;
        }
        int color = ColorUtil.alpha(backgroundColor.get(), backgroundOpacity.getFloat());
        RenderUtil.drawRoundedRect(-2.0F, -1.0F, width + 4.0F, height + 2.0F, Theme.cornerRadius(), color);
    }

    protected boolean hasBackground() {
        return background != null && background.get();
    }

    protected int getTextColor() {
        return textColor == null ? 0xFFFFFFFF : textColor.get();
    }

    protected IFont font() {
        return fontSetting == null ? MinecraftFont.INSTANCE : FontManager.byName(fontSetting.get());
    }

    /** Convenience for elements that are a single line of text. */
    protected float textWidth(String text) {
        return font().getStringWidth(text);
    }

    protected float textHeight() {
        return font().getHeight();
    }
}
