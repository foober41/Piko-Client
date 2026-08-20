package piko.event.events;

/**
 * Fired directly before and after Minecraft renders a frame.
 *
 * <p>Used by effects that need to wrap the whole frame, such as motion blur or the
 * camera shake suppression of {@code No Hurt Cam}.</p>
 */
public final class FrameEvent {

    private float partialTicks;

    public void set(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
