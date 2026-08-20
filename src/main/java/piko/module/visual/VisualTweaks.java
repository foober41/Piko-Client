package piko.module.visual;

import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import piko.event.events.FrameEvent;
import piko.event.events.OverlayEvent;
import piko.event.events.TickEvent;
import piko.event.listener.FrameListener;
import piko.event.listener.OverlayListener;
import piko.event.listener.TickListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;

/**
 * The small rendering annoyances a PvP player usually wants gone.
 *
 * <p>Each option removes or reduces something the client draws. None of them change block
 * data, movement or anything the server evaluates.</p>
 */
public class VisualTweaks extends Module implements OverlayListener, FrameListener, TickListener {

    private final BooleanSetting clearWater;
    private final BooleanSetting noPumpkinBlur;
    private final BooleanSetting noBossBar;
    private final BooleanSetting noHurtCam;
    private final BooleanSetting noWeather;
    private final BooleanSetting fastGraphics;
    private final BooleanSetting noPortalOverlay;

    private int suppressedHurtTime = -1;
    private int suppressedMaxHurtTime = -1;

    public VisualTweaks() {
        super("PvP Visuals", "Clear water, no boss bar, no hurt cam and friends", ModuleCategory.VISUAL);
        clearWater = settings.add(new BooleanSetting("Clear Water", true));
        noPumpkinBlur = settings.add(new BooleanSetting("No Pumpkin Blur", true));
        noBossBar = settings.add(new BooleanSetting("No Boss Bar", false));
        noHurtCam = settings.add(new BooleanSetting("No Hurt Cam", true));
        noWeather = settings.add(new BooleanSetting("No Weather", false));
        fastGraphics = settings.add(new BooleanSetting("Better Foliage Off", false));
        noPortalOverlay = settings.add(new BooleanSetting("No Portal Overlay", false));
        useForgeEvents();
    }

    @Override
    public void onRenderOverlay(OverlayEvent event) {
        RenderGameOverlayEvent.ElementType type = event.getType();
        if (noPumpkinBlur.get() && type == RenderGameOverlayEvent.ElementType.HELMET) {
            event.cancel();
        } else if (noBossBar.get() && type == RenderGameOverlayEvent.ElementType.BOSSHEALTH) {
            event.cancel();
        } else if (noPortalOverlay.get() && type == RenderGameOverlayEvent.ElementType.PORTAL) {
            event.cancel();
        }
    }

    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        if (clearWater.get() && event.overlayType == RenderBlockOverlayEvent.OverlayType.WATER) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (!clearWater.get() || mc.thePlayer == null || !mc.thePlayer.isInsideOfMaterial(net.minecraft.block.material.Material.water)) {
            return;
        }
        // Cancelling the event is what tells Forge to use the density we set.
        event.density = 0.02F;
        event.setCanceled(true);
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.theWorld != null && noWeather.get()) {
            // Client side only: the server keeps its own weather, this just stops drawing it.
            mc.theWorld.setRainStrength(0.0F);
        }
        if (fastGraphics.get() && mc.gameSettings.fancyGraphics) {
            mc.gameSettings.fancyGraphics = false;
        }
    }

    @Override
    public void onFrameStart(FrameEvent event) {
        if (!noHurtCam.get() || mc.thePlayer == null) {
            return;
        }
        // The camera shake reads hurtTime while the frame is drawn, so it is hidden for
        // exactly that window and restored before the next tick uses it.
        suppressedHurtTime = mc.thePlayer.hurtTime;
        suppressedMaxHurtTime = mc.thePlayer.maxHurtTime;
        mc.thePlayer.hurtTime = 0;
        mc.thePlayer.maxHurtTime = 0;
    }

    @Override
    public void onFrameEnd(FrameEvent event) {
        if (suppressedHurtTime < 0 || mc.thePlayer == null) {
            return;
        }
        mc.thePlayer.hurtTime = suppressedHurtTime;
        mc.thePlayer.maxHurtTime = suppressedMaxHurtTime;
        suppressedHurtTime = -1;
        suppressedMaxHurtTime = -1;
    }

    @Override
    protected void onDisable() {
        if (suppressedHurtTime >= 0 && mc.thePlayer != null) {
            mc.thePlayer.hurtTime = suppressedHurtTime;
            mc.thePlayer.maxHurtTime = suppressedMaxHurtTime;
        }
        suppressedHurtTime = -1;
    }
}
