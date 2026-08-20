package piko.render;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static int rgba(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    public static int alpha(int color, float alpha) {
        int value = (int) (Math.max(0F, Math.min(1F, alpha)) * 255F);
        return (color & 0x00FFFFFF) | (value << 24);
    }

    /** Multiplies the existing alpha of a colour, used for fade animations. */
    public static int fade(int color, float factor) {
        int existing = (color >> 24) & 0xFF;
        int faded = (int) (existing * Math.max(0F, Math.min(1F, factor)));
        return (color & 0x00FFFFFF) | (faded << 24);
    }

    public static int mix(int from, int to, float progress) {
        float clamped = Math.max(0F, Math.min(1F, progress));
        int a = (int) (((from >> 24) & 0xFF) + (((to >> 24) & 0xFF) - ((from >> 24) & 0xFF)) * clamped);
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * clamped);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * clamped);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * clamped);
        return rgba(r, g, b, a);
    }

    /** Darkens a colour towards black, keeping its alpha. */
    public static int darker(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return rgba(r, g, b, a);
    }

    public static int brighter(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int) ((color & 0xFF) * factor));
        return rgba(r, g, b, a);
    }

    public static float[] toHsb(int color) {
        return java.awt.Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
    }

    public static int fromHsb(float hue, float saturation, float brightness, int alpha) {
        int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        return (rgb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }
}
