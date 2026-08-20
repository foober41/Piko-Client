package piko.module.visual;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;

/**
 * Shrinks the first person fire overlay so burning does not blind you mid fight.
 *
 * <p>The vanilla overlay is cancelled and drawn again lower on the screen. Fire damage,
 * duration and everything else about burning is unchanged.</p>
 */
public class LowFire extends Module {

    private final NumberSetting height;
    private final NumberSetting scale;
    private final BooleanSetting hideCompletely;

    public LowFire() {
        super("Low Fire", "Moves the fire overlay out of your view", ModuleCategory.VISUAL);
        height = settings.add(new NumberSetting("Fire Height", 0.55D, 0.0D, 1.0D, 0.05D));
        scale = settings.add(new NumberSetting("Fire Scale", 0.8D, 0.3D, 1.0D, 0.05D).suffix("x"));
        hideCompletely = settings.add(new BooleanSetting("Hide Completely", false));
        useForgeEvents();
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderBlockOverlayEvent event) {
        if (event.overlayType != RenderBlockOverlayEvent.OverlayType.FIRE) {
            return;
        }
        event.setCanceled(true);
        if (hideCompletely.get()) {
            return;
        }

        GlStateManager.pushMatrix();
        // Push the flames down and shrink them; the vanilla renderer draws around the origin.
        GlStateManager.translate(0.0F, -height.getFloat(), 0.0F);
        float factor = scale.getFloat();
        GlStateManager.scale(factor, factor, factor);
        mc.getItemRenderer().renderFireInFirstPerson(event.renderPartialTicks);
        GlStateManager.popMatrix();
    }
}
