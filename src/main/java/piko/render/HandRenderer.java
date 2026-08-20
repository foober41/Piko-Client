package piko.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemMap;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import piko.PikoClient;
import piko.module.pvp.BlockHitAnimation;
import piko.module.visual.OldAnimations;

/**
 * Piko's own first person hand pipeline.
 *
 * <p>Forge only offers a single cancellable hook around the whole hand pass, so replacing
 * an animation means taking over that pass and reproducing what vanilla does with the
 * pieces the player asked to change. Every transform below is cosmetic; attack rate, reach,
 * damage and the packets sent to the server are never touched.</p>
 *
 * <p>The renderer stays subscribed for the lifetime of the game but returns immediately
 * when neither animation module is enabled, in which case vanilla renders the hand.</p>
 */
public class HandRenderer {

    /** Vanilla swing animation length in milliseconds (six ticks). */
    private static final float BASE_SWING_MILLIS = 300.0F;

    private final Minecraft mc = Minecraft.getMinecraft();

    private long swingStart;
    private boolean wasSwinging;
    private int previousSwingTick;

    private BlockHitAnimation blockHit;
    private OldAnimations oldAnimations;

    private BlockHitAnimation blockHit() {
        if (blockHit == null) {
            blockHit = PikoClient.getInstance().getModuleManager().getModule(BlockHitAnimation.class);
        }
        return blockHit;
    }

    private OldAnimations oldAnimations() {
        if (oldAnimations == null) {
            oldAnimations = PikoClient.getInstance().getModuleManager().getModule(OldAnimations.class);
        }
        return oldAnimations;
    }

    private boolean active() {
        BlockHitAnimation hit = blockHit();
        OldAnimations old = oldAnimations();
        return (hit != null && hit.isEnabled()) || (old != null && old.isEnabled());
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (!active() || mc.thePlayer == null) {
            return;
        }
        event.setCanceled(true);
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        renderHand(event.partialTicks, event.renderPass);
    }

    /** Mirrors {@code EntityRenderer.renderHand} so cancelling the vanilla pass loses nothing. */
    private void renderHand(float partialTicks, int renderPass) {
        if (mc.entityRenderer.debugView) {
            return;
        }
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();

        if (mc.gameSettings.anaglyph) {
            GlStateManager.translate(-(renderPass * 2 - 1) * 0.07F, 0.0F, 0.0F);
        }
        Project.gluPerspective(mc.entityRenderer.getFOVModifier(partialTicks, false),
                (float) mc.displayWidth / (float) mc.displayHeight, 0.05F,
                mc.entityRenderer.farPlaneDistance * 2.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        if (mc.gameSettings.anaglyph) {
            GlStateManager.translate((renderPass * 2 - 1) * 0.1F, 0.0F, 0.0F);
        }

        GlStateManager.pushMatrix();
        mc.entityRenderer.hurtCameraEffect(partialTicks);
        if (mc.gameSettings.viewBobbing) {
            mc.entityRenderer.setupViewBobbing(partialTicks);
        }

        boolean sleeping = mc.getRenderViewEntity() instanceof EntityLivingBase
                && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();
        if (mc.gameSettings.thirdPersonView == 0 && !sleeping
                && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator()) {
            mc.entityRenderer.enableLightmap();
            renderItemInFirstPerson(partialTicks);
            mc.entityRenderer.disableLightmap();
        }
        GlStateManager.popMatrix();

        if (mc.gameSettings.thirdPersonView == 0 && !sleeping) {
            mc.getItemRenderer().renderOverlays(partialTicks);
            mc.entityRenderer.hurtCameraEffect(partialTicks);
        }
        if (mc.gameSettings.viewBobbing) {
            mc.entityRenderer.setupViewBobbing(partialTicks);
        }
    }

    /**
     * Swing progress driven by Piko's own clock so the length can be adjusted without
     * touching how often the player is allowed to attack.
     */
    private float swingProgress(AbstractClientPlayer player, float partialTicks) {
        BlockHitAnimation hit = blockHit();
        float speed = hit == null ? 1.0F : hit.getSwingSpeed();
        OldAnimations old = oldAnimations();
        boolean customTiming = speed != 1.0F || (old != null && old.isEnabled() && old.isOldSwing());

        boolean swinging = player.isSwingInProgress;
        if (swinging && (!wasSwinging || player.swingProgressInt < previousSwingTick)) {
            swingStart = System.currentTimeMillis();
        }
        wasSwinging = swinging;
        previousSwingTick = player.swingProgressInt;

        if (!customTiming) {
            return player.getSwingProgress(partialTicks);
        }
        if (!swinging) {
            return 0.0F;
        }
        float duration = BASE_SWING_MILLIS / Math.max(0.1F, speed);
        float elapsed = System.currentTimeMillis() - swingStart;
        return MathHelper.clamp_float(elapsed / duration, 0.0F, 1.0F);
    }

    private void renderItemInFirstPerson(float partialTicks) {
        ItemRenderer itemRenderer = mc.getItemRenderer();
        AbstractClientPlayer player = mc.thePlayer;

        float equipProgress = 1.0F - (itemRenderer.prevEquippedProgress
                + (itemRenderer.equippedProgress - itemRenderer.prevEquippedProgress) * partialTicks);
        float swing = swingProgress(player, partialTicks);
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;

        itemRenderer.rotateArroundXAndY(pitch, yaw);
        itemRenderer.setLightMapFromPlayer(player);
        itemRenderer.rotateWithPlayerRotations((EntityPlayerSP) player, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();

        if (itemRenderer.itemToRender != null) {
            OldAnimations old = oldAnimations();
            boolean oldEnabled = old != null && old.isEnabled();

            if (itemRenderer.itemToRender.getItem() instanceof ItemMap) {
                itemRenderer.renderItemMap(player, pitch, equipProgress, swing);
            } else if (player.getItemInUseCount() > 0) {
                EnumAction action = itemRenderer.itemToRender.getItemUseAction();
                switch (action) {
                    case NONE:
                        transformItem(equipProgress, 0.0F);
                        break;
                    case EAT:
                    case DRINK:
                        itemRenderer.performDrinking(player, partialTicks);
                        // 1.7 kept swinging the arm while eating and drinking.
                        transformItem(equipProgress, oldEnabled && old.isOldEating() ? swing : 0.0F);
                        break;
                    case BLOCK:
                        transformItem(equipProgress, useBlockSwing(oldEnabled, old) ? swing : 0.0F);
                        applyBlockTransform(swing);
                        break;
                    case BOW:
                        transformItem(equipProgress, oldEnabled && old.isOldBow() ? swing : 0.0F);
                        itemRenderer.doBowTransformations(partialTicks, player);
                        break;
                    default:
                        transformItem(equipProgress, 0.0F);
                        break;
                }
            } else {
                itemRenderer.doItemUsedTransformations(swing);
                transformItem(equipProgress, swing);
            }

            applyItemOffsets(oldEnabled, old);
            itemRenderer.renderItem(player, itemRenderer.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
        } else if (!player.isInvisible()) {
            itemRenderer.renderPlayerArm(player, equipProgress, swing);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    private boolean useBlockSwing(boolean oldEnabled, OldAnimations old) {
        if (oldEnabled && old.isOldBlockHit()) {
            return true;
        }
        BlockHitAnimation hit = blockHit();
        return hit != null && hit.isEnabled() && hit.swingsWhileBlocking();
    }

    /** The vanilla 1.8.9 first person transform, kept identical so nothing drifts. */
    private void transformItem(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float swingSquared = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float swingRoot = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(swingSquared * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(swingRoot * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(swingRoot * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    /** Block pose, either the vanilla one or one of the Piko styles. */
    private void applyBlockTransform(float swing) {
        BlockHitAnimation hit = blockHit();
        String style = hit == null || !hit.isEnabled() ? "Default" : hit.getStyle();

        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);

        float wave = MathHelper.sin(MathHelper.sqrt_float(swing) * (float) Math.PI);
        if (style.equals("Slide")) {
            GlStateManager.translate(wave * -0.25F, 0.0F, 0.0F);
        } else if (style.equals("Swing")) {
            GlStateManager.rotate(wave * -25.0F, 0.0F, 0.0F, 1.0F);
        } else if (style.equals("Push")) {
            GlStateManager.translate(0.0F, 0.0F, wave * -0.3F);
        } else if (style.equals("Piko")) {
            GlStateManager.translate(wave * -0.08F, wave * 0.05F, wave * -0.12F);
            GlStateManager.rotate(wave * -12.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(1.0F + wave * 0.04F, 1.0F + wave * 0.04F, 1.0F);
        }
    }

    /** Player tuned offsets plus the optional 1.7 style holding and rod positions. */
    private void applyItemOffsets(boolean oldEnabled, OldAnimations old) {
        BlockHitAnimation hit = blockHit();
        if (hit != null && hit.isEnabled()) {
            GlStateManager.translate(hit.getOffsetX(), hit.getOffsetY(), hit.getOffsetZ());
            float scale = hit.getItemScale();
            GlStateManager.scale(scale, scale, scale);
        }
        if (!oldEnabled) {
            return;
        }
        if (old.isOldItemHolding()) {
            // Nudges the item back towards the lower, closer 1.7 resting pose.
            GlStateManager.translate(0.02F, 0.045F, 0.03F);
            GlStateManager.rotate(2.0F, 0.0F, 0.0F, 1.0F);
        }
        if (old.isOldRod() && mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem() instanceof ItemFishingRod) {
            GlStateManager.translate(0.08F, 0.02F, -0.06F);
            GlStateManager.rotate(-8.0F, 0.0F, 0.0F, 1.0F);
        }
        if (old.isOldBow() && mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
            GlStateManager.translate(0.0F, 0.02F, -0.04F);
        }
    }
}
