package piko.setting;

/**
 * A number that the GUI always draws as a slider.
 *
 * <p>Behaves exactly like {@link NumberSetting}; the distinct type exists so number
 * fields that are better typed than dragged (for example a fixed HUD offset) can opt out
 * of the slider widget.</p>
 */
public class SliderSetting extends NumberSetting {

    public SliderSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue, min, max, step);
    }
}
