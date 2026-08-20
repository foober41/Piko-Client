package piko.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A dropdown of mutually exclusive named options. */
public class ModeSetting extends Setting {

    private final String[] modes;
    private final int defaultIndex;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = modes;
        int found = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(defaultMode)) {
                found = i;
                break;
            }
        }
        this.defaultIndex = found;
        this.index = found;
    }

    public String get() {
        return modes[index];
    }

    public int getIndex() {
        return index;
    }

    public String[] getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return modes[index].equalsIgnoreCase(mode);
    }

    public void set(String mode) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(mode)) {
                setIndex(i);
                return;
            }
        }
    }

    public void setIndex(int newIndex) {
        int wrapped = ((newIndex % modes.length) + modes.length) % modes.length;
        if (wrapped != index) {
            index = wrapped;
            fireChanged();
        }
    }

    public void cycle() {
        setIndex(index + 1);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(modes[index]);
    }

    @Override
    public void deserialize(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            String stored = element.getAsString();
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].equalsIgnoreCase(stored)) {
                    index = i;
                    return;
                }
            }
        }
    }

    @Override
    public void reset() {
        setIndex(defaultIndex);
    }

    @Override
    public String displayValue() {
        return modes[index];
    }
}
