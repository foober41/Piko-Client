package piko.module.visual;

import piko.event.events.TickEvent;
import piko.event.listener.TickListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.NumberSetting;

/**
 * Raises the client brightness beyond the vanilla slider limit.
 *
 * <p>This is the same gamma value the vanilla video settings control, only allowed to go
 * higher. Nothing is transmitted and no block data is revealed that the client did not
 * already have.</p>
 */
public class Fullbright extends Module implements TickListener {

    private final NumberSetting brightness;

    private float savedGamma = 1.0F;
    private boolean saved;

    public Fullbright() {
        super("Fullbright", "Removes darkness from the world", ModuleCategory.VISUAL);
        brightness = settings.add(new NumberSetting("Brightness Level", 10.0D, 1.0D, 20.0D, 0.5D));
    }

    @Override
    protected void onEnable() {
        if (mc.gameSettings == null) {
            return;
        }
        if (!saved) {
            savedGamma = mc.gameSettings.gammaSetting;
            saved = true;
        }
    }

    @Override
    protected void onDisable() {
        if (mc.gameSettings != null && saved) {
            mc.gameSettings.gammaSetting = savedGamma;
            saved = false;
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.gameSettings == null) {
            return;
        }
        if (!saved) {
            savedGamma = mc.gameSettings.gammaSetting;
            saved = true;
        }
        mc.gameSettings.gammaSetting = brightness.getFloat();
    }
}
