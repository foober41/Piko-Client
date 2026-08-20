package piko.module.gui;

import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;

/** Replaces the vanilla 1.8.9 main menu with the Piko branded one. */
public class MainMenuModule extends Module {

    private final BooleanSetting animatedBackground;

    public MainMenuModule() {
        super("Piko Main Menu", "Branded replacement for the vanilla menu", ModuleCategory.GUI, true);
        animatedBackground = settings.add(new BooleanSetting("Animated Background", true));
    }

    public boolean isAnimatedBackground() {
        return animatedBackground.get();
    }
}
