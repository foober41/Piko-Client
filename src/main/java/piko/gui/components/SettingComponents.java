package piko.gui.components;

import piko.setting.BooleanSetting;
import piko.setting.ColorSetting;
import piko.setting.KeybindSetting;
import piko.setting.ModeSetting;
import piko.setting.NumberSetting;
import piko.setting.Setting;
import piko.setting.StringSetting;

/** Maps a setting to the widget that edits it. */
public final class SettingComponents {

    private SettingComponents() {
    }

    public static Component create(Setting setting) {
        if (setting instanceof BooleanSetting) {
            return new BooleanRow((BooleanSetting) setting);
        }
        if (setting instanceof NumberSetting) {
            return new SliderComponent((NumberSetting) setting);
        }
        if (setting instanceof ModeSetting) {
            return new DropdownComponent((ModeSetting) setting);
        }
        if (setting instanceof ColorSetting) {
            return new ColorPickerComponent((ColorSetting) setting);
        }
        if (setting instanceof KeybindSetting) {
            return new KeybindComponent((KeybindSetting) setting);
        }
        if (setting instanceof StringSetting) {
            return new StringFieldComponent((StringSetting) setting);
        }
        return null;
    }
}
