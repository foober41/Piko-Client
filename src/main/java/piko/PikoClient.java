package piko;

import net.minecraftforge.common.MinecraftForge;
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
