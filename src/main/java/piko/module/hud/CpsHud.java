package piko.module.hud;

import piko.module.HudModule;
import piko.setting.BooleanSetting;
import piko.setting.ColorSetting;
import piko.setting.ModeSetting;
import piko.util.ClickCounters;

/**
 * Clicks per second display.
 *
 * <p>The numbers come from counting the player's real mouse input during the last second.
 * Piko has no auto clicker and cannot produce a click of its own.</p>
 */
public class CpsHud extends HudModule {

    private final BooleanSetting leftClick;
    private final BooleanSetting rightClick;
    private final ModeSetting style;
    private final ColorSetting accentColor;

    public CpsHud() {
        super("CPS Counter", "Measured clicks per second", 0.008F, 0.045F);
        leftClick = settings.add(new BooleanSetting("Left Click CPS", true));
        rightClick = settings.add(new BooleanSetting("Right Click CPS", false));
        style = settings.add(new ModeSetting("Display Style", "Labelled", "Labelled", "Combined", "Compact"));
        accentColor = settings.add(new ColorSetting("Accent Color", 0xFF55CCFF));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    private String text(boolean editing) {
        int left = editing ? 8 : ClickCounters.left();
        int right = editing ? 3 : ClickCounters.right();

        if (style.is("Combined")) {
            int total = (leftClick.get() ? left : 0) + (rightClick.get() ? right : 0);
            return total + " CPS";
        }
        if (style.is("Compact")) {
            if (leftClick.get() && rightClick.get()) {
                return left + " | " + right;
            }
            return String.valueOf(leftClick.get() ? left : right);
        }
        if (leftClick.get() && rightClick.get()) {
            return "LMB " + left + " | RMB " + right;
        }
        if (rightClick.get()) {
            return "RMB " + right;
        }
        return left + " CPS";
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

        // Digits use the text colour, the separators and labels the accent colour.
        float cursor = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            String single = String.valueOf(character);
            int color = Character.isDigit(character) ? getTextColor() : accentColor.get();
            font().drawStringWithShadow(single, cursor, 0, color);
            cursor += font().getStringWidth(single);
        }
    }
}
