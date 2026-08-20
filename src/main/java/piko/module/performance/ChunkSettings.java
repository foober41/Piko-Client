package piko.module.performance;

import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;
import piko.setting.Setting;

/**
 * Chunk rendering options.
 *
 * <p>These write into the same game settings the vanilla video options use and then ask
 * the renderer to rebuild, which is exactly what changing the option in the menu does.</p>
 */
public class ChunkSettings extends Module {

    private final NumberSetting renderDistance;
    private final BooleanSetting useVbo;
    private final BooleanSetting smoothLighting;

    public ChunkSettings() {
        super("Chunk Settings", "Render distance and chunk rendering", ModuleCategory.PERFORMANCE);
        renderDistance = settings.add(new NumberSetting("Render Distance", 8.0D, 2.0D, 16.0D, 1.0D).suffix(" chunks"));
        useVbo = settings.add(new BooleanSetting("Use VBOs", true));
        smoothLighting = settings.add(new BooleanSetting("Smooth Lighting", true));

        Setting.ChangeListener listener = new Setting.ChangeListener() {
            @Override
            public void onSettingChanged(Setting setting) {
                apply();
            }
        };
        renderDistance.onChange(listener);
        useVbo.onChange(listener);
        smoothLighting.onChange(listener);
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

    private void apply() {
        if (!isEnabled() || mc.gameSettings == null) {
            return;
        }
        boolean needsReload = mc.gameSettings.renderDistanceChunks != renderDistance.getInt()
                || mc.gameSettings.useVbo != useVbo.get()
                || (mc.gameSettings.ambientOcclusion > 0) != smoothLighting.get();

        mc.gameSettings.renderDistanceChunks = renderDistance.getInt();
        mc.gameSettings.useVbo = useVbo.get();
        mc.gameSettings.ambientOcclusion = smoothLighting.get() ? 2 : 0;

        if (needsReload && mc.renderGlobal != null) {
            mc.renderGlobal.loadRenderers();
        }
    }
}
