package piko.module.visual;

import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import piko.animation.Animation;
import piko.event.events.TickEvent;
import piko.event.listener.TickListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.KeybindSetting;
import piko.setting.NumberSetting;
import piko.util.MathUtil;

/**
 * Built in zoom, no OptiFine required.
 *
 * <p>The field of view is reduced through the Forge FOV hook while the key is held. The
 * player's reach, hit detection and rotation are untouched; only the camera lens changes.</p>
 */
public class Zoom extends Module implements TickListener {

    private final KeybindSetting zoomKey;
    private final NumberSetting amount;
    private final BooleanSetting smoothCamera;
    private final BooleanSetting scrollAdjust;
    private final BooleanSetting smoothTransition;

    private final Animation transition = new Animation(0.0F, 120.0F);
    private float scrollFactor = 1.0F;
    private boolean zooming;
    private boolean savedSmoothCamera;

    public Zoom() {
        super("Zoom", "Hold a key to zoom in", ModuleCategory.VISUAL, true);
        zoomKey = settings.add(new KeybindSetting("Zoom Key", Keyboard.KEY_C));
        amount = settings.add(new NumberSetting("Zoom Amount", 4.0D, 1.5D, 12.0D, 0.5D).suffix("x"));
        smoothCamera = settings.add(new BooleanSetting("Smooth Camera", true));
        scrollAdjust = settings.add(new BooleanSetting("Scroll Wheel Adjustment", true));
        smoothTransition = settings.add(new BooleanSetting("Smooth Transition", true));
        useForgeEvents();
    }

    @Override
    public void onTick(TickEvent event) {
        boolean down = zoomKey.isDown() && mc.currentScreen == null;
        if (down != zooming) {
            zooming = down;
            if (down) {
                scrollFactor = 1.0F;
                if (smoothCamera.get()) {
                    savedSmoothCamera = mc.gameSettings.smoothCamera;
                    mc.gameSettings.smoothCamera = true;
                }
            } else if (smoothCamera.get()) {
                mc.gameSettings.smoothCamera = savedSmoothCamera;
            }
        }
        transition.setTarget(zooming ? 1.0F : 0.0F);

        if (zooming && scrollAdjust.get()) {
            int wheel = Mouse.getDWheel();
            if (wheel != 0) {
                scrollFactor = MathUtil.clamp(scrollFactor + (wheel > 0 ? 0.15F : -0.15F), 0.35F, 3.0F);
            }
        }
    }

    @Override
    protected void onDisable() {
        if (zooming && smoothCamera.get()) {
            mc.gameSettings.smoothCamera = savedSmoothCamera;
        }
        zooming = false;
        transition.snapTo(0.0F);
    }

    public boolean isZooming() {
        return zooming;
    }

    @SubscribeEvent
    public void onFovUpdate(FOVUpdateEvent event) {
        float progress = smoothTransition.get() ? transition.getValue() : (zooming ? 1.0F : 0.0F);
        if (progress <= 0.001F) {
            return;
        }
        float divisor = 1.0F + (amount.getFloat() * scrollFactor - 1.0F) * progress;
        event.newfov = event.newfov / divisor;
    }
}
