package piko.module.hud;

import piko.gui.Theme;
import piko.module.HudModule;
import piko.render.ColorUtil;
import piko.render.RenderUtil;
import piko.setting.BooleanSetting;

/** Heap usage of the game, useful when hunting frame drops during long sessions. */
public class MemoryHud extends HudModule {

    private static final long REFRESH_INTERVAL = 500L;

    private final BooleanSetting showBar;
    private final BooleanSetting showMegabytes;

    private long usedMb;
    private long maxMb;
    private int percent;
    private long lastUpdate;

    public MemoryHud() {
        super("Memory Usage", "Java heap usage", 0.86F, 0.06F);
        showBar = settings.add(new BooleanSetting("Usage Bar", true));
        showMegabytes = settings.add(new BooleanSetting("Show Megabytes", true));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    /** Querying the runtime twice a second is more than enough for a readout. */
    private void refresh() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate < REFRESH_INTERVAL) {
            return;
        }
        lastUpdate = now;
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        usedMb = used / 1024L / 1024L;
        maxMb = max / 1024L / 1024L;
        percent = max <= 0 ? 0 : (int) (used * 100L / max);
    }

    private String text() {
        refresh();
        if (showMegabytes.get()) {
            return percent + "% " + usedMb + "/" + maxMb + " MB";
        }
        return percent + "% Memory";
    }

    @Override
    public float getWidth() {
        return Math.max(textWidth(text()), showBar.get() ? 60.0F : 0.0F);
    }

    @Override
    public float getHeight() {
        return textHeight() + (showBar.get() ? 5.0F : 0.0F);
    }

    @Override
    protected void render(boolean editing) {
        String text = text();
        drawBackground(getWidth(), getHeight());
        font().drawStringWithShadow(text, 0, 0, getTextColor());

        if (!showBar.get()) {
            return;
        }
        float barY = textHeight() + 1.5F;
        float barWidth = getWidth();
        RenderUtil.drawRoundedRect(0, barY, barWidth, 2.5F, 1.25F, ColorUtil.alpha(0xFF000000, 0.5F));
        int color = percent > 85 ? 0xFFF87171 : (percent > 65 ? 0xFFFACC15 : Theme.accent());
        RenderUtil.drawRoundedRect(0, barY, barWidth * percent / 100.0F, 2.5F, 1.25F, color);
    }
}
