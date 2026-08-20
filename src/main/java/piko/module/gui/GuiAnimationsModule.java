package piko.module.gui;

import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.NumberSetting;

/**
 * Speed of the interface animations.
 *
 * <p>Disabling the module removes every GUI transition, which is the cheapest possible
 * option for players who want the menu to appear instantly.</p>
 */
public class GuiAnimationsModule extends Module {

    private final NumberSetting speed;

    public GuiAnimationsModule() {
        super("Animations", "Menu and HUD transition speed", ModuleCategory.GUI, true);
        speed = settings.add(new NumberSetting("Animation Speed", 1.0D, 0.5D, 3.0D, 0.1D).suffix("x"));
    }

    public float getSpeed() {
        return isEnabled() ? speed.getFloat() : 0.0F;
    }
}
