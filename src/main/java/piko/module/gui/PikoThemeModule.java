package piko.module.gui;

import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.ColorSetting;
import piko.setting.NumberSetting;

/** Colours and shape of the Piko interface. */
public class PikoThemeModule extends Module {

    private final ColorSetting accent;
    private final ColorSetting secondary;
    private final NumberSetting cornerRadius;
    private final NumberSetting panelOpacity;

    public PikoThemeModule() {
        super("Piko Theme", "Accent colours and panel shape", ModuleCategory.GUI, true);
        accent = settings.add(new ColorSetting("Accent Color", 0xFF55CCFF));
        secondary = settings.add(new ColorSetting("Secondary Accent", 0xFF32E6E2));
        cornerRadius = settings.add(new NumberSetting("Corner Radius", 4.0D, 0.0D, 10.0D, 0.5D));
        panelOpacity = settings.add(new NumberSetting("Panel Opacity", 0.94D, 0.4D, 1.0D, 0.02D));
    }

    public int getAccent() {
        return accent.get();
    }

    public int getSecondaryAccent() {
        return secondary.get();
    }

    public float getCornerRadius() {
        return cornerRadius.getFloat();
    }

    public float getPanelOpacity() {
        return panelOpacity.getFloat();
    }
}
