package piko.module.pvp;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import piko.event.events.TickEvent;
import piko.event.listener.TickListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.render.PikoRenderPlayer;
import piko.setting.ColorSetting;
import piko.setting.NumberSetting;

import java.util.HashMap;
import java.util.Map;

/**
 * Recolours the damage flash drawn on players.
 *
 * <p>Purely cosmetic. The overlay is produced by swapping in a player renderer that uses
 * the configured colour; hit detection, knockback and damage are untouched.</p>
 */
public class HitColor extends Module implements TickListener {

    private final ColorSetting color;
    private final NumberSetting red;
    private final NumberSetting green;
    private final NumberSetting blue;
    private final NumberSetting strength;

    private final Map<String, RenderPlayer> originalRenderers = new HashMap<String, RenderPlayer>();
    private boolean installed;
    private boolean syncingFromColor;

    public HitColor() {
        super("Hit Color", "Colour of the damage flash on players", ModuleCategory.PVP);
        color = settings.add(new ColorSetting("Hit Color", 0xFFFF0000));
        red = settings.add(new NumberSetting("Red", 1.0D, 0.0D, 1.0D, 0.01D));
        green = settings.add(new NumberSetting("Green", 0.0D, 0.0D, 1.0D, 0.01D));
        blue = settings.add(new NumberSetting("Blue", 0.0D, 0.0D, 1.0D, 0.01D));
        strength = settings.add(new NumberSetting("Opacity", 0.3D, 0.0D, 1.0D, 0.01D));

        // The picker and the individual channels edit the same colour from both directions.
        color.onChange(setting -> {
            if (syncingFromColor) {
                return;
            }
            syncingFromColor = true;
            red.set(((color.get() >> 16) & 0xFF) / 255.0D);
            green.set(((color.get() >> 8) & 0xFF) / 255.0D);
            blue.set((color.get() & 0xFF) / 255.0D);
            syncingFromColor = false;
        });
        piko.setting.Setting.ChangeListener channelListener = setting -> {
            if (syncingFromColor) {
                return;
            }
            syncingFromColor = true;
            color.setComponents((int) (red.get() * 255), (int) (green.get() * 255), (int) (blue.get() * 255), 255);
            syncingFromColor = false;
        };
        red.onChange(channelListener);
        green.onChange(channelListener);
        blue.onChange(channelListener);
    }

    public float getRed() {
        return red.getFloat();
    }

    public float getGreen() {
        return green.getFloat();
    }

    public float getBlue() {
        return blue.getFloat();
    }

    public float getStrength() {
        return strength.getFloat();
    }

    @Override
    public void onTick(TickEvent event) {
        // The render manager only exists once the game is fully started, so installation
        // happens on the first tick after the module is enabled and then never again.
        if (!installed) {
            install();
        }
    }

    @Override
    protected void onDisable() {
        restore();
    }

    private void install() {
        RenderManager manager = mc.getRenderManager();
        if (manager == null || manager.skinMap == null) {
            return;
        }
        originalRenderers.clear();
        originalRenderers.putAll(manager.skinMap);
        manager.skinMap.put("default", new PikoRenderPlayer(manager, false, this));
        manager.skinMap.put("slim", new PikoRenderPlayer(manager, true, this));
        installed = true;
    }

    private void restore() {
        if (!installed) {
            return;
        }
        RenderManager manager = mc.getRenderManager();
        if (manager != null && manager.skinMap != null) {
            manager.skinMap.putAll(originalRenderers);
        }
        installed = false;
    }
}
