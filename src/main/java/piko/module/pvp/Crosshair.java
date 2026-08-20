package piko.module.pvp;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import piko.event.events.OverlayEvent;
import piko.event.listener.OverlayListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.BooleanSetting;
import piko.setting.ColorSetting;
import piko.setting.ModeSetting;
import piko.setting.NumberSetting;
import piko.setting.Setting;

/**
 * Replaces the vanilla crosshair with a fully configurable one.
 *
 * <p>Presets write into the same sliders the player can edit, so picking a preset is a
 * starting point rather than a separate mode.</p>
 */
public class Crosshair extends Module implements OverlayListener {

    private final ModeSetting preset;
    private final NumberSetting lineWidth;
    private final NumberSetting lineHeight;
    private final NumberSetting thickness;
    private final NumberSetting gap;
    private final BooleanSetting centerDot;
    private final NumberSetting dotSize;
    private final BooleanSetting outline;
    private final NumberSetting outlineThickness;
    private final ColorSetting color;
    private final ColorSetting outlineColor;
    private final NumberSetting opacity;

    /** Guards against the preset listener firing while a preset is being applied. */
    private boolean applyingPreset;

    public Crosshair() {
        super("Crosshair", "Custom crosshair shape and colour", ModuleCategory.PVP);
        preset = settings.add(new ModeSetting("Preset", "Default", "Default", "PvP", "Small", "Dot", "Plus", "Large"));
        lineWidth = settings.add(new NumberSetting("Width", 5.0D, 0.0D, 20.0D, 1.0D));
        lineHeight = settings.add(new NumberSetting("Height", 5.0D, 0.0D, 20.0D, 1.0D));
        thickness = settings.add(new NumberSetting("Thickness", 1.0D, 1.0D, 5.0D, 1.0D));
        gap = settings.add(new NumberSetting("Gap", 3.0D, 0.0D, 12.0D, 1.0D));
        centerDot = settings.add(new BooleanSetting("Center Dot", false));
        dotSize = settings.add((NumberSetting) new NumberSetting("Dot Size", 1.0D, 1.0D, 5.0D, 1.0D)
                .setVisibility(new Setting.VisibilityRule() {
                    @Override
                    public boolean isVisible() {
                        return centerDot.get();
                    }
                }));
        outline = settings.add(new BooleanSetting("Outline", true));
        outlineThickness = settings.add((NumberSetting) new NumberSetting("Outline Thickness", 1.0D, 0.5D, 3.0D, 0.5D)
                .setVisibility(new Setting.VisibilityRule() {
                    @Override
                    public boolean isVisible() {
                        return outline.get();
                    }
                }));
        color = settings.add(new ColorSetting("Crosshair Color", 0xFF55CCFF));
        outlineColor = settings.add(new ColorSetting("Outline Color", 0xC0000000));
        opacity = settings.add(new NumberSetting("Opacity", 1.0D, 0.1D, 1.0D, 0.05D));

        preset.onChange(new Setting.ChangeListener() {
            @Override
            public void onSettingChanged(Setting setting) {
                applyPreset();
            }
        });
    }

    private void applyPreset() {
        if (applyingPreset) {
            return;
        }
        applyingPreset = true;
        try {
            if (preset.is("PvP")) {
                set(4, 4, 1, 2, false, true);
            } else if (preset.is("Small")) {
                set(3, 3, 1, 1, false, true);
            } else if (preset.is("Dot")) {
                set(0, 0, 1, 0, true, true);
            } else if (preset.is("Plus")) {
                set(5, 5, 1, 0, false, false);
            } else if (preset.is("Large")) {
                set(9, 9, 2, 4, false, true);
            } else {
                set(5, 5, 1, 3, false, true);
            }
        } finally {
            applyingPreset = false;
        }
    }

    private void set(int width, int height, int line, int space, boolean dot, boolean withOutline) {
        lineWidth.set(width);
        lineHeight.set(height);
        thickness.set(line);
        gap.set(space);
        centerDot.set(dot);
        outline.set(withOutline);
    }

    @Override
    public void onRenderOverlay(OverlayEvent event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }
        event.cancel();
        draw();
    }

    private void draw() {
        ScaledResolution resolution = piko.PikoClient.getInstance().getEventBridge().getResolution();
        float centerX = resolution.getScaledWidth() / 2.0F;
        float centerY = resolution.getScaledHeight() / 2.0F;

        float alpha = opacity.getFloat();
        int main = ColorUtil.alpha(color.get(), alpha * ((color.get() >>> 24) / 255.0F));
        int border = ColorUtil.alpha(outlineColor.get(), alpha * ((outlineColor.get() >>> 24) / 255.0F));

        float line = thickness.getFloat();
        float space = gap.getFloat();
        float armX = lineWidth.getFloat();
        float armY = lineHeight.getFloat();
        float outlineSize = outline.get() ? outlineThickness.getFloat() : 0.0F;

        // Outline first so the coloured arms sit on top of it.
        if (outlineSize > 0) {
            drawArms(centerX, centerY, armX, armY, line, space, outlineSize, border);
        }
        drawArms(centerX, centerY, armX, armY, line, space, 0.0F, main);

        if (centerDot.get()) {
            float dot = dotSize.getFloat();
            if (outlineSize > 0) {
                RenderUtil.drawRect(centerX - dot / 2.0F - outlineSize, centerY - dot / 2.0F - outlineSize,
                        dot + outlineSize * 2, dot + outlineSize * 2, border);
            }
            RenderUtil.drawRect(centerX - dot / 2.0F, centerY - dot / 2.0F, dot, dot, main);
        }
    }

    private void drawArms(float centerX, float centerY, float armX, float armY,
                          float line, float space, float grow, int arms) {
        if (armX > 0) {
            RenderUtil.drawRect(centerX - space - armX - grow, centerY - line / 2.0F - grow,
                    armX + grow * 2, line + grow * 2, arms);
            RenderUtil.drawRect(centerX + space - grow, centerY - line / 2.0F - grow,
                    armX + grow * 2, line + grow * 2, arms);
        }
        if (armY > 0) {
            RenderUtil.drawRect(centerX - line / 2.0F - grow, centerY - space - armY - grow,
                    line + grow * 2, armY + grow * 2, arms);
            RenderUtil.drawRect(centerX - line / 2.0F - grow, centerY + space - grow,
                    line + grow * 2, armY + grow * 2, arms);
        }
    }
}
