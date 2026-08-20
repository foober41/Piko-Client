package piko.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns a group of settings and translates it to and from json.
 *
 * <p>Every module has one of these. It also carries the global "config is dirty" flag:
 * changing any setting anywhere marks the configuration for the next scheduled save
 * instead of writing to disk immediately, which keeps the render thread free of file IO.</p>
 */
public class SettingManager {

    private static volatile boolean dirty;
    private static volatile boolean loading;

    private final List<Setting> settings = new ArrayList<Setting>();

    public static void markDirty() {
        if (!loading) {
            dirty = true;
        }
    }

    public static boolean isDirty() {
        return dirty;
    }

    public static void clearDirty() {
        dirty = false;
    }

    /** Suppresses dirty marking while a config file is being applied. */
    public static void setLoading(boolean value) {
        loading = value;
    }

    public <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public void addAll(Setting... toAdd) {
        for (Setting setting : toAdd) {
            settings.add(setting);
        }
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public Setting getByName(String name) {
        for (int i = 0; i < settings.size(); i++) {
            Setting setting = settings.get(i);
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    public void resetAll() {
        for (int i = 0; i < settings.size(); i++) {
            settings.get(i).reset();
        }
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        for (int i = 0; i < settings.size(); i++) {
            Setting setting = settings.get(i);
            object.add(setting.getKey(), setting.serialize());
        }
        return object;
    }

    /**
     * Serialises only the keybinds or only everything else.
     *
     * <p>Keybinds are kept in their own file so players can share a config without
     * overwriting the hotkeys they are used to.</p>
     */
    public JsonObject serialize(boolean keybindsOnly) {
        JsonObject object = new JsonObject();
        for (int i = 0; i < settings.size(); i++) {
            Setting setting = settings.get(i);
            if (setting instanceof KeybindSetting == keybindsOnly) {
                object.add(setting.getKey(), setting.serialize());
            }
        }
        return object;
    }

    public void deserialize(JsonObject object) {
        if (object == null) {
            return;
        }
        for (int i = 0; i < settings.size(); i++) {
            Setting setting = settings.get(i);
            JsonElement element = object.get(setting.getKey());
            if (element != null) {
                setting.deserialize(element);
            }
        }
    }
}
