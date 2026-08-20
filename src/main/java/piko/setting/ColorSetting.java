package piko.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** An ARGB colour, edited through the Piko colour picker and stored as a hex string. */
public class ColorSetting extends Setting {

    private final int defaultValue;
    private int value;

    public ColorSetting(String name, int defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public int get() {
        return value;
    }

    /** The colour with its alpha replaced by {@code alpha} in the 0-1 range. */
    public int withAlpha(float alpha) {
        int a = (int) (Math.max(0F, Math.min(1F, alpha)) * 255F);
        return (value & 0x00FFFFFF) | (a << 24);
    }

    public void set(int value) {
        if (this.value != value) {
            this.value = value;
            fireChanged();
        }
    }

    public int getAlpha() {
        return (value >> 24) & 0xFF;
    }

    public int getRed() {
        return (value >> 16) & 0xFF;
    }

    public int getGreen() {
        return (value >> 8) & 0xFF;
    }

    public int getBlue() {
        return value & 0xFF;
    }

    public void setComponents(int red, int green, int blue, int alpha) {
        set(((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(String.format("#%08X", value));
    }

    @Override
    public void deserialize(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return;
        }
        String raw = element.getAsString().trim();
        if (raw.startsWith("#")) {
            raw = raw.substring(1);
        }
        try {
            long parsed = Long.parseLong(raw, 16);
            if (raw.length() <= 6) {
                parsed |= 0xFF000000L;
            }
            value = (int) parsed;
        } catch (NumberFormatException ignored) {
            // Keep the previous value when a config file was edited by hand and broke.
        }
    }

    @Override
    public void reset() {
        set(defaultValue);
    }

    @Override
    public String displayValue() {
        return String.format("#%06X", value & 0xFFFFFF);
    }
}
