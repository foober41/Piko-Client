package piko.config;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import piko.PikoClient;
import piko.module.HudModule;
import piko.module.Module;
import piko.profile.ProfileManager;
import piko.setting.SettingManager;
import piko.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * Reads and writes everything under {@code .minecraft/piko/}.
 *
 * <p>Layout:</p>
 * <pre>
 * piko/config.json      client level state such as the active profile
 * piko/modules.json     enabled flags and settings
 * piko/hud.json         HUD element positions
 * piko/keybinds.json    client hotkeys and module keybinds
 * piko/profiles/        one json per saved profile
 * </pre>
 *
 * <p>Nothing is ever written from a render or tick hot path. A change only raises a dirty
 * flag; the actual file write happens at most once every few seconds, when a screen closes
 * or when the game shuts down.</p>
 */
public class ConfigManager {

    private static final String CONFIG_VERSION = "1.0";
    private static final long SAVE_INTERVAL_MILLIS = 4000L;

    private final File rootDirectory;
    private final File configFile;
    private final File modulesFile;
    private final File hudFile;
    private final File keybindsFile;
    private final File profilesDirectory;
    private final File exportsDirectory;

    private long lastSave = System.currentTimeMillis();

    public ConfigManager() {
        rootDirectory = new File(Minecraft.getMinecraft().mcDataDir, "piko");
        configFile = new File(rootDirectory, "config.json");
        modulesFile = new File(rootDirectory, "modules.json");
        hudFile = new File(rootDirectory, "hud.json");
        keybindsFile = new File(rootDirectory, "keybinds.json");
        profilesDirectory = new File(rootDirectory, "profiles");
        exportsDirectory = new File(rootDirectory, "exports");
        FileUtil.ensureDirectory(rootDirectory);
        FileUtil.ensureDirectory(profilesDirectory);
        FileUtil.ensureDirectory(exportsDirectory);
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public File getProfilesDirectory() {
        return profilesDirectory;
    }

    public File getExportsDirectory() {
        return exportsDirectory;
    }

    public void load() {
        SettingManager.setLoading(true);
        try {
            JsonObject config = FileUtil.readJson(configFile);
            ProfileManager profiles = PikoClient.getInstance().getProfileManager();
            if (config != null && config.has("active_profile")) {
                profiles.setActiveProfileName(config.get("active_profile").getAsString());
            }

            applyModules(FileUtil.readJson(modulesFile));
            applyHud(FileUtil.readJson(hudFile));
            PikoClient.getInstance().getKeybindManager().deserialize(FileUtil.readJson(keybindsFile));
        } finally {
            SettingManager.setLoading(false);
            SettingManager.clearDirty();
        }
        PikoClient.getInstance().getModuleManager().syncListeners();
    }

    /** Applies a module state document, shared by the config loader and the profile loader. */
    public void applyModules(JsonObject root) {
        if (root == null) {
            return;
        }
        List<Module> modules = PikoClient.getInstance().getModuleManager().getModules();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            if (!root.has(module.getId()) || !root.get(module.getId()).isJsonObject()) {
                continue;
            }
            JsonObject stored = root.getAsJsonObject(module.getId());
            if (stored.has("enabled")) {
                module.setEnabledQuietly(stored.get("enabled").getAsBoolean());
            }
            if (stored.has("settings") && stored.get("settings").isJsonObject()) {
                module.getSettingManager().deserialize(stored.getAsJsonObject("settings"));
            }
        }
    }

    public void applyHud(JsonObject root) {
        if (root == null) {
            return;
        }
        List<HudModule> hudModules = PikoClient.getInstance().getModuleManager().getHudModules();
        for (int i = 0; i < hudModules.size(); i++) {
            HudModule hud = hudModules.get(i);
            if (!root.has(hud.getId()) || !root.get(hud.getId()).isJsonObject()) {
                continue;
            }
            JsonObject stored = root.getAsJsonObject(hud.getId());
            float x = stored.has("x") ? stored.get("x").getAsFloat() : hud.getRelativeX();
            float y = stored.has("y") ? stored.get("y").getAsFloat() : hud.getRelativeY();
            hud.setRelativePosition(x, y);
        }
    }

    public JsonObject serializeModules() {
        JsonObject root = new JsonObject();
        List<Module> modules = PikoClient.getInstance().getModuleManager().getModules();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.isEnabled());
            entry.add("settings", module.getSettingManager().serialize(false));
            root.add(module.getId(), entry);
        }
        return root;
    }

    public JsonObject serializeHud() {
        JsonObject root = new JsonObject();
        List<HudModule> hudModules = PikoClient.getInstance().getModuleManager().getHudModules();
        for (int i = 0; i < hudModules.size(); i++) {
            HudModule hud = hudModules.get(i);
            JsonObject entry = new JsonObject();
            entry.addProperty("x", hud.getRelativeX());
            entry.addProperty("y", hud.getRelativeY());
            root.add(hud.getId(), entry);
        }
        return root;
    }

    /** Writes every file immediately. */
    public void save() {
        JsonObject config = new JsonObject();
        config.addProperty("version", CONFIG_VERSION);
        config.addProperty("active_profile", PikoClient.getInstance().getProfileManager().getActiveProfileName());
        FileUtil.writeJson(configFile, config);

        FileUtil.writeJson(modulesFile, serializeModules());
        FileUtil.writeJson(hudFile, serializeHud());
        FileUtil.writeJson(keybindsFile, PikoClient.getInstance().getKeybindManager().serialize());

        PikoClient.getInstance().getProfileManager().saveActiveProfile();

        lastSave = System.currentTimeMillis();
        SettingManager.clearDirty();
    }

    /** Saves only when something changed and the write interval has elapsed. */
    public void saveIfNeeded() {
        if (SettingManager.isDirty() && System.currentTimeMillis() - lastSave >= SAVE_INTERVAL_MILLIS) {
            save();
        }
    }

    /** Called when a Piko screen closes so edits are on disk right away. */
    public void saveOnScreenClose() {
        if (SettingManager.isDirty()) {
            save();
        }
    }
}
