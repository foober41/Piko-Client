package piko.module.hud;

import piko.module.HudModule;
import piko.setting.BooleanSetting;
import piko.util.PlayerUtil;

/** Server latency in milliseconds. */
public class PingHud extends HudModule {

    private final BooleanSetting showPrefix;
    private final BooleanSetting colorByQuality;

    public PingHud() {
        super("Ping Display", "Latency to the current server", 0.008F, 0.080F);
        showPrefix = settings.add(new BooleanSetting("Prefix", false));
        colorByQuality = settings.add(new BooleanSetting("Color By Quality", true));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    private String text(boolean editing) {
        int ping = editing ? 32 : PlayerUtil.getPing();
        return showPrefix.get() ? "Ping " + ping + " ms" : ping + " ms";
    }

    private int color(boolean editing) {
        if (!colorByQuality.get()) {
            return getTextColor();
        }
        int ping = editing ? 32 : PlayerUtil.getPing();
        if (ping <= 60) {
            return 0xFF4ADE80;
        }
        if (ping <= 120) {
            return 0xFFFACC15;
        }
        return 0xFFF87171;
    }

    @Override
    public float getWidth() {
        return textWidth(text(true));
    }

    @Override
    public float getHeight() {
        return textHeight();
    }

    @Override
    protected void render(boolean editing) {
        String text = text(editing);
        drawBackground(textWidth(text), textHeight());
        font().drawStringWithShadow(text, 0, 0, color(editing));
    }
}
