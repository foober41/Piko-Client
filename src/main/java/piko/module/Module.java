package piko.module;

import net.minecraft.client.Minecraft;
import piko.PikoClient;
import piko.setting.KeybindSetting;
import piko.setting.Setting;
import piko.setting.SettingManager;

import java.util.List;

/**
 * A single Piko feature.
 *
 * <p>Modules are pure client side behaviour. Enabling one registers it with the event bus
 * and disabling one removes it again, so a disabled module never executes a line of code.</p>
 */
public abstract class Module {

    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final String id;
    private final boolean enabledByDefault;

    protected final SettingManager settings = new SettingManager();
    private KeybindSetting keybind;
    private boolean enabled;
    private boolean keybindOpensAction;
    private boolean forgeEvents;

    protected Module(String name, String description, ModuleCategory category) {
        this(name, description, category, false);
    }

    protected Module(String name, String description, ModuleCategory category, boolean enabledByDefault) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.id = name.toLowerCase().replace(' ', '_');
        this.enabledByDefault = enabledByDefault;
        this.enabled = enabledByDefault;
    }

    /** Gives the module a bindable hotkey; {@code 0} means unbound by default. */
    protected KeybindSetting bindable(int defaultKey) {
        keybind = settings.add(new KeybindSetting("Keybind", defaultKey));
        return keybind;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public KeybindSetting getKeybind() {
        return keybind;
    }

    public boolean hasKeybind() {
        return keybind != null;
    }

    /**
     * Marks the keybind as running {@link #onKeybindPressed()} instead of toggling the
     * module, which suits modules that perform an action such as opening a screen.
     */
    protected void setKeybindOpensAction(boolean value) {
        this.keybindOpensAction = value;
    }

    public boolean isKeybindAction() {
        return keybindOpensAction;
    }

    public void onKeybindPressed() {
    }

    public List<Setting> getSettings() {
        return settings.getSettings();
    }

    public SettingManager getSettingManager() {
        return settings;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        PikoClient client = PikoClient.getInstance();
        if (client != null) {
            if (value) {
                client.getEventBus().register(this);
            } else {
                client.getEventBus().unregister(this);
            }
        }
        updateForgeSubscription(value);
        SettingManager.markDirty();
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * Applies a stored enabled flag without firing the toggle callbacks that expect a
     * fully initialised game, used while the config is being loaded.
     */
    public void setEnabledQuietly(boolean value) {
        enabled = value;
    }

    /**
     * Declares that this module handles raw Forge events itself.
     *
     * <p>The subscription follows the enabled state, so a disabled module is not even on
     * the Forge bus.</p>
     */
    protected void useForgeEvents() {
        this.forgeEvents = true;
    }

    public void updateForgeSubscription(boolean subscribe) {
        if (!forgeEvents) {
            return;
        }
        if (subscribe) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
        } else {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    /** Called once after the config has been applied and the client is fully loaded. */
    public void onPostInit() {
    }

    /**
     * Called by the module manager once a module is registered, after the subclass
     * constructor has added its own options. Used to append shared settings such as the
     * HUD scale so they always appear at the end of the list.
     */
    public void finalizeSettings() {
    }

    /** Restores every setting of this module, including its keybind, to the defaults. */
    public void resetSettings() {
        settings.resetAll();
    }

    /** Extra words that should match this module in the search box. */
    public String getSearchTerms() {
        return name + " " + description + " " + category.getDisplayName();
    }
}
