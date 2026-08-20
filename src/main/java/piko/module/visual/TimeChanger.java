package piko.module.visual;

import piko.event.events.TickEvent;
import piko.event.listener.TickListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.ModeSetting;
import piko.setting.NumberSetting;
import piko.setting.Setting;

/**
 * Client side world time.
 *
 * <p>Only the local copy of the world clock is adjusted so the sky and light level look
 * the way the player wants. Nothing is sent to the server and other players are unaffected.</p>
 */
public class TimeChanger extends Module implements TickListener {

    private final ModeSetting mode;
    private final NumberSetting customTime;

    public TimeChanger() {
        super("Time Changer", "Client side time of day", ModuleCategory.VISUAL);
        mode = settings.add(new ModeSetting("Time", "Day",
                "Server Time", "Sunrise", "Day", "Sunset", "Night", "Midnight", "Custom"));
        customTime = settings.add((NumberSetting) new NumberSetting("Custom Time", 6000.0D, 0.0D, 24000.0D, 100.0D)
                .suffix(" ticks")
                .setVisibility(new Setting.VisibilityRule() {
                    @Override
                    public boolean isVisible() {
                        return mode.is("Custom");
                    }
                }));
    }

    private long targetTime() {
        if (mode.is("Sunrise")) {
            return 23000L;
        }
        if (mode.is("Day")) {
            return 1000L;
        }
        if (mode.is("Sunset")) {
            return 12500L;
        }
        if (mode.is("Night")) {
            return 14000L;
        }
        if (mode.is("Midnight")) {
            return 18000L;
        }
        return (long) customTime.get();
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.theWorld == null || mode.is("Server Time")) {
            return;
        }
        // The server keeps sending time updates, so the override is reapplied every tick.
        mc.theWorld.setWorldTime(targetTime());
    }
}
