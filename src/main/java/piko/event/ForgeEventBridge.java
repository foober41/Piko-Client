package piko.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import piko.PikoClient;
import piko.event.events.AttackEvent;
import piko.event.events.ChatEvent;
import piko.event.events.FrameEvent;
import piko.event.events.KeyPressEvent;
import piko.event.events.MouseClickEvent;
import piko.event.events.OverlayEvent;
import piko.event.events.Render2DEvent;
import piko.event.events.Render3DEvent;
import piko.gui.PikoMainMenu;
import piko.gui.PikoMenu;
import piko.gui.hud.HudEditorScreen;
import piko.module.gui.MainMenuModule;

/**
 * Translates Forge and FML events into Piko events.
 *
 * <p>This is the only place in the client that talks to the Forge event system for the
 * shared hooks; modules that need a very specific vanilla event subscribe on their own and
 * only while they are enabled.</p>
 */
public class ForgeEventBridge {

    private final PikoClient client;
    private final Minecraft mc = Minecraft.getMinecraft();

    // Event objects are reused; these hooks run every frame and must not allocate.
    private final Render2DEvent render2DEvent = new Render2DEvent();
    private final Render3DEvent render3DEvent = new Render3DEvent();
    private final FrameEvent frameEvent = new FrameEvent();
    private final MouseClickEvent mouseClickEvent = new MouseClickEvent();
    private final KeyPressEvent keyPressEvent = new KeyPressEvent();
    private final AttackEvent attackEvent = new AttackEvent();
    private final OverlayEvent overlayEvent = new OverlayEvent();
    private final ChatEvent chatEvent = new ChatEvent();

    private ScaledResolution cachedResolution;
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    public ForgeEventBridge(PikoClient client) {
        this.client = client;
    }

    /**
     * Shared scaled resolution.
     *
     * <p>Creating a {@code ScaledResolution} allocates and recomputes the GUI scale, so the
     * instance is cached and only rebuilt when the window or the GUI scale changes.</p>
     */
    public ScaledResolution getResolution() {
        if (cachedResolution == null || cachedWidth != mc.displayWidth || cachedHeight != mc.displayHeight) {
            cachedResolution = new ScaledResolution(mc);
            cachedWidth = mc.displayWidth;
            cachedHeight = mc.displayHeight;
        }
        return cachedResolution;
    }

    public void invalidateResolution() {
        cachedResolution = null;
    }

    @SubscribeEvent
    public void onOverlayPre(RenderGameOverlayEvent.Pre event) {
        overlayEvent.set(event.type, event.partialTicks);
        client.getEventBus().postOverlay(overlayEvent);
        if (overlayEvent.isCancelled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onOverlayPost(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) {
            return;
        }
        render2DEvent.set(event.resolution, event.partialTicks);
        client.getEventBus().postRender2D(render2DEvent);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        render3DEvent.set(event.partialTicks);
        client.getEventBus().postRender3D(render3DEvent);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        frameEvent.set(event.renderTickTime);
        if (event.phase == TickEvent.Phase.START) {
            client.getEventBus().postFrameStart(frameEvent);
        } else {
            client.getEventBus().postFrameEnd(frameEvent);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        client.getEventBus().postTick(piko.event.events.TickEvent.INSTANCE);
        client.getConfigManager().saveIfNeeded();
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (event.button < 0) {
            return;
        }
        piko.util.ClickCounters.onMouseClick(event.button, event.buttonstate);
        mouseClickEvent.set(event.button, event.buttonstate);
        client.getEventBus().postMouseClick(mouseClickEvent);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE || !Keyboard.getEventKeyState()) {
            return;
        }

        if (client.getKeybindManager().getMenuKey().matches(key)) {
            mc.displayGuiScreen(new PikoMenu());
            return;
        }
        if (client.getKeybindManager().getHudEditorKey().matches(key)) {
            mc.displayGuiScreen(new HudEditorScreen(null));
            return;
        }

        client.getModuleManager().handleKeyPress(key);
        keyPressEvent.set(key);
        client.getEventBus().postKeyPress(keyPressEvent);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.entityPlayer != mc.thePlayer || event.target == null) {
            return;
        }
        attackEvent.set(event.target);
        client.getEventBus().postAttack(attackEvent);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.world != null && event.world.isRemote) {
            client.getEventBus().postWorldJoin();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && event.world.isRemote) {
            client.getEventBus().postWorldLeave();
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (event.message == null) {
            return;
        }
        chatEvent.set(event.message, event.type);
        client.getEventBus().postChat(chatEvent);
        event.message = chatEvent.getMessage();
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        MainMenuModule mainMenu = client.getModuleManager().getModule(MainMenuModule.class);
        if (mainMenu != null && mainMenu.isEnabled()
                && event.gui instanceof GuiMainMenu && !(event.gui instanceof PikoMainMenu)) {
            event.gui = new PikoMainMenu();
        }
    }
}
