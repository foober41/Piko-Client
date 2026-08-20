package piko.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Free text, for example the custom FPS prefix. */
public class StringSetting extends Setting {

    private final String defaultValue;
    private final int maxLength;
    private String value;

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, 32);
    }

    public StringSetting(String name, String defaultValue, int maxLength) {
        super(name);
        this.defaultValue = defaultValue;
        this.maxLength = maxLength;
        this.value = defaultValue;
    }

    public String get() {
        return value;
    }

    public void set(String newValue) {
        String trimmed = newValue == null ? "" : newValue;
        if (trimmed.length() > maxLength) {
            trimmed = trimmed.substring(0, maxLength);
        }
        if (!trimmed.equals(value)) {
            value = trimmed;
            fireChanged();
        }
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(value);
    }

    @Override
    public void deserialize(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            value = element.getAsString();
        }
    }

    @Override
    public void reset() {
        set(defaultValue);
    }

    @Override
    public String displayValue() {
        return value.isEmpty() ? "-" : value;
    }
}
