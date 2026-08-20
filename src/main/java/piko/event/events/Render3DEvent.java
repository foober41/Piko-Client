package piko.event.events;

/** Fired after the world has been rendered, before the overlay. */
public final class Render3DEvent {

    private float partialTicks;

    public void set(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
