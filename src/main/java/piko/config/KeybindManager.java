package piko.config;

import com.google.gson.JsonObject;
import org.lwjgl.input.Keyboard;
import piko.PikoClient;
import piko.module.Module;
import piko.setting.KeybindSetting;
import piko.setting.Setting;

import java.util.List;

/**
 * Owns the client level hotkeys and serialises every keybind in the client.
 *
 * <p>Module keybinds live on the modules themselves, this class just gathers them so all
 * of them end up in a single readable {@code keybinds.json}.</p>
 */
public class KeybindManager {

    private final KeybindSetting menuKey = new KeybindSetting("Piko Menu", Keyboard.KEY_RSHIFT);
    private final KeybindSetting hudEditorKey = new KeybindSetting("HUD Editor", Keyboard.KEY_NONE);

    public KeybindSetting getMenuKey() {
        return menuKey;
    }

    public KeybindSetting getHudEditorKey() {
        return hudEditorKey;
    }

    public JsonObject serialize() {
        JsonObject root = new JsonObject();

        JsonObject client = new JsonObject();
        client.add("menu", menuKey.serialize());
        client.add("hud_editor", hudEditorKey.serialize());
        root.add("client", client);

        JsonObject modules = new JsonObject();
        List<Module> moduleList = PikoClient.getInstance().getModuleManager().getModules();
        for (int i = 0; i < moduleList.size(); i++) {
            Module module = moduleList.get(i);
            JsonObject moduleBinds = module.getSettingManager().serialize(true);
            if (moduleBinds.entrySet().size() > 0) {
                modules.add(module.getId(), moduleBinds);
            }
        }
        root.add("modules", modules);
        return root;
    }

    public void deserialize(JsonObject root) {
        if (root == null) {
            return;
        }
        if (root.has("client") && root.get("client").isJsonObject()) {
            JsonObject client = root.getAsJsonObject("client");
            menuKey.deserialize(client.get("menu"));
            hudEditorKey.deserialize(client.get("hud_editor"));
        }
        if (root.has("modules") && root.get("modules").isJsonObject()) {
            JsonObject modules = root.getAsJsonObject("modules");
            List<Module> moduleList = PikoClient.getInstance().getModuleManager().getModules();
            for (int i = 0; i < moduleList.size(); i++) {
                Module module = moduleList.get(i);
                if (modules.has(module.getId()) && modules.get(module.getId()).isJsonObject()) {
                    module.getSettingManager().deserialize(modules.getAsJsonObject(module.getId()));
                }
            }
        }
    }

    /** Restores the two client hotkeys and every module keybind to their defaults. */
    public void resetAll() {
        menuKey.reset();
        hudEditorKey.reset();
        List<Module> moduleList = PikoClient.getInstance().getModuleManager().getModules();
        for (int i = 0; i < moduleList.size(); i++) {
            List<Setting> settings = moduleList.get(i).getSettings();
            for (int j = 0; j < settings.size(); j++) {
                if (settings.get(j) instanceof KeybindSetting) {
                    settings.get(j).reset();
                }
            }
        }
    }
}
