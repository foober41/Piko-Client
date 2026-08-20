package piko.module.visual;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import piko.PikoClient;
import piko.event.events.FrameEvent;
import piko.event.listener.FrameListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.module.performance.FpsBoost;
import piko.setting.NumberSetting;

/**
 * Frame blending motion blur.
 *
 * <p>The previous frame is kept in a small framebuffer and blended over the current one.
 * That is two textured quads per frame, which is far cheaper than an accumulation buffer
 * and works on any driver that already runs Minecraft's own framebuffer path.</p>
 *
 * <p>Maximum FPS mode switches the effect off automatically.</p>
 */
public class MotionBlur extends Module implements FrameListener {

    private final NumberSetting strength;

    private Framebuffer previousFrame;
    private int bufferWidth;
    private int bufferHeight;
    private boolean unsupported;

    public MotionBlur() {
        super("Motion Blur", "Blends the previous frame into the current one", ModuleCategory.VISUAL);
        strength = settings.add(new NumberSetting("Blur Strength", 0.45D, 0.05D, 0.85D, 0.05D));
    }

    private boolean shouldRun() {
        if (unsupported || !OpenGlHelper.isFramebufferEnabled() || mc.getFramebuffer() == null) {
            return false;
        }
        FpsBoost boost = PikoClient.getInstance().getModuleManager().getModule(FpsBoost.class);
        return boost == null || !boost.isMaximumMode();
    }

    @Override
    public void onFrameStart(FrameEvent event) {
    }

    @Override
    public void onFrameEnd(FrameEvent event) {
        if (!shouldRun()) {
            return;
        }
        try {
            blend();
        } catch (Throwable throwable) {
            unsupported = true;
            System.err.println("[Piko] Motion blur disabled, the driver rejected it: " + throwable);
        }
    }

    private void blend() {
        int width = mc.displayWidth;
        int height = mc.displayHeight;
        if (width <= 0 || height <= 0) {
            return;
        }
        if (previousFrame == null || bufferWidth != width || bufferHeight != height) {
            if (previousFrame != null) {
                previousFrame.deleteFramebuffer();
            }
            previousFrame = new Framebuffer(width, height, false);
            previousFrame.setFramebufferFilter(GL11.GL_LINEAR);
            bufferWidth = width;
            bufferHeight = height;
            copyCurrentFrame();
            return;
        }

        beginOverlay();
        previousFrame.bindFramebufferTexture();
        GlStateManager.color(1.0F, 1.0F, 1.0F, strength.getFloat());
        drawFullscreenQuad();
        endOverlay();

        copyCurrentFrame();
    }

    /** Stores the frame that is currently on screen for the next blend. */
    private void copyCurrentFrame() {
        previousFrame.bindFramebuffer(true);
        GlStateManager.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        beginOverlay();
        mc.getFramebuffer().bindFramebufferTexture();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawFullscreenQuad();
        endOverlay();

        mc.getFramebuffer().bindFramebuffer(true);
    }

    private void beginOverlay() {
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, 1.0D, 1.0D, 0.0D, -1.0D, 1.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    private void endOverlay() {
        GlStateManager.bindTexture(0);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
    }

    /** Framebuffer textures are stored bottom up, hence the flipped v coordinates. */
    private void drawFullscreenQuad() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(0.0D, 1.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        worldRenderer.pos(1.0D, 1.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldRenderer.pos(1.0D, 0.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldRenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        tessellator.draw();
    }

    @Override
    protected void onDisable() {
        if (previousFrame != null) {
            previousFrame.deleteFramebuffer();
            previousFrame = null;
        }
    }
}
