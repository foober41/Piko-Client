package piko;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import piko.config.ConfigManager;
import piko.config.KeybindManager;
import piko.event.EventBus;
import piko.event.ForgeEventBridge;
import piko.module.ModuleManager;
import piko.module.ModuleRegistry;
import piko.profile.ProfileManager;
import piko.render.HandRenderer;
import piko.render.PikoItemEntityRenderer;

/**
 * Entry point of Piko Client, a legitimate Minecraft 1.8.9 PvP client.
 *
 * <p>Piko is deliberately limited to rendering, HUD information and client side
 * convenience. It contains no combat automation, no reach or movement changes and no
 * packet manipulation of any kind.</p>
 */
@Mod(modid = PikoClient.MOD_ID,
        name = PikoClient.NAME,
        version = PikoClient.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.9]",
        guiFactory = "piko.PikoGuiFactory")
public class PikoClient {

    public static final String MOD_ID = "piko";
    public static final String NAME = "Piko Client";
    public static final String VERSION = "@PIKO_VERSION@";
    public static final String MINECRAFT_VERSION = "1.8.9";

    private static PikoClient instance;

    private EventBus eventBus;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private ProfileManager profileManager;
    private KeybindManager keybindManager;
    private ForgeEventBridge eventBridge;
    private HandRenderer handRenderer;

    public static PikoClient getInstance() {
        return instance;
    }

    public static String getFullVersion() {
        return NAME + " " + VERSION;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;

        eventBus = new EventBus();
        moduleManager = new ModuleManager();
        keybindManager = new KeybindManager();

        configManager = new ConfigManager();
        profileManager = new ProfileManager(configManager.getProfilesDirectory());

        ModuleRegistry.registerAll(moduleManager);
        configManager.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        eventBridge = new ForgeEventBridge(this);
        MinecraftForge.EVENT_BUS.register(eventBridge);
        FMLCommonHandler.instance().bus().register(eventBridge);

        handRenderer = new HandRenderer();
        MinecraftForge.EVENT_BUS.register(handRenderer);

        // The dropped item renderer must be registered before the render manager is built;
        // it falls through to vanilla behaviour whenever the related modules are off.
        RenderingRegistry.registerEntityRenderingHandler(EntityItem.class, new IRenderFactory<EntityItem>() {
            @Override
            public Render<? super EntityItem> createRenderFor(RenderManager manager) {
                return new PikoItemEntityRenderer(manager, Minecraft.getMinecraft().getRenderItem());
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread("Piko config save") {
            @Override
            public void run() {
                try {
                    configManager.save();
                } catch (Throwable throwable) {
                    System.err.println("[Piko] Could not save the configuration on shutdown: " + throwable);
                }
            }
        });
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        profileManager.createStarterProfiles();
        moduleManager.postInit();
        moduleManager.syncListeners();
        configManager.save();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    public ForgeEventBridge getEventBridge() {
        return eventBridge;
    }
}
