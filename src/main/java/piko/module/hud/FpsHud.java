package piko.module.hud;

import net.minecraft.client.Minecraft;
import piko.module.HudModule;
import piko.setting.ModeSetting;
import piko.setting.Setting;
import piko.setting.StringSetting;

/** Shows the frame rate Minecraft itself reports, for example {@code 243 FPS}. */
public class FpsHud extends HudModule {

    private final ModeSetting prefixMode;
    private final StringSetting customPrefix;

    public FpsHud() {
        super("FPS Counter", "Current frames per second", 0.008F, 0.010F, true);
        prefixMode = settings.add(new ModeSetting("Prefix", "Suffix", "None", "Prefix", "Suffix", "Custom"));
        customPrefix = settings.add((StringSetting) new StringSetting("Custom Prefix", "FPS", 12)
                .setVisibility(new Setting.VisibilityRule() {
                    @Override
                    public boolean isVisible() {
                        return prefixMode.is("Custom");
                    }
                }));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    private String text() {
        int fps = Minecraft.getDebugFPS();
        if (prefixMode.is("None")) {
            return String.valueOf(fps);
        }
        if (prefixMode.is("Prefix")) {
            return "FPS " + fps;
        }
        if (prefixMode.is("Custom")) {
            return customPrefix.get() + " " + fps;
        }
        return fps + " FPS";
    }

    @Override
    public float getWidth() {
        return textWidth(text());
    }

    @Override
    public float getHeight() {
        return textHeight();
    }

    @Override
    protected void render(boolean editing) {
        String text = text();
        drawBackground(textWidth(text), textHeight());
        font().drawStringWithShadow(text, 0, 0, getTextColor());
    }
}
