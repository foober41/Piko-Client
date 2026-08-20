package piko.module.gui;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import piko.gui.hud.HudEditorScreen;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;

/**
 * Entry point and options for the HUD layout editor.
 *
 * <p>The keybind opens the editor instead of toggling the module, because an editor is an
 * action rather than a state.</p>
 */
public class HudEditorModule extends Module {

    private final BooleanSetting snapToEdges;
    private final BooleanSetting showGuides;
    private final NumberSetting snapDistance;

    public HudEditorModule() {
        super("HUD Editor", "Move, scale and toggle HUD elements", ModuleCategory.GUI, true);
        bindable(Keyboard.KEY_NONE);
        snapToEdges = settings.add(new BooleanSetting("Snap To Edges", true));
        showGuides = settings.add(new BooleanSetting("Alignment Guides", true));
        snapDistance = settings.add(new NumberSetting("Snap Distance", 4.0D, 1.0D, 12.0D, 1.0D));
        setKeybindOpensAction(true);
    }

    public boolean isSnapEnabled() {
        return snapToEdges.get();
    }

    public boolean areGuidesEnabled() {
        return showGuides.get();
    }

    public float getSnapDistance() {
        return snapDistance.getFloat();
    }

    @Override
    public void onKeybindPressed() {
        Minecraft.getMinecraft().displayGuiScreen(new HudEditorScreen(null));
    }
}
