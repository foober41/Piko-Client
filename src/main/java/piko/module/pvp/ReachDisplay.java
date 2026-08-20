package piko.module.pvp;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import piko.event.events.AttackEvent;
import piko.event.listener.AttackListener;
import piko.module.HudModule;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;

/**
 * Reports how far away the target was when a hit landed, for example {@code 2.84 blocks}.
 *
 * <p>The value is measured after the fact from the player's eye position and the target's
 * hit box. Piko never changes Minecraft's attack range: the number simply describes the
 * hit that vanilla already allowed.</p>
 */
public class ReachDisplay extends HudModule implements AttackListener {

    private final BooleanSetting showUnit;
    private final NumberSetting holdTime;
    private final BooleanSetting hideWhenIdle;

    private double lastReach;
    private long lastHit;

    public ReachDisplay() {
        super("Reach Display", "Distance of your last landed hit", ModuleCategory.PVP, 0.5F, 0.34F, false);
        showUnit = settings.add(new BooleanSetting("Show Unit", true));
        holdTime = settings.add(new NumberSetting("Hold Time", 3.0D, 0.5D, 10.0D, 0.5D).suffix("s"));
        hideWhenIdle = settings.add(new BooleanSetting("Hide When Idle", true));
        enableBackground(true);
        enableTextColor(0xFFFFFFFF);
        enableFont();
    }

    @Override
    public void onAttack(AttackEvent event) {
        Entity target = event.getTarget();
        if (target == null || mc.thePlayer == null) {
            return;
        }
        lastReach = measure(target);
        lastHit = System.currentTimeMillis();
    }

    /**
     * Distance from the eye position to the point of the target's hit box that the look
     * vector crosses, falling back to the distance to the closest point of the box.
     */
    private double measure(Entity target) {
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 look = mc.thePlayer.getLook(1.0F);
        Vec3 end = eyes.addVector(look.xCoord * 6.0D, look.yCoord * 6.0D, look.zCoord * 6.0D);

        float border = target.getCollisionBorderSize();
        AxisAlignedBB box = target.getEntityBoundingBox().expand(border, border, border);
        MovingObjectPosition hit = box.calculateIntercept(eyes, end);
        if (hit != null && hit.hitVec != null) {
            return eyes.distanceTo(hit.hitVec);
        }

        double clampedX = Math.max(box.minX, Math.min(eyes.xCoord, box.maxX));
        double clampedY = Math.max(box.minY, Math.min(eyes.yCoord, box.maxY));
        double clampedZ = Math.max(box.minZ, Math.min(eyes.zCoord, box.maxZ));
        return eyes.distanceTo(new Vec3(clampedX, clampedY, clampedZ));
    }

    private boolean isFresh() {
        return System.currentTimeMillis() - lastHit <= (long) (holdTime.get() * 1000.0D);
    }

    private String text(boolean editing) {
        double value = editing ? 2.84D : lastReach;
        String formatted = String.format("%.2f", value);
        return showUnit.get() ? formatted + " blocks" : formatted;
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
        if (!editing && hideWhenIdle.get() && !isFresh()) {
            return;
        }
        String text = text(editing);
        drawBackground(textWidth(text), textHeight());
        font().drawStringWithShadow(text, 0, 0, getTextColor());
    }
}
