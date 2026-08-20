package piko.module.visual;

import org.lwjgl.input.Keyboard;
import piko.event.events.FrameEvent;
import piko.event.events.KeyPressEvent;
import piko.event.events.TickEvent;
import piko.event.listener.FrameListener;
import piko.event.listener.KeyListener;
import piko.event.listener.TickListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.KeybindSetting;
import piko.setting.NumberSetting;
import piko.util.MathUtil;

/**
 * Free look: turn the camera without turning the player.
 *
 * <p>The trick is entirely camera side. While free look is active the mouse drives a
 * private camera rotation that is applied only for the duration of the frame; the
 * rotation the game ticks with, and therefore everything the server sees, is frozen at the
 * direction the player was facing when they pressed the key.</p>
 */
public class Perspective extends Module implements FrameListener, TickListener, KeyListener {

    private final KeybindSetting key;
    private final BooleanSetting holdMode;
    private final NumberSetting sensitivity;
    private final BooleanSetting frontView;

    private boolean active;
    private float cameraYaw;
    private float cameraPitch;
    private float bodyYaw;
    private float bodyPitch;
    private int savedPerspective;
    private float savedSensitivity;

    public Perspective() {
        super("Perspective", "Look around without turning your body", ModuleCategory.VISUAL);
        key = settings.add(new KeybindSetting("Perspective Key", Keyboard.KEY_LMENU));
        holdMode = settings.add(new BooleanSetting("Hold Mode", true));
        sensitivity = settings.add(new NumberSetting("Sensitivity", 1.0D, 0.2D, 2.0D, 0.05D).suffix("x"));
        frontView = settings.add(new BooleanSetting("Front View", false));
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
        if (holdMode.get() || !key.matches(event.getKey())) {
            return;
        }
        if (active) {
            stop();
        } else {
            start();
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (!holdMode.get()) {
            if (active && mc.thePlayer == null) {
                stop();
            }
            return;
        }
        boolean down = key.isDown() && mc.currentScreen == null && mc.thePlayer != null;
        if (down && !active) {
            start();
        } else if (!down && active) {
            stop();
        }
    }

    private void start() {
        if (mc.thePlayer == null) {
            return;
        }
        active = true;
        bodyYaw = mc.thePlayer.rotationYaw;
        bodyPitch = mc.thePlayer.rotationPitch;
        cameraYaw = bodyYaw;
        cameraPitch = bodyPitch;
        savedPerspective = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = frontView.get() ? 2 : 1;
        savedSensitivity = mc.gameSettings.mouseSensitivity;
        mc.gameSettings.mouseSensitivity = savedSensitivity * sensitivity.getFloat();
    }

    private void stop() {
        if (!active) {
            return;
        }
        active = false;
        mc.gameSettings.thirdPersonView = savedPerspective;
        mc.gameSettings.mouseSensitivity = savedSensitivity;
        restoreBody();
    }

    @Override
    protected void onDisable() {
        stop();
    }

    @Override
    public void onFrameStart(FrameEvent event) {
        if (!active || mc.thePlayer == null) {
            return;
        }
        // Hand the camera rotation to the player for the duration of the frame so both the
        // mouse input and the camera use it.
        mc.thePlayer.rotationYaw = cameraYaw;
        mc.thePlayer.prevRotationYaw = cameraYaw;
        mc.thePlayer.rotationPitch = cameraPitch;
        mc.thePlayer.prevRotationPitch = cameraPitch;
    }

    @Override
    public void onFrameEnd(FrameEvent event) {
        if (!active || mc.thePlayer == null) {
            return;
        }
        // Whatever the mouse did this frame belongs to the camera, not to the body.
        cameraYaw = mc.thePlayer.rotationYaw;
        cameraPitch = MathUtil.clamp(mc.thePlayer.rotationPitch, -90.0F, 90.0F);
        restoreBody();
    }

    private void restoreBody() {
        if (mc.thePlayer == null) {
            return;
        }
        mc.thePlayer.rotationYaw = bodyYaw;
        mc.thePlayer.prevRotationYaw = bodyYaw;
        mc.thePlayer.rotationPitch = bodyPitch;
        mc.thePlayer.prevRotationPitch = bodyPitch;
    }
}
