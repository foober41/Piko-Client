package piko.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * In game overlay that routes chat through {@link PikoChatGui}.
 *
 * <p>Subclassing is used instead of reaching into Minecraft's private chat field: the
 * overridden {@code getChatGUI} means new messages are stored in the Piko chat instance,
 * so history and scrolling keep working.</p>
 */
public class PikoIngameGui extends GuiIngameForge {

    private final GuiNewChat chatGui;
    private float partialTicks;

    public PikoIngameGui(Minecraft minecraft, GuiNewChat chatGui) {
        super(minecraft);
        this.chatGui = chatGui;
    }

    @Override
    public GuiNewChat getChatGUI() {
        return chatGui;
    }

    @Override
    public void renderGameOverlay(float partialTicks) {
        this.partialTicks = partialTicks;
        super.renderGameOverlay(partialTicks);
    }

    @Override
    protected void renderChat(int width, int height) {
        mc.mcProfiler.startSection("chat");

        // Forge keeps its own parent event private, so an equivalent one is built here to
        // keep the Chat and Post events other mods listen for firing exactly as before.
        RenderGameOverlayEvent parent = new RenderGameOverlayEvent(partialTicks,
                piko.PikoClient.getInstance().getEventBridge().getResolution());
        RenderGameOverlayEvent.Chat event = new RenderGameOverlayEvent.Chat(parent, 0, height - 48);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            mc.mcProfiler.endSection();
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) event.posX, (float) event.posY, 0.0F);
        chatGui.drawChat(updateCounter);
        GlStateManager.popMatrix();

        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(parent,
                RenderGameOverlayEvent.ElementType.CHAT));
        mc.mcProfiler.endSection();
    }
}
