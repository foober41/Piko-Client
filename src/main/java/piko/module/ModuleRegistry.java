package piko.module;

import piko.module.gui.GuiAnimationsModule;
import piko.module.gui.GuiScaleModule;
import piko.module.gui.HudEditorModule;
import piko.module.gui.MainMenuModule;
import piko.module.gui.PikoThemeModule;
import piko.module.hud.ArmorStatusHud;
import piko.module.hud.ClockHud;
import piko.module.hud.CoordinatesHud;
import piko.module.hud.CpsHud;
import piko.module.hud.FpsHud;
import piko.module.hud.KeystrokesHud;
import piko.module.hud.MemoryHud;
import piko.module.hud.PackDisplayHud;
import piko.module.hud.PingHud;
import piko.module.hud.PotionEffectsHud;

/** Single place where every Piko module is created and handed to the manager. */
public final class ModuleRegistry {

    private ModuleRegistry() {
    }

    public static void registerAll(ModuleManager manager) {
        registerHud(manager);
        registerGui(manager);
    }

    private static void registerHud(ModuleManager manager) {
        manager.register(new FpsHud());
        manager.register(new CpsHud());
        manager.register(new KeystrokesHud());
        manager.register(new PingHud());
        manager.register(new CoordinatesHud());
        manager.register(new ArmorStatusHud());
        manager.register(new PotionEffectsHud());
        manager.register(new PackDisplayHud());
        manager.register(new ClockHud());
        manager.register(new MemoryHud());
    }

    private static void registerGui(ModuleManager manager) {
        manager.register(new HudEditorModule());
        manager.register(new PikoThemeModule());
        manager.register(new GuiScaleModule());
        manager.register(new GuiAnimationsModule());
        manager.register(new MainMenuModule());
    }
}
