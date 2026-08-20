package piko.setting;

import com.google.gson.JsonElement;

/**
 * Base class for every configurable value in Piko.
 *
 * <p>A setting owns its value, knows how to serialise itself and can tell the GUI whether
 * it is currently relevant (see {@link #setVisibility(VisibilityRule)}), which is how
 * dependent options such as {@code Custom Prefix} hide themselves.</p>
 */
public abstract class Setting {

    /** Decides whether a setting should currently be offered in the GUI. */
    public interface VisibilityRule {
        boolean isVisible();
    }

    /** Notified whenever the value of a setting actually changes. */
    public interface ChangeListener {
        void onSettingChanged(Setting setting);
    }

    private final String name;
    private final String key;
    private String description = "";
    private VisibilityRule visibility;
    private ChangeListener listener;

    protected Setting(String name) {
        this.name = name;
        this.key = name.toLowerCase().replace(' ', '_').replace(".", "");
    }

    public String getName() {
        return name;
    }

    /** Stable identifier used inside the json files, independent of the display name casing. */
    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public Setting describe(String description) {
        this.description = description;
        return this;
    }

    public Setting setVisibility(VisibilityRule visibility) {
        this.visibility = visibility;
        return this;
    }

    public boolean isVisible() {
        return visibility == null || visibility.isVisible();
    }

    public Setting onChange(ChangeListener listener) {
        this.listener = listener;
        return this;
    }

    protected void fireChanged() {
        SettingManager.markDirty();
        if (listener != null) {
            listener.onSettingChanged(this);
        }
    }

    public abstract JsonElement serialize();

    public abstract void deserialize(JsonElement element);

    public abstract void reset();

    /** Short human readable form of the current value, used on the setting cards. */
    public abstract String displayValue();
}
