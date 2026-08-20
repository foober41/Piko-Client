package piko.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import piko.PikoClient;
import piko.module.performance.EntityRenderingModule;
import piko.module.visual.ItemPhysics;

/**
 * Dropped item renderer used for Item Physics and for the dropped item culling of the
 * performance module.
 *
 * <p>When both features are off it defers to the vanilla renderer, so the only cost is a
 * boolean check per item.</p>
 */
public class PikoItemEntityRenderer extends RenderEntityItem {

    private final RenderItem renderItem;

    public PikoItemEntityRenderer(RenderManager renderManager, RenderItem renderItem) {
        super(renderManager, renderItem);
        this.renderItem = renderItem;
    }

    private ItemPhysics physics() {
        return PikoClient.getInstance().getModuleManager().getModule(ItemPhysics.class);
    }

    private EntityRenderingModule entityRendering() {
        return PikoClient.getInstance().getModuleManager().getModule(EntityRenderingModule.class);
    }

    @Override
    public boolean shouldBob() {
        ItemPhysics module = physics();
        return module == null || !module.isEnabled() || module.isBobbing();
    }

    @Override
    public void doRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        EntityRenderingModule culling = entityRendering();
        if (culling != null && culling.isEnabled() && culling.shouldCullItem(entity)) {
            return;
        }

        ItemPhysics module = physics();
        if (module == null || !module.isEnabled()) {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
            return;
        }
        renderFlat(entity, x, y, z, partialTicks, module);
    }

    /** Lays the item on the ground at a stable angle instead of spinning it in the air. */
    private void renderFlat(EntityItem entity, double x, double y, double z, float partialTicks, ItemPhysics module) {
        ItemStack stack = entity.getEntityItem();
        if (stack == null || stack.getItem() == null) {
            return;
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();

        IBakedModel model = renderItem.getItemModelMesher().getItemModel(stack);
        boolean gui3d = model.isGui3d();

        // A deterministic angle per entity keeps a pile of drops from looking cloned while
        // never changing between frames.
        float yaw = (entity.getEntityId() * 47 % 360) + module.getRotationOffset();
        float lift = gui3d ? 0.06F : 0.02F;

        GlStateManager.translate((float) x, (float) y + lift, (float) z);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        float scale = module.getScale();
        GlStateManager.scale(scale, scale, scale);
        if (gui3d) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
        }

        int layers = module.isStackDepth() ? countLayers(stack) : 1;
        for (int i = 0; i < layers; i++) {
            GlStateManager.pushMatrix();
            if (i > 0) {
                GlStateManager.translate(0.0F, 0.0F, -0.03F * i);
            }
            IBakedModel transformed = net.minecraftforge.client.ForgeHooksClient
                    .handleCameraTransforms(model, ItemCameraTransforms.TransformType.GROUND);
            renderItem.renderItem(stack, transformed);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(getEntityTexture(entity));
    }

    private int countLayers(ItemStack stack) {
        if (stack.stackSize > 48) {
            return 4;
        }
        if (stack.stackSize > 32) {
            return 3;
        }
        if (stack.stackSize > 1) {
            return 2;
        }
        return 1;
    }
}
