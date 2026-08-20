package piko.module.performance;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import piko.event.events.TickEvent;
import piko.event.listener.TickListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Turns individual animated block textures off.
 *
 * <p>Animated textures are re-uploaded to the GPU every tick. Dropping the ones a PvP
 * player never looks at removes that upload entirely rather than just hiding it.</p>
 */
public class AnimationSettings extends Module implements TickListener {

    private final BooleanSetting water;
    private final BooleanSetting lava;
    private final BooleanSetting fire;
    private final BooleanSetting portal;
    private final BooleanSetting other;

    private final List<TextureAtlasSprite> removed = new ArrayList<TextureAtlasSprite>();
    private boolean applied;

    public AnimationSettings() {
        super("Animation Settings", "Disable animated block textures", ModuleCategory.PERFORMANCE);
        water = settings.add(new BooleanSetting("Water Animation", true));
        lava = settings.add(new BooleanSetting("Lava Animation", true));
        fire = settings.add(new BooleanSetting("Fire Animation", true));
        portal = settings.add(new BooleanSetting("Portal Animation", true));
        other = settings.add(new BooleanSetting("Other Animations", true));
    }

    private boolean keep(String name) {
        if (name.contains("water")) {
            return water.get();
        }
        if (name.contains("lava")) {
            return lava.get();
        }
        if (name.contains("fire")) {
            return fire.get();
        }
        if (name.contains("portal")) {
            return portal.get();
        }
        return other.get();
    }

    @Override
    public void onTick(TickEvent event) {
        // Re-applied whenever the texture atlas is rebuilt, for example on a resource
        // pack change, which is why this is checked rather than done once.
        apply();
    }

    @Override
    protected void onEnable() {
        applied = false;
        apply();
    }

    @Override
    protected void onDisable() {
        restore();
    }

    private void apply() {
        TextureMap map = mc.getTextureMapBlocks();
        if (map == null || map.listAnimatedSprites == null) {
            return;
        }
        Iterator<TextureAtlasSprite> iterator = map.listAnimatedSprites.iterator();
        while (iterator.hasNext()) {
            TextureAtlasSprite sprite = iterator.next();
            if (!keep(sprite.getIconName())) {
                removed.add(sprite);
                iterator.remove();
            }
        }
        // Restore sprites whose category was switched back on.
        Iterator<TextureAtlasSprite> restoreIterator = removed.iterator();
        while (restoreIterator.hasNext()) {
            TextureAtlasSprite sprite = restoreIterator.next();
            if (keep(sprite.getIconName())) {
                map.listAnimatedSprites.add(sprite);
                restoreIterator.remove();
            }
        }
        applied = true;
    }

    private void restore() {
        TextureMap map = mc.getTextureMapBlocks();
        if (map == null || map.listAnimatedSprites == null || !applied) {
            removed.clear();
            return;
        }
        for (int i = 0; i < removed.size(); i++) {
            if (!map.listAnimatedSprites.contains(removed.get(i))) {
                map.listAnimatedSprites.add(removed.get(i));
            }
        }
        removed.clear();
        applied = false;
    }
}
