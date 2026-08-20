package piko.module.performance;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityCrit2FX;
import net.minecraft.client.particle.EntityEnchantmentTableParticleFX;
import net.minecraft.client.particle.EntityExplodeFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.particle.EntityHugeExplodeFX;
import net.minecraft.client.particle.EntityLargeExplodeFX;
import net.minecraft.client.particle.EntityRainFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.particle.EntitySpellParticleFX;
import piko.event.events.TickEvent;
import piko.event.listener.TickListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.render.PikoEffectRenderer;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;

/**
 * Per type particle control.
 *
 * <p>Large fights turn into particle storms; being able to drop crit and enchantment
 * particles while keeping explosions visible is one of the cheapest frame rate wins there
 * is. Filtering is done by swapping Minecraft's effect renderer for a subclass, which
 * Minecraft recreates whenever a world loads, so the swap is rechecked each tick.</p>
 */
public class ParticleSettings extends Module implements TickListener {

    private final BooleanSetting criticalParticles;
    private final BooleanSetting enchantmentParticles;
    private final BooleanSetting hitParticles;
    private final BooleanSetting potionParticles;
    private final BooleanSetting explosionParticles;
    private final BooleanSetting smokeParticles;
    private final BooleanSetting fireParticles;
    private final BooleanSetting rainParticles;
    private final NumberSetting multiplier;

    public ParticleSettings() {
        super("Particle Settings", "Which particles are drawn and how many", ModuleCategory.PERFORMANCE);
        criticalParticles = settings.add(new BooleanSetting("Critical Particles", true));
        enchantmentParticles = settings.add(new BooleanSetting("Enchantment Particles", true));
        hitParticles = settings.add(new BooleanSetting("Hit Particles", true));
        potionParticles = settings.add(new BooleanSetting("Potion Particles", true));
        explosionParticles = settings.add(new BooleanSetting("Explosion Particles", true));
        smokeParticles = settings.add(new BooleanSetting("Smoke", true));
        fireParticles = settings.add(new BooleanSetting("Fire", true));
        rainParticles = settings.add(new BooleanSetting("Rain", true));
        multiplier = settings.add(new NumberSetting("Particle Multiplier", 1.0D, 0.0D, 5.0D, 0.1D).suffix("x"));
    }

    public float getMultiplier() {
        return multiplier.getFloat();
    }

    /** Decides whether one particle instance is allowed to exist. */
    public boolean isAllowed(EntityFX effect) {
        if (multiplier.getFloat() <= 0.0F) {
            return false;
        }
        if (effect instanceof EntityCrit2FX) {
            // Crit2 covers both the crit sparks and the magic crit sparks of enchanted hits.
            return criticalParticles.get() && hitParticles.get();
        }
        if (effect instanceof EntityEnchantmentTableParticleFX) {
            return enchantmentParticles.get();
        }
        if (effect instanceof EntitySpellParticleFX) {
            return potionParticles.get();
        }
        if (effect instanceof EntityHugeExplodeFX || effect instanceof EntityLargeExplodeFX
                || effect instanceof EntityExplodeFX) {
            return explosionParticles.get();
        }
        if (effect instanceof EntitySmokeFX) {
            return smokeParticles.get();
        }
        if (effect instanceof EntityFlameFX) {
            return fireParticles.get();
        }
        if (effect instanceof EntityRainFX) {
            return rainParticles.get();
        }
        return true;
    }

    @Override
    public void onTick(TickEvent event) {
        install();
    }

    @Override
    protected void onEnable() {
        install();
    }

    @Override
    protected void onDisable() {
        // Minecraft rebuilds its own effect renderer on the next world load; dropping ours
        // immediately keeps behaviour predictable in the meantime.
        if (mc.effectRenderer instanceof PikoEffectRenderer && mc.theWorld != null) {
            mc.effectRenderer = new EffectRenderer(mc.theWorld, mc.getTextureManager());
        }
    }

    private void install() {
        if (mc.theWorld == null || mc.effectRenderer instanceof PikoEffectRenderer) {
            return;
        }
        mc.effectRenderer = new PikoEffectRenderer(mc.theWorld, mc.getTextureManager());
    }
}
