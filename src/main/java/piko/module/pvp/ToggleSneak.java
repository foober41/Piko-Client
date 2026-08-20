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
 * Keeps the vanilla sneak binding held after a single press.
 *
 * <p>As with toggle sprint this only drives Minecraft's own key binding, so sneaking looks
 * and behaves identically to holding the key by hand.</p>
 */
public class ToggleSneak extends HudModule implements TickListener, KeyListener {

    private final BooleanSetting toggleMode;
    private final KeybindSetting sneakKey;
    private final BooleanSetting showStatus;

    private boolean toggled;
    private boolean forcing;

    public ToggleSneak() {
        super("Toggle Sneak", "Hold the sneak key for you", ModuleCategory.PVP, 0.5F, 0.76F, false);
        toggleMode = settings.add(new BooleanSetting("Toggle Sneak", true));
        sneakKey = settings.add((KeybindSetting) new KeybindSetting("Sneak Key", 0)
                .describe("Leave unbound to use the Minecraft sneak key"));
        showStatus = settings.add(new BooleanSetting("Show Status", true));
        enableTextColor(0xFF55CCFF);
        enableFont();
        enableBackground(false);
    }

    private int effectiveKey() {
        return sneakKey.isBound() ? sneakKey.get() : mc.gameSettings.keyBindSneak.getKeyCode();
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
        if (!toggleMode.get()) {
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
        if (toggleMode.get() && toggled) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            forcing = true;
        } else if (forcing) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            forcing = false;
        }
    }

    @Override
    protected void onDisable() {
        if (forcing) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            forcing = false;
        }
        toggled = false;
    }

    @Override
    public float getWidth() {
        return textWidth("[Sneaking]");
    }

    @Override
    public float getHeight() {
        return textHeight();
    }

    @Override
    protected void render(boolean editing) {
        if (!showStatus.get() && !editing) {
            return;
        }
        if (!editing && !toggled) {
            return;
        }
        String text = "[Sneaking]";
        drawBackground(textWidth(text), textHeight());
        font().drawStringWithShadow(text, 0, 0, getTextColor());
    }
}
