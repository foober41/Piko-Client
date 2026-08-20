package piko.module.gui;

import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.ModeSetting;
import piko.setting.Setting;

/**
 * Overrides the vanilla GUI scale.
 *
 * <p>The value is written straight into the Minecraft game settings, so it behaves exactly
 * like changing the option in the vanilla video settings and survives without extra code.</p>
 */
public class GuiScaleModule extends Module {

    private final ModeSetting scale;

    public GuiScaleModule() {
        super("GUI Scale", "Overrides the Minecraft GUI scale", ModuleCategory.GUI);
        scale = settings.add(new ModeSetting("Scale", "Normal", "Auto", "Small", "Normal", "Large"));
        scale.onChange(new Setting.ChangeListener() {
            @Override
            public void onSettingChanged(Setting setting) {
                apply();
            }
        });
    }

    private int scaleValue() {
        if (scale.is("Auto")) {
            return 0;
        }
        if (scale.is("Small")) {
            return 1;
        }
        if (scale.is("Large")) {
            return 3;
        }
        return 2;
    }

    private void apply() {
        if (!isEnabled() || mc.gameSettings == null) {
            return;
        }
        mc.gameSettings.guiScale = scaleValue();
        if (mc.currentScreen != null) {
            mc.currentScreen.setWorldAndResolution(mc, mc.displayWidth, mc.displayHeight);
        }
        piko.PikoClient client = piko.PikoClient.getInstance();
        if (client != null && client.getEventBridge() != null) {
            client.getEventBridge().invalidateResolution();
        }
    }

    @Override
    protected void onEnable() {
        apply();
    }

    @Override
    public void onPostInit() {
        apply();
    }
}
