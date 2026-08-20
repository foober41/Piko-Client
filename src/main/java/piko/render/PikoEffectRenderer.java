package piko.render;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;
import piko.PikoClient;
import piko.module.performance.ParticleSettings;

/**
 * Effect renderer that applies the particle rules of the performance module.
 *
 * <p>Filtering happens at the single point where every particle is added, so no particle
 * type has to be handled twice and nothing is spawned that will be thrown away later.</p>
 */
public class PikoEffectRenderer extends EffectRenderer {

    private final java.util.Random random = new java.util.Random();

    public PikoEffectRenderer(World world, TextureManager textureManager) {
        super(world, textureManager);
    }

    private ParticleSettings settings() {
        PikoClient client = PikoClient.getInstance();
        if (client == null) {
            return null;
        }
        return client.getModuleManager().getModule(ParticleSettings.class);
    }

    @Override
    public void addEffect(EntityFX effect) {
        ParticleSettings settings = settings();
        if (settings == null || !settings.isEnabled()) {
            super.addEffect(effect);
            return;
        }
        if (effect == null || !settings.isAllowed(effect)) {
            return;
        }

        float multiplier = settings.getMultiplier();
        if (multiplier < 1.0F && random.nextFloat() > multiplier) {
            return;
        }
        super.addEffect(effect);
    }

    @Override
    public EntityFX spawnEffectParticle(int particleId, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        EntityFX spawned = super.spawnEffectParticle(particleId, x, y, z, xSpeed, ySpeed, zSpeed, parameters);

        ParticleSettings settings = settings();
        if (settings == null || !settings.isEnabled() || spawned == null) {
            return spawned;
        }
        // Values above 1 spawn genuine extra particles; each one needs its own instance.
        float multiplier = settings.getMultiplier();
        int extra = (int) multiplier - 1;
        for (int i = 0; i < extra && i < 4; i++) {
            super.spawnEffectParticle(particleId,
                    x + (random.nextDouble() - 0.5D) * 0.2D,
                    y + (random.nextDouble() - 0.5D) * 0.2D,
                    z + (random.nextDouble() - 0.5D) * 0.2D,
                    xSpeed, ySpeed, zSpeed, parameters);
        }
        return spawned;
    }
}
