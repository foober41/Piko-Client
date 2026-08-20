package piko.module.performance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;

/**
 * Distance limits for entity rendering.
 *
 * <p>Crowded fights and item spam are the two things that reliably tank frame rate in
 * 1.8.9. Players are always drawn to the configured player distance so nothing relevant to
 * combat disappears, while mobs and dropped items can be cut much shorter.</p>
 */
public class EntityRenderingModule extends Module {

    private final NumberSetting playerDistance;
    private final NumberSetting mobDistance;
    private final NumberSetting itemDistance;
    private final BooleanSetting hideItemsInFights;
    private final NumberSetting itemCrowdLimit;

    private int visibleItemCount;
    private long itemCountTick;

    public EntityRenderingModule() {
        super("Entity Rendering", "Limits how far entities are drawn", ModuleCategory.PERFORMANCE);
        playerDistance = settings.add(new NumberSetting("Player Distance", 96.0D, 16.0D, 128.0D, 4.0D).suffix("m"));
        mobDistance = settings.add(new NumberSetting("Mob Distance", 48.0D, 8.0D, 128.0D, 4.0D).suffix("m"));
        itemDistance = settings.add(new NumberSetting("Dropped Item Distance", 24.0D, 4.0D, 96.0D, 2.0D).suffix("m"));
        hideItemsInFights = settings.add(new BooleanSetting("Limit Dropped Items", true));
        itemCrowdLimit = settings.add(new NumberSetting("Max Dropped Items", 96.0D, 16.0D, 512.0D, 16.0D));
        useForgeEvents();
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre<EntityLivingBase> event) {
        EntityLivingBase entity = event.entity;
        if (entity == null || mc.getRenderViewEntity() == null || entity == mc.thePlayer) {
            return;
        }
        double limit = entity instanceof EntityPlayer ? playerDistance.get() : mobDistance.get();
        if (distanceSquared(entity) > limit * limit) {
            event.setCanceled(true);
        }
    }

    /** Called by the Piko dropped item renderer. */
    public boolean shouldCullItem(EntityItem item) {
        if (distanceSquared(item) > itemDistance.get() * itemDistance.get()) {
            return true;
        }
        if (!hideItemsInFights.get()) {
            return false;
        }
        // Counting once per tick keeps this out of the per item render path.
        long worldTime = mc.theWorld == null ? 0L : mc.theWorld.getTotalWorldTime();
        if (worldTime != itemCountTick) {
            itemCountTick = worldTime;
            visibleItemCount = 0;
            if (mc.theWorld != null) {
                for (Entity entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityItem) {
                        visibleItemCount++;
                    }
                }
            }
        }
        return visibleItemCount > itemCrowdLimit.getInt()
                && distanceSquared(item) > (itemDistance.get() * 0.5D) * (itemDistance.get() * 0.5D);
    }

    private double distanceSquared(Entity entity) {
        Entity viewer = mc.getRenderViewEntity();
        return viewer == null ? 0.0D : entity.getDistanceSqToEntity(viewer);
    }
}
