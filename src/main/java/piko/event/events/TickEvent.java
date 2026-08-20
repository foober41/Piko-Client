package piko.event.events;

/** Fired at the end of every client tick (20 times per second). */
public final class TickEvent {

    public static final TickEvent INSTANCE = new TickEvent();

    private TickEvent() {
    }
}
