package piko.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import piko.font.IFont;
import piko.module.visual.ChatCustomization;
import piko.render.ColorUtil;
import piko.render.RenderUtil;

/**
 * Chat renderer with the Piko options applied.
 *
 * <p>Falls back to the vanilla drawing routine whenever the chat module is disabled, so
 * the replacement can stay installed without changing anything the player did not ask
 * for. Message history lives in the vanilla superclass exactly as before.</p>
 */
public class PikoChatGui extends GuiNewChat {

    private static final float LINE_HEIGHT = 9.0F;

    private final ChatCustomization module;

    private int lastLineCount;
    private long lastLineTime;

    public PikoChatGui(Minecraft minecraft, ChatCustomization module) {
        super(minecraft);
        this.module = module;
    }

    @Override
    public void drawChat(int updateCounter) {
        if (module == null || !module.isEnabled()) {
            super.drawChat(updateCounter);
            return;
        }
        if (mc.gameSettings.chatVisibility == EntityPlayer.EnumChatVisibility.HIDDEN) {
            return;
        }

        int visibleLines = getLineCount();
        int total = drawnChatLines.size();
        if (total <= 0) {
            return;
        }
        if (total != lastLineCount) {
            lastLineCount = total;
            lastLineTime = System.currentTimeMillis();
        }

        boolean chatOpen = getChatOpen();
        float baseOpacity = mc.gameSettings.chatOpacity * 0.9F + 0.1F;
        float scale = getChatScale() * module.getScaleMultiplier();
        int width = MathHelper.ceiling_float_int(getChatWidth() / scale);
        IFont font = module.getFont();

        float entryProgress = module.isMessageAnimation()
                ? Math.min(1.0F, (System.currentTimeMillis() - lastLineTime) / 180.0F)
                : 1.0F;
        float slide = module.isSmoothChat() ? (1.0F - entryProgress) * LINE_HEIGHT : 0.0F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(2.0F, 20.0F + slide, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);

        int drawn = 0;
        for (int index = 0; index + scrollPos < drawnChatLines.size() && index < visibleLines; index++) {
            ChatLine line = drawnChatLines.get(index + scrollPos);
            if (line == null) {
                continue;
            }
            int age = updateCounter - line.getUpdatedCounter();
            if (age >= 200 && !chatOpen) {
                continue;
            }

            double fade = MathHelper.clamp_double((1.0D - age / 200.0D) * 10.0D, 0.0D, 1.0D);
            fade = fade * fade;
            int alpha = chatOpen ? 255 : (int) (255.0D * fade);
            alpha = (int) (alpha * baseOpacity);
            drawn++;
            if (alpha <= 3) {
                continue;
            }

            float lineY = -index * LINE_HEIGHT;
            float offsetX = 0.0F;
            float lineAlpha = alpha / 255.0F;
            if (index == 0 && module.isMessageAnimation()) {
                offsetX = (1.0F - entryProgress) * -14.0F;
                lineAlpha *= entryProgress;
            }

            if (!module.isTransparentBackground()) {
                int background = ColorUtil.alpha(module.getBackgroundColor(),
                        module.getBackgroundOpacity() * lineAlpha);
                RenderUtil.drawRect(offsetX, lineY - LINE_HEIGHT, width + 4.0F, LINE_HEIGHT, background);
            }

            String text = line.getChatComponent().getFormattedText();
            GlStateManager.enableBlend();
            int textColor = 0xFFFFFF | ((int) (lineAlpha * 255.0F) << 24);
            if (module.isShadow()) {
                font.drawStringWithShadow(text, offsetX, lineY - 8.0F, textColor);
            } else {
                font.drawString(text, offsetX, lineY - 8.0F, textColor);
            }
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        }

        if (chatOpen) {
            drawScrollBar(visibleLines, drawn, total);
        }
        GlStateManager.popMatrix();
        RenderUtil.resetState();
    }

    private void drawScrollBar(int visibleLines, int drawn, int total) {
        int fontHeight = mc.fontRendererObj.FONT_HEIGHT;
        GlStateManager.translate(-3.0F, 0.0F, 0.0F);
        int fullHeight = total * fontHeight + total;
        int shownHeight = drawn * fontHeight + drawn;
        int offset = scrollPos * shownHeight / total;
        int barHeight = shownHeight * shownHeight / fullHeight;
        if (fullHeight == shownHeight) {
            return;
        }
        int alpha = offset > 0 ? 170 : 96;
        int color = isScrolled ? 0xCC3333 : 0x333333;
        RenderUtil.drawRect(0, -offset - barHeight, 2.0F, barHeight, ColorUtil.alpha(color | 0xFF000000, alpha / 255.0F));
    }
}
