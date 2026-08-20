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
import piko.module.performance.AnimationSettings;
import piko.module.performance.ChunkSettings;
import piko.module.performance.EntityRenderingModule;
import piko.module.performance.FpsBoost;
import piko.module.performance.MemoryOptimization;
import piko.module.performance.ParticleSettings;
import piko.module.pvp.BlockHitAnimation;
import piko.module.pvp.ComboCounter;
import piko.module.pvp.Crosshair;
import piko.module.pvp.DirectionHud;
import piko.module.pvp.HitColor;
import piko.module.pvp.ReachDisplay;
import piko.module.pvp.ToggleSneak;
import piko.module.pvp.ToggleSprint;
import piko.module.visual.ChatCustomization;
import piko.module.visual.Fullbright;
import piko.module.visual.ItemPhysics;
import piko.module.visual.LowFire;
import piko.module.visual.MotionBlur;
import piko.module.visual.NameTags;
import piko.module.visual.OldAnimations;
import piko.module.visual.Perspective;
import piko.module.visual.TimeChanger;
import piko.module.visual.VisualTweaks;
import piko.module.visual.Zoom;

/** Single place where every Piko module is created and handed to the manager. */
public final class ModuleRegistry {

    private ModuleRegistry() {
    }

    public static void registerAll(ModuleManager manager) {
        registerHud(manager);
        registerPvp(manager);
        registerVisual(manager);
        registerPerformance(manager);
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

    private static void registerPvp(ModuleManager manager) {
        manager.register(new ToggleSprint());
        manager.register(new ToggleSneak());
        manager.register(new Crosshair());
        manager.register(new BlockHitAnimation());
        manager.register(new HitColor());
        manager.register(new ComboCounter());
        manager.register(new ReachDisplay());
        manager.register(new DirectionHud());
    }

    private static void registerVisual(ModuleManager manager) {
        manager.register(new TimeChanger());
        manager.register(new Fullbright());
        manager.register(new MotionBlur());
        manager.register(new ItemPhysics());
        manager.register(new Perspective());
        manager.register(new Zoom());
        manager.register(new NameTags());
        manager.register(new ChatCustomization());
        manager.register(new OldAnimations());
        manager.register(new LowFire());
        manager.register(new VisualTweaks());
    }

    private static void registerPerformance(ModuleManager manager) {
        manager.register(new FpsBoost());
        manager.register(new ParticleSettings());
        manager.register(new EntityRenderingModule());
        manager.register(new AnimationSettings());
        manager.register(new ChunkSettings());
        manager.register(new MemoryOptimization());
    }

    private static void registerGui(ModuleManager manager) {
        manager.register(new HudEditorModule());
        manager.register(new PikoThemeModule());
        manager.register(new GuiScaleModule());
        manager.register(new GuiAnimationsModule());
        manager.register(new MainMenuModule());
    }
}
