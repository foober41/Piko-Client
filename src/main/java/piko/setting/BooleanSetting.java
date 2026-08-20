package piko.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting {

    private final boolean defaultValue;
    private boolean value;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        if (this.value != value) {
            this.value = value;
            fireChanged();
        }
    }

    public void toggle() {
        set(!value);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(value);
    }

    @Override
    public void deserialize(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            value = element.getAsBoolean();
        }
    }

    @Override
    public void reset() {
        set(defaultValue);
    }

    @Override
    public String displayValue() {
        return value ? "ON" : "OFF";
    }
}
