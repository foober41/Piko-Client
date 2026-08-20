package piko.profile;

import com.google.gson.JsonObject;
import piko.PikoClient;
import piko.config.ConfigManager;
import piko.module.ModuleManager;
import piko.setting.SettingManager;
import piko.util.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Named configuration snapshots stored in {@code .minecraft/piko/profiles/}.
 *
 * <p>A profile file contains the full module state, HUD layout and keybinds, so switching
 * from a Bedwars layout to a Practice layout swaps the entire client setup at once.</p>
 */
public class ProfileManager {

    public static final String DEFAULT_PROFILE = "Default";

    /** Profiles created on first launch so the list is useful straight away. */
    private static final String[] STARTER_PROFILES = {"Default", "Bedwars", "SkyWars", "Boxing", "Practice", "Hypixel"};

    private final File directory;
    private String activeProfile = DEFAULT_PROFILE;

    public ProfileManager(File profilesDirectory) {
        this.directory = profilesDirectory;
        FileUtil.ensureDirectory(directory);
    }

    public String getActiveProfileName() {
        return activeProfile;
    }

    public void setActiveProfileName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            activeProfile = name.trim();
        }
    }

    public File fileFor(String name) {
        return new File(directory, FileUtil.sanitize(name) + ".json");
    }

    public List<String> listProfiles() {
        List<String> names = new ArrayList<String>();
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".json");
            }
        });
        if (files != null) {
            for (File file : files) {
                JsonObject stored = FileUtil.readJson(file);
                if (stored != null && stored.has("name")) {
                    names.add(stored.get("name").getAsString());
                } else {
                    names.add(file.getName().substring(0, file.getName().length() - 5));
                }
            }
        }
        if (!names.contains(activeProfile)) {
            names.add(activeProfile);
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    /** Creates the starter profiles the first time Piko runs. */
    public void createStarterProfiles() {
        for (String name : STARTER_PROFILES) {
            File file = fileFor(name);
            if (!file.exists()) {
                FileUtil.writeJson(file, snapshot(name));
            }
        }
    }

    public JsonObject snapshot(String name) {
        PikoClient client = PikoClient.getInstance();
        ConfigManager config = client.getConfigManager();
        JsonObject root = new JsonObject();
        root.addProperty("name", name);
        root.add("modules", config.serializeModules());
        root.add("hud", config.serializeHud());
        root.add("keybinds", client.getKeybindManager().serialize());
        return root;
    }

    public void saveActiveProfile() {
        FileUtil.writeJson(fileFor(activeProfile), snapshot(activeProfile));
    }

    public boolean createProfile(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        File file = fileFor(name);
        if (file.exists()) {
            return false;
        }
        return FileUtil.writeJson(file, snapshot(name.trim()));
    }

    public boolean renameProfile(String oldName, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }
        File source = fileFor(oldName);
        File target = fileFor(newName);
        if (!source.exists() || target.exists()) {
            return false;
        }
        JsonObject stored = FileUtil.readJson(source);
        if (stored == null) {
            return false;
        }
        stored.addProperty("name", newName.trim());
        if (!FileUtil.writeJson(target, stored)) {
            return false;
        }
        if (!source.delete()) {
            System.err.println("[Piko] Could not delete the old profile file " + source.getName());
        }
        if (activeProfile.equalsIgnoreCase(oldName)) {
            activeProfile = newName.trim();
        }
        SettingManager.markDirty();
        return true;
    }

    public boolean deleteProfile(String name) {
        if (DEFAULT_PROFILE.equalsIgnoreCase(name)) {
            return false;
        }
        File file = fileFor(name);
        boolean deleted = file.exists() && file.delete();
        if (deleted && activeProfile.equalsIgnoreCase(name)) {
            switchTo(DEFAULT_PROFILE);
        }
        return deleted;
    }

    /** Stores the current setup under the old profile, then loads the new one. */
    public void switchTo(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase(activeProfile)) {
            return;
        }
        saveActiveProfile();
        activeProfile = name.trim();
        File file = fileFor(activeProfile);
        if (!file.exists()) {
            FileUtil.writeJson(file, snapshot(activeProfile));
        } else {
            apply(FileUtil.readJson(file));
        }
        PikoClient.getInstance().getConfigManager().save();
    }

    public void apply(JsonObject profile) {
        if (profile == null) {
            return;
        }
        PikoClient client = PikoClient.getInstance();
        ModuleManager modules = client.getModuleManager();
        SettingManager.setLoading(true);
        try {
            if (profile.has("modules") && profile.get("modules").isJsonObject()) {
                client.getConfigManager().applyModules(profile.getAsJsonObject("modules"));
            }
            if (profile.has("hud") && profile.get("hud").isJsonObject()) {
                client.getConfigManager().applyHud(profile.getAsJsonObject("hud"));
            }
            if (profile.has("keybinds") && profile.get("keybinds").isJsonObject()) {
                client.getKeybindManager().deserialize(profile.getAsJsonObject("keybinds"));
            }
        } finally {
            SettingManager.setLoading(false);
        }
        modules.syncListeners();
    }

    /** Copies a profile into {@code piko/exports/} so it can be shared. */
    public File exportProfile(String name) {
        JsonObject stored = FileUtil.readJson(fileFor(name));
        if (stored == null) {
            stored = snapshot(name);
        }
        File target = new File(PikoClient.getInstance().getConfigManager().getExportsDirectory(),
                FileUtil.sanitize(name) + ".json");
        return FileUtil.writeJson(target, stored) ? target : null;
    }

    /** Imports any json file previously exported, keeping the name stored inside it. */
    public boolean importProfile(File file) {
        JsonObject stored = FileUtil.readJson(file);
        if (stored == null) {
            return false;
        }
        String name = stored.has("name")
                ? stored.get("name").getAsString()
                : file.getName().replace(".json", "");
        stored.addProperty("name", name);
        return FileUtil.writeJson(fileFor(name), stored);
    }

    /** Lists importable files sitting in the exports folder. */
    public List<File> listImportable() {
        List<File> files = new ArrayList<File>();
        File[] found = PikoClient.getInstance().getConfigManager().getExportsDirectory()
                .listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".json");
                    }
                });
        if (found != null) {
            Collections.addAll(files, found);
        }
        return files;
    }
}
