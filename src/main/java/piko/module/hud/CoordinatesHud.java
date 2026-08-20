package piko.module.hud;

import piko.module.HudModule;
import piko.setting.BooleanSetting;
import piko.setting.ColorSetting;

/** X, Y and Z of the local player, optionally with the biome and the compass direction. */
public class CoordinatesHud extends HudModule {

    private final BooleanSetting singleLine;
    private final BooleanSetting decimals;
    private final BooleanSetting showDirection;
    private final ColorSetting labelColor;

    public CoordinatesHud() {
        super("Coordinates", "Player position", 0.008F, 0.115F);
        singleLine = settings.add(new BooleanSetting("Single Line", false));
        decimals = settings.add(new BooleanSetting("Decimals", false));
        showDirection = settings.add(new BooleanSetting("Direction", false));
        labelColor = settings.add(new ColorSetting("Label Color", 0xFF55CCFF));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    private String format(double value) {
        return decimals.get() ? String.format("%.1f", value) : String.valueOf((int) Math.floor(value));
    }

    private String[] lines(boolean editing) {
        String x;
        String y;
        String z;
        if (editing || mc.thePlayer == null) {
            x = "128";
            y = "64";
            z = "-256";
        } else {
            x = format(mc.thePlayer.posX);
            y = format(mc.thePlayer.posY);
            z = format(mc.thePlayer.posZ);
        }
        String direction = "";
        if (showDirection.get()) {
            direction = editing || mc.thePlayer == null
                    ? "NE"
                    : piko.util.PlayerUtil.getDirection(mc.thePlayer.rotationYaw);
        }
        if (singleLine.get()) {
            String combined = "X " + x + "  Y " + y + "  Z " + z;
            if (!direction.isEmpty()) {
                combined += "  " + direction;
            }
            return new String[]{combined};
        }
        if (!direction.isEmpty()) {
            return new String[]{"X " + x, "Y " + y, "Z " + z, direction};
        }
        return new String[]{"X " + x, "Y " + y, "Z " + z};
    }

    @Override
    public float getWidth() {
        float widest = 0;
        String[] lines = lines(true);
        for (int i = 0; i < lines.length; i++) {
            widest = Math.max(widest, textWidth(lines[i]));
        }
        return widest;
    }

    @Override
    public float getHeight() {
        return lines(true).length * (textHeight() + 1.0F) - 1.0F;
    }

    @Override
    protected void render(boolean editing) {
        String[] lines = lines(editing);
        drawBackground(getWidth(), getHeight());
        float lineHeight = textHeight() + 1.0F;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Draw the axis letter in the label colour and the value in the text colour.
            if (line.length() > 2 && line.charAt(1) == ' ') {
                font().drawStringWithShadow(line.substring(0, 1), 0, i * lineHeight, labelColor.get());
                font().drawStringWithShadow(line.substring(2), textWidth("X "), i * lineHeight, getTextColor());
            } else {
                font().drawStringWithShadow(line, 0, i * lineHeight, getTextColor());
            }
        }
    }
}
