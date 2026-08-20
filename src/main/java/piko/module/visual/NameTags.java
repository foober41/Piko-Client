package piko.module.visual;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.ColorSetting;
import piko.setting.NumberSetting;

/**
 * Custom name plates above players.
 *
 * <p>Replaces the vanilla label with one Piko draws itself so the scale, background and
 * colour can be configured. Only entities that vanilla would already label are drawn, so
 * nothing hidden becomes visible.</p>
 */
public class NameTags extends Module {

    private final NumberSetting scale;
    private final NumberSetting backgroundOpacity;
    private final ColorSetting textColor;
    private final BooleanSetting playersOnly;
    private final BooleanSetting showHealth;
    private final NumberSetting maxDistance;

    public NameTags() {
        super("Name Tags", "Scale and style of player name plates", ModuleCategory.VISUAL);
        scale = settings.add(new NumberSetting("Scale", 1.0D, 0.5D, 2.5D, 0.05D).suffix("x"));
        backgroundOpacity = settings.add(new NumberSetting("Background Opacity", 0.25D, 0.0D, 1.0D, 0.05D));
        textColor = settings.add(new ColorSetting("Text Color", 0xFFFFFFFF));
        playersOnly = settings.add(new BooleanSetting("Players Only", true));
        showHealth = settings.add(new BooleanSetting("Show Health", false));
        maxDistance = settings.add(new NumberSetting("Max Distance", 48.0D, 8.0D, 96.0D, 1.0D).suffix("m"));
        useForgeEvents();
    }

    @SubscribeEvent
    public void onRenderSpecials(RenderLivingEvent.Specials.Pre<EntityLivingBase> event) {
        EntityLivingBase entity = event.entity;
        if (entity == null || mc.thePlayer == null) {
            return;
        }
        if (playersOnly.get() && !(entity instanceof EntityPlayer)) {
            return;
        }
        if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) {
            return;
        }
        if (!entity.hasCustomName() && !(entity instanceof EntityPlayer)) {
            return;
        }
        double distanceSquared = entity.getDistanceSqToEntity(mc.getRenderViewEntity());
        if (distanceSquared > maxDistance.get() * maxDistance.get()) {
            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);
        drawLabel(entity, event.x, event.y, event.z);
    }

    private void drawLabel(EntityLivingBase entity, double x, double y, double z) {
        String text = entity.getDisplayName().getFormattedText();
        if (showHealth.get()) {
            text = text + " \u00A7c" + (int) Math.ceil(entity.getHealth());
        }

        FontRenderer font = mc.fontRendererObj;
        float baseScale = 0.016666668F * 1.6F * scale.getFloat();

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + entity.height + 0.5F, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-baseScale, -baseScale, baseScale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int halfWidth = font.getStringWidth(text) / 2;
        int alpha = (int) (backgroundOpacity.getFloat() * 255.0F);
        if (alpha > 0) {
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(-halfWidth - 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, alpha / 255.0F).endVertex();
            worldRenderer.pos(-halfWidth - 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, alpha / 255.0F).endVertex();
            worldRenderer.pos(halfWidth + 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, alpha / 255.0F).endVertex();
            worldRenderer.pos(halfWidth + 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, alpha / 255.0F).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        // Vanilla draws the label twice: once through walls, once solid on top.
        font.drawString(text, -halfWidth, 0, (textColor.get() & 0xFFFFFF) | 0x20000000);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        font.drawString(text, -halfWidth, 0, textColor.get());

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
