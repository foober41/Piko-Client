package piko.module.visual;

import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;

/** Dropped items lie flat on the ground instead of floating and spinning. */
public class ItemPhysics extends Module {

    private final BooleanSetting bobbing;
    private final BooleanSetting stackDepth;
    private final NumberSetting scale;
    private final NumberSetting rotationOffset;

    public ItemPhysics() {
        super("Item Physics", "Dropped items rest on the ground", ModuleCategory.VISUAL);
        bobbing = settings.add(new BooleanSetting("Bobbing", false));
        stackDepth = settings.add(new BooleanSetting("Stack Depth", true));
        scale = settings.add(new NumberSetting("Item Scale", 1.0D, 0.5D, 1.5D, 0.05D).suffix("x"));
        rotationOffset = settings.add(new NumberSetting("Rotation Offset", 0.0D, 0.0D, 360.0D, 5.0D).suffix("\u00B0"));
    }

    public boolean isBobbing() {
        return bobbing.get();
    }

    public boolean isStackDepth() {
        return stackDepth.get();
    }

    public float getScale() {
        return scale.getFloat();
    }

    public float getRotationOffset() {
        return rotationOffset.getFloat();
    }
}
