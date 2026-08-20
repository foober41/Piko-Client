package piko.font;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Lazily created Piko fonts.
 *
 * <p>Only logical or already installed families are used so no binary font asset has to be
 * shipped. Atlases are built the first time a font is actually drawn, which keeps start up
 * time untouched for players who never enable a Piko font.</p>
 */
public final class FontManager {

    private static final String[] PREFERRED_FAMILIES = {"Inter", "Segoe UI", "Helvetica Neue", "Roboto", "Arial"};

    private static PikoFontRenderer small;
    private static PikoFontRenderer regular;
    private static PikoFontRenderer medium;
    private static PikoFontRenderer title;
    private static String family;

    private FontManager() {
    }

    private static String family() {
        if (family == null) {
            family = Font.SANS_SERIF;
            try {
                String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                outer:
                for (String preferred : PREFERRED_FAMILIES) {
                    for (String candidate : available) {
                        if (candidate.equalsIgnoreCase(preferred)) {
                            family = candidate;
                            break outer;
                        }
                    }
                }
            } catch (Throwable ignored) {
                // Headless or restricted environments keep the logical sans serif family.
            }
        }
        return family;
    }

    public static IFont small() {
        if (small == null) {
            small = new PikoFontRenderer(new Font(family(), Font.PLAIN, 8), true);
        }
        return small;
    }

    public static IFont regular() {
        if (regular == null) {
            regular = new PikoFontRenderer(new Font(family(), Font.PLAIN, 10), true);
        }
        return regular;
    }

    public static IFont medium() {
        if (medium == null) {
            medium = new PikoFontRenderer(new Font(family(), Font.BOLD, 12), true);
        }
        return medium;
    }

    public static IFont title() {
        if (title == null) {
            title = new PikoFontRenderer(new Font(family(), Font.BOLD, 24), true);
        }
        return title;
    }

    /** Resolves the font named by a module {@code Font} setting. */
    public static IFont byName(String name) {
        if (name == null) {
            return MinecraftFont.INSTANCE;
        }
        if (name.equalsIgnoreCase("Piko")) {
            return regular();
        }
        if (name.equalsIgnoreCase("Piko Small")) {
            return small();
        }
        if (name.equalsIgnoreCase("Piko Bold")) {
            return medium();
        }
        return MinecraftFont.INSTANCE;
    }

    /** Font choices offered to the player in module settings. */
    public static String[] options() {
        return new String[]{"Minecraft", "Piko", "Piko Small", "Piko Bold"};
    }
}
