package piko.module;

import piko.PikoClient;
import piko.setting.KeybindSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Registry and lookup for every Piko module. */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<Module>();
    private final Map<Class<? extends Module>, Module> byClass = new HashMap<Class<? extends Module>, Module>();
    private final Map<ModuleCategory, List<Module>> byCategory = new HashMap<ModuleCategory, List<Module>>();
    private final List<HudModule> hudModules = new ArrayList<HudModule>();

    public ModuleManager() {
        for (ModuleCategory category : ModuleCategory.values()) {
            byCategory.put(category, new ArrayList<Module>());
        }
    }

    public <T extends Module> T register(T module) {
        modules.add(module);
        byClass.put(module.getClass(), module);
        byCategory.get(module.getCategory()).add(module);
        if (module instanceof HudModule) {
            hudModules.add((HudModule) module);
        }
        module.finalizeSettings();
        return module;
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<HudModule> getHudModules() {
        return hudModules;
    }

    public List<Module> getModules(ModuleCategory category) {
        return byCategory.get(category);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> type) {
        return (T) byClass.get(type);
    }

    public Module getModule(String name) {
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            if (module.getName().equalsIgnoreCase(name) || module.getId().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    /**
     * Fuzzy module search used by the settings screen.
     *
     * <p>Name prefixes rank above name matches, which rank above description matches, so
     * typing {@code cross} lands on Crosshair and {@code fps} on the FPS counter.</p>
     */
    public List<Module> search(String query) {
        List<Module> results = new ArrayList<Module>();
        if (query == null || query.trim().isEmpty()) {
            results.addAll(modules);
            return results;
        }
        final String needle = query.trim().toLowerCase();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            if (score(module, needle) > 0) {
                results.add(module);
            }
        }
        Collections.sort(results, new Comparator<Module>() {
            @Override
            public int compare(Module first, Module second) {
                int difference = score(second, needle) - score(first, needle);
                return difference != 0 ? difference : first.getName().compareToIgnoreCase(second.getName());
            }
        });
        return results;
    }

    private static int score(Module module, String needle) {
        String name = module.getName().toLowerCase();
        if (name.startsWith(needle)) {
            return 100;
        }
        if (name.contains(needle)) {
            return 70;
        }
        if (module.getId().contains(needle)) {
            return 60;
        }
        if (module.getSearchTerms().toLowerCase().contains(needle)) {
            return 30;
        }
        return 0;
    }

    /** Toggles any module bound to the pressed key. */
    public void handleKeyPress(int key) {
        if (key == 0) {
            return;
        }
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            KeybindSetting keybind = module.getKeybind();
            if (keybind != null && keybind.matches(key)) {
                if (module.isKeybindAction()) {
                    module.onKeybindPressed();
                } else {
                    module.toggle();
                }
            }
        }
    }

    /**
     * Brings the event bus in line with the enabled flags, used after a config or profile
     * has been loaded without firing enable callbacks per module.
     */
    public void syncListeners() {
        PikoClient client = PikoClient.getInstance();
        if (client == null) {
            return;
        }
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            if (module.isEnabled()) {
                client.getEventBus().register(module);
            } else {
                client.getEventBus().unregister(module);
            }
            module.updateForgeSubscription(module.isEnabled());
        }
    }

    public void postInit() {
        for (int i = 0; i < modules.size(); i++) {
            modules.get(i).onPostInit();
        }
    }
}
