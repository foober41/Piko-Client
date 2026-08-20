package piko.module.pvp;

import piko.module.HudModule;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.util.PlayerUtil;

/** Compass direction the player is facing, optionally with the raw yaw. */
public class DirectionHud extends HudModule {

    private final BooleanSetting showYaw;
    private final BooleanSetting longNames;

    public DirectionHud() {
        super("Direction HUD", "Facing direction", ModuleCategory.PVP, 0.5F, 0.05F, false);
        showYaw = settings.add(new BooleanSetting("Show Yaw", false));
        longNames = settings.add(new BooleanSetting("Long Names", false));
        enableBackground(false);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    private static String expand(String direction) {
        if (direction.equals("N")) {
            return "North";
        }
        if (direction.equals("S")) {
            return "South";
        }
        if (direction.equals("E")) {
            return "East";
        }
        if (direction.equals("W")) {
            return "West";
        }
        if (direction.equals("NE")) {
            return "North East";
        }
        if (direction.equals("NW")) {
            return "North West";
        }
        if (direction.equals("SE")) {
            return "South East";
        }
        return "South West";
    }

    private String text(boolean editing) {
        float yaw = editing || mc.thePlayer == null ? 45.0F : mc.thePlayer.rotationYaw;
        String direction = PlayerUtil.getDirection(yaw);
        if (longNames.get()) {
            direction = expand(direction);
        }
        if (showYaw.get()) {
            return direction + " " + Math.round(piko.util.MathUtil.wrapDegrees(yaw)) + "\u00B0";
        }
        return direction;
    }

    @Override
    public float getWidth() {
        return Math.max(textWidth(text(true)), textWidth(longNames.get() ? "North West" : "NW"));
    }

    @Override
    public float getHeight() {
        return textHeight();
    }

    @Override
    protected void render(boolean editing) {
        String text = text(editing);
        drawBackground(getWidth(), textHeight());
        font().drawStringWithShadow(text, 0, 0, getTextColor());
    }
}
