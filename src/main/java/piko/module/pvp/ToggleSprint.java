package piko.module.pvp;

import net.minecraft.client.settings.KeyBinding;
import piko.event.events.KeyPressEvent;
import piko.event.events.TickEvent;
import piko.event.listener.KeyListener;
import piko.event.listener.TickListener;
import piko.module.HudModule;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.KeybindSetting;

/**
 * Automates holding the vanilla sprint key.
 *
 * <p>Piko only presses and releases Minecraft's own sprint binding. Movement speed,
 * acceleration and the packets sent to the server are exactly what vanilla produces while
 * the player holds the key themselves.</p>
 */
public class ToggleSprint extends HudModule implements TickListener, KeyListener {

    private final BooleanSetting toggleMode;
    private final BooleanSetting alwaysSprint;
    private final KeybindSetting sprintKey;
    private final BooleanSetting hudIndicator;
    private final BooleanSetting hideWhenInactive;

    private boolean toggled;
    private boolean forcing;

    public ToggleSprint() {
        super("Toggle Sprint", "Hold the sprint key for you", ModuleCategory.PVP, 0.5F, 0.72F, false);
        toggleMode = settings.add(new BooleanSetting("Toggle Sprint", true));
        alwaysSprint = settings.add(new BooleanSetting("Always Sprint", false));
        sprintKey = settings.add((KeybindSetting) new KeybindSetting("Sprint Key", 0)
                .describe("Leave unbound to use the Minecraft sprint key"));
        hudIndicator = settings.add(new BooleanSetting("HUD Indicator", true));
        hideWhenInactive = settings.add(new BooleanSetting("Hide When Inactive", true));
        enableTextColor(0xFF55CCFF);
        enableFont();
        enableBackground(false);
    }

    private int effectiveKey() {
        if (sprintKey.isBound()) {
            return sprintKey.get();
        }
        return mc.gameSettings.keyBindSprint.getKeyCode();
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
        if (!toggleMode.get() || alwaysSprint.get()) {
            return;
        }
        if (event.getKey() == effectiveKey()) {
            toggled = !toggled;
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null) {
            return;
        }
        boolean shouldSprint = alwaysSprint.get() || (toggleMode.get() && toggled);
        if (shouldSprint) {
            // Holding the binding is all vanilla needs; it decides on its own whether the
            // player is actually allowed to sprint right now.
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            forcing = true;
        } else if (forcing) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            forcing = false;
        }
    }

    @Override
    protected void onDisable() {
        if (forcing) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            forcing = false;
        }
        toggled = false;
    }

    private boolean isSprinting() {
        return mc.thePlayer != null && mc.thePlayer.isSprinting();
    }

    private String text(boolean editing) {
        if (editing) {
            return "[Sprinting]";
        }
        if (alwaysSprint.get()) {
            return isSprinting() ? "[Sprinting]" : "[Always Sprint]";
        }
        if (toggled) {
            return isSprinting() ? "[Sprinting]" : "[Sprint Toggled]";
        }
        return "[Sprint Off]";
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
        if (!hudIndicator.get() && !editing) {
            return;
        }
        boolean active = alwaysSprint.get() || toggled;
        if (!editing && !active && hideWhenInactive.get()) {
            return;
        }
        String text = text(editing);
        drawBackground(textWidth(text), textHeight());
        font().drawStringWithShadow(text, 0, 0, active || editing ? getTextColor() : 0xFF9AA3AF);
    }
}
