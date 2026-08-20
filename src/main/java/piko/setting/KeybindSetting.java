package piko.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import piko.util.KeyUtil;

/**
 * A single key binding stored as an LWJGL key code.
 *
 * <p>Negative values are mouse buttons ({@code -100 - button}) so extra mouse keys can be
 * bound the same way Minecraft itself does it.</p>
 */
public class KeybindSetting extends Setting {

    public static final int NONE = 0;

    private final int defaultKey;
    private int key;

    public KeybindSetting(String name, int defaultKey) {
        super(name);
        this.defaultKey = defaultKey;
        this.key = defaultKey;
    }

    public int get() {
        return key;
    }

    public void set(int key) {
        if (this.key != key) {
            this.key = key;
            fireChanged();
        }
    }

    public boolean isBound() {
        return key != NONE;
    }

    public boolean isDown() {
        return KeyUtil.isKeyDown(key);
    }

    public boolean matches(int pressedKey) {
        return key != NONE && key == pressedKey;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(key);
    }

    @Override
    public void deserialize(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            key = element.getAsInt();
        }
    }

    @Override
    public void reset() {
        set(defaultKey);
    }

    @Override
    public String displayValue() {
        return KeyUtil.getKeyName(key);
    }
}
