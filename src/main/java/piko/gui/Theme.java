package piko.gui;

import piko.PikoClient;
import piko.module.gui.GuiAnimationsModule;
import piko.module.gui.PikoThemeModule;
import piko.render.ColorUtil;

/**
 * Central colour palette for the whole client: black, very dark gray, light blue and
 * cyan accents, white primary text and gray secondary text.
 *
 * <p>The accent colour and the animation speed are read live from the {@code Piko Theme}
 * module so changing them updates every screen instantly.</p>
 */
public final class Theme {

    public static final int BLACK = 0xFF07080A;
    public static final int BACKGROUND = 0xF00B0C0F;
    public static final int PANEL = 0xFF121419;
    public static final int PANEL_LIGHT = 0xFF181B21;
    public static final int PANEL_HOVER = 0xFF1E222A;
    public static final int DIVIDER = 0xFF23272F;

    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFF9AA3AF;
    public static final int TEXT_DISABLED = 0xFF5C636E;

    public static final int DEFAULT_ACCENT = 0xFF55CCFF;
    public static final int CYAN = 0xFF32E6E2;
    public static final int OFF = 0xFF39404B;

    private Theme() {
    }

    public static int accent() {
        PikoThemeModule theme = themeModule();
        return theme == null ? DEFAULT_ACCENT : theme.getAccent();
    }

    public static int accentSecondary() {
        PikoThemeModule theme = themeModule();
        return theme == null ? CYAN : theme.getSecondaryAccent();
    }

    /** Accent colour at a reduced alpha, used for outlines and glows. */
    public static int accentSoft(float alpha) {
        return ColorUtil.alpha(accent(), alpha);
    }

    /** Opacity of the large Piko panels, configurable in the Piko Theme module. */
    public static float panelOpacity() {
        PikoThemeModule theme = themeModule();
        return theme == null ? 0.94F : theme.getPanelOpacity();
    }

    public static float cornerRadius() {
        PikoThemeModule theme = themeModule();
        return theme == null ? 4.0F : theme.getCornerRadius();
    }

    /** Multiplier applied to every GUI animation duration; 0 disables animations. */
    public static float animationSpeed() {
        PikoClient client = PikoClient.getInstance();
        if (client == null || client.getModuleManager() == null) {
            return 1.0F;
        }
        GuiAnimationsModule animations = client.getModuleManager().getModule(GuiAnimationsModule.class);
        return animations == null ? 1.0F : animations.getSpeed();
    }

    public static float animationDuration(float baseMillis) {
        float speed = animationSpeed();
        if (speed <= 0.01F) {
            return 1.0F;
        }
        return baseMillis / speed;
    }

    private static PikoThemeModule themeModule() {
        PikoClient client = PikoClient.getInstance();
        if (client == null || client.getModuleManager() == null) {
            return null;
        }
        return client.getModuleManager().getModule(PikoThemeModule.class);
    }
}
