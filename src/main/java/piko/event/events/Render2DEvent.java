package piko.event.events;

import net.minecraft.client.gui.ScaledResolution;

/**
 * Fired once per frame while the in game overlay is drawn.
 *
 * <p>The instance is reused between frames on purpose: HUD rendering happens every single
 * frame and allocating a fresh event object each time only feeds the garbage collector.</p>
 */
public final class Render2DEvent {

    private ScaledResolution resolution;
    private float partialTicks;

    public void set(ScaledResolution resolution, float partialTicks) {
        this.resolution = resolution;
        this.partialTicks = partialTicks;
    }

    public ScaledResolution getResolution() {
        return resolution;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
