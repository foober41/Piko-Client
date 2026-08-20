package piko.module.hud;

import piko.module.HudModule;
import piko.setting.BooleanSetting;
import piko.setting.ModeSetting;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Real world clock. */
public class ClockHud extends HudModule {

    private final ModeSetting format;
    private final BooleanSetting seconds;

    private SimpleDateFormat formatter;
    private String cached = "";
    private long lastUpdate;
    private String lastPattern = "";

    public ClockHud() {
        super("Clock", "Local time of day", 0.86F, 0.02F);
        format = settings.add(new ModeSetting("Format", "24 Hour", "24 Hour", "12 Hour"));
        seconds = settings.add(new BooleanSetting("Seconds", false));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    private String pattern() {
        String base = format.is("12 Hour") ? "hh:mm" : "HH:mm";
        if (seconds.get()) {
            base += ":ss";
        }
        return format.is("12 Hour") ? base + " a" : base;
    }

    /** Formatting once per second is plenty and avoids per frame allocations. */
    private String text() {
        long now = System.currentTimeMillis();
        String pattern = pattern();
        if (formatter == null || !pattern.equals(lastPattern)) {
            formatter = new SimpleDateFormat(pattern);
            lastPattern = pattern;
            lastUpdate = 0;
        }
        if (now - lastUpdate >= 500L) {
            cached = formatter.format(new Date(now));
            lastUpdate = now;
        }
        return cached;
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
