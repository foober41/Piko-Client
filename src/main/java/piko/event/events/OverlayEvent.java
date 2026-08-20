package piko.event.events;

import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Fired before a vanilla overlay element is drawn so modules can replace or hide it. */
public final class OverlayEvent {

    private RenderGameOverlayEvent.ElementType type;
    private float partialTicks;
    private boolean cancelled;

    public void set(RenderGameOverlayEvent.ElementType type, float partialTicks) {
        this.type = type;
        this.partialTicks = partialTicks;
        this.cancelled = false;
    }

    public RenderGameOverlayEvent.ElementType getType() {
        return type;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
