package piko.util;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Key code helpers.
 *
 * <p>Piko stores keyboard keys as plain LWJGL key codes and mouse buttons as
 * {@code -100 - button}, matching the convention Minecraft itself uses for its own
 * key bindings so bound mouse buttons behave the same everywhere.</p>
 */
public final class KeyUtil {

    public static final int MOUSE_OFFSET = -100;

    private KeyUtil() {
    }

    public static int fromMouseButton(int button) {
        return MOUSE_OFFSET - button;
    }

    public static boolean isMouse(int key) {
        return key < 0 && key != Keyboard.KEY_NONE;
    }

    public static int toMouseButton(int key) {
        return MOUSE_OFFSET - key;
    }

    public static boolean isKeyDown(int key) {
        if (key == Keyboard.KEY_NONE) {
            return false;
        }
        if (isMouse(key)) {
            int button = toMouseButton(key);
            return button >= 0 && button < Mouse.getButtonCount() && Mouse.isButtonDown(button);
        }
        return key < Keyboard.KEYBOARD_SIZE && Keyboard.isKeyDown(key);
    }

    public static String getKeyName(int key) {
        if (key == Keyboard.KEY_NONE) {
            return "NONE";
        }
        if (isMouse(key)) {
            int button = toMouseButton(key);
            switch (button) {
                case 0:
                    return "LMB";
                case 1:
                    return "RMB";
                case 2:
                    return "MMB";
                default:
                    return "MOUSE " + (button + 1);
            }
        }
        String name = Keyboard.getKeyName(key);
        return name == null ? "KEY " + key : name;
    }
}
