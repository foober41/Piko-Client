package piko.util;

public final class MathUtil {

    private MathUtil() {
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    public static double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    public static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    /** Wraps a yaw into the -180 to 180 range. */
    public static float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }
}
