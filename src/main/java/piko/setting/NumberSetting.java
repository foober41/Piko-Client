package piko.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * A numeric value constrained to a range and snapped to a step.
 *
 * <p>Values are kept as doubles but rounded to the step so config files stay readable and
 * sliders always land on clean numbers.</p>
 */
public class NumberSetting extends Setting {

    private final double defaultValue;
    private final double min;
    private final double max;
    private final double step;
    private String suffix = "";
    private double value;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name);
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? 1 : step;
        this.value = defaultValue;
    }

    public double get() {
        return value;
    }

    public float getFloat() {
        return (float) value;
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public void set(double newValue) {
        double clamped = clamp(newValue);
        if (clamped != value) {
            value = clamped;
            fireChanged();
        }
    }

    private double clamp(double input) {
        double clamped = Math.max(min, Math.min(max, input));
        double snapped = Math.round(clamped / step) * step;
        // Guard against floating point dust such as 0.30000000000000004.
        snapped = Math.round(snapped * 1000.0D) / 1000.0D;
        return Math.max(min, Math.min(max, snapped));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    /** Position of the current value inside the range, 0 to 1. */
    public double getProgress() {
        if (max - min == 0) {
            return 0;
        }
        return (value - min) / (max - min);
    }

    public void setProgress(double progress) {
        set(min + (max - min) * Math.max(0, Math.min(1, progress)));
    }

    public NumberSetting suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String getSuffix() {
        return suffix;
    }

    /** True when the setting only ever holds whole numbers, so the GUI can hide decimals. */
    public boolean isInteger() {
        return step >= 1.0D && min == Math.floor(min) && max == Math.floor(max);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(value);
    }

    @Override
    public void deserialize(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            value = clamp(element.getAsDouble());
        }
    }

    @Override
    public void reset() {
        set(defaultValue);
    }

    @Override
    public String displayValue() {
        if (isInteger()) {
            return getInt() + suffix;
        }
        return String.format("%.2f", value) + suffix;
    }
}
