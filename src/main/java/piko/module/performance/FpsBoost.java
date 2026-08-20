package piko.module.performance;

import piko.PikoClient;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.module.visual.MotionBlur;
import piko.setting.BooleanSetting;
import piko.setting.ModeSetting;
import piko.setting.NumberSetting;
import piko.setting.Setting;

/**
 * Frame rate presets plus the individual switches behind them.
 *
 * <p>Choosing a preset writes concrete values into the Minecraft settings and into the
 * other Piko performance modules. Everything it touched stays editable afterwards, so a
 * preset is a starting point and never a lock.</p>
 */
public class FpsBoost extends Module {

    private final ModeSetting preset;
    private final NumberSetting maxFramerate;
    private final BooleanSetting fancyGraphics;
    private final BooleanSetting clouds;
    private final BooleanSetting entityShadows;
    private final BooleanSetting viewBobbing;
    private final NumberSetting mipmapLevels;

    private boolean applyingPreset;

    public FpsBoost() {
        super("FPS Boost", "Performance presets and the switches behind them",
                ModuleCategory.PERFORMANCE, true);
        preset = settings.add(new ModeSetting("Preset", "Balanced", "Quality", "Balanced", "Maximum FPS"));
        maxFramerate = settings.add(new NumberSetting("Max Framerate", 260.0D, 30.0D, 260.0D, 10.0D).suffix(" fps"));
        fancyGraphics = settings.add(new BooleanSetting("Fancy Graphics", true));
        clouds = settings.add(new BooleanSetting("Clouds", true));
        entityShadows = settings.add(new BooleanSetting("Entity Shadows", true));
        viewBobbing = settings.add(new BooleanSetting("View Bobbing", true));
        mipmapLevels = settings.add(new NumberSetting("Mipmap Levels", 4.0D, 0.0D, 4.0D, 1.0D));

        preset.onChange(new Setting.ChangeListener() {
            @Override
            public void onSettingChanged(Setting setting) {
                applyPreset();
            }
        });
        Setting.ChangeListener applyListener = new Setting.ChangeListener() {
            @Override
            public void onSettingChanged(Setting setting) {
                apply();
            }
        };
        maxFramerate.onChange(applyListener);
        fancyGraphics.onChange(applyListener);
        clouds.onChange(applyListener);
        entityShadows.onChange(applyListener);
        viewBobbing.onChange(applyListener);
        mipmapLevels.onChange(applyListener);
    }

    public boolean isMaximumMode() {
        return isEnabled() && preset.is("Maximum FPS");
    }

    @Override
    protected void onEnable() {
        apply();
    }

    @Override
    public void onPostInit() {
        if (isEnabled()) {
            apply();
        }
    }

    private void applyPreset() {
        if (applyingPreset) {
            return;
        }
        applyingPreset = true;
        try {
            if (preset.is("Quality")) {
                fancyGraphics.set(true);
                clouds.set(true);
                entityShadows.set(true);
                viewBobbing.set(true);
                mipmapLevels.set(4);
                maxFramerate.set(260);
                particles(1.0D);
                entityDistances(96, 64, 48);
            } else if (preset.is("Maximum FPS")) {
                fancyGraphics.set(false);
                clouds.set(false);
                entityShadows.set(false);
                viewBobbing.set(false);
                mipmapLevels.set(0);
                maxFramerate.set(260);
                particles(0.2D);
                entityDistances(64, 24, 12);
                disableExpensiveEffects();
            } else {
                fancyGraphics.set(true);
                clouds.set(false);
                entityShadows.set(false);
                viewBobbing.set(true);
                mipmapLevels.set(2);
                maxFramerate.set(260);
                particles(0.6D);
                entityDistances(96, 48, 24);
            }
        } finally {
            applyingPreset = false;
        }
        apply();
    }

    private void particles(double multiplier) {
        ParticleSettings particles = PikoClient.getInstance().getModuleManager().getModule(ParticleSettings.class);
        if (particles == null) {
            return;
        }
        particles.setEnabled(true);
        Setting setting = particles.getSettingManager().getByName("Particle Multiplier");
        if (setting instanceof NumberSetting) {
            ((NumberSetting) setting).set(multiplier);
        }
    }

    private void entityDistances(double players, double mobs, double items) {
        EntityRenderingModule entities = PikoClient.getInstance().getModuleManager()
                .getModule(EntityRenderingModule.class);
        if (entities == null) {
            return;
        }
        set(entities, "Player Distance", players);
        set(entities, "Mob Distance", mobs);
        set(entities, "Dropped Item Distance", items);
    }

    private void set(Module module, String name, double value) {
        Setting setting = module.getSettingManager().getByName(name);
        if (setting instanceof NumberSetting) {
            ((NumberSetting) setting).set(value);
        }
    }

    /** Maximum FPS turns off the effects that cost the most for the least benefit. */
    private void disableExpensiveEffects() {
        MotionBlur blur = PikoClient.getInstance().getModuleManager().getModule(MotionBlur.class);
        if (blur != null) {
            blur.setEnabled(false);
        }
        AnimationSettings animations = PikoClient.getInstance().getModuleManager()
                .getModule(AnimationSettings.class);
        if (animations != null) {
            animations.setEnabled(true);
            setBoolean(animations, "Water Animation", false);
            setBoolean(animations, "Lava Animation", false);
            setBoolean(animations, "Fire Animation", false);
            setBoolean(animations, "Portal Animation", false);
            setBoolean(animations, "Other Animations", false);
        }
    }

    private void setBoolean(Module module, String name, boolean value) {
        Setting setting = module.getSettingManager().getByName(name);
        if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).set(value);
        }
    }

    private void apply() {
        if (!isEnabled() || mc.gameSettings == null) {
            return;
        }
        mc.gameSettings.limitFramerate = maxFramerate.getInt();
        mc.gameSettings.fancyGraphics = fancyGraphics.get();
        mc.gameSettings.clouds = clouds.get() ? 2 : 0;
        mc.gameSettings.entityShadows = entityShadows.get();
        mc.gameSettings.viewBobbing = viewBobbing.get();

        int mipmap = mipmapLevels.getInt();
        if (mc.gameSettings.mipmapLevels != mipmap) {
            mc.gameSettings.mipmapLevels = mipmap;
            if (mc.getTextureMapBlocks() != null) {
                mc.getTextureMapBlocks().setMipmapLevels(mipmap);
                mc.getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture);
                mc.getTextureMapBlocks().setBlurMipmapDirect(false, mipmap > 0);
            }
        }
    }
}
