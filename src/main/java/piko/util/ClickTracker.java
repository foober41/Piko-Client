package piko.util;

/**
 * Counts real mouse clicks inside a one second sliding window.
 *
 * <p>This only ever observes input that the player physically produced. Piko contains no
 * automation of any kind, so the tracker has no way to create a click, only to count one.
 * Timestamps live in a fixed ring buffer to avoid allocating during play.</p>
 */
public final class ClickTracker {

    private static final int CAPACITY = 64;

    private final long[] timestamps = new long[CAPACITY];
    private int head;
    private int size;

    public void click() {
        long now = System.currentTimeMillis();
        timestamps[head] = now;
        head = (head + 1) % CAPACITY;
        if (size < CAPACITY) {
            size++;
        }
    }

    /** Number of clicks registered during the last 1000 milliseconds. */
    public int getCps() {
        long cutoff = System.currentTimeMillis() - 1000L;
        int count = 0;
        for (int i = 0; i < size; i++) {
            int index = ((head - 1 - i) % CAPACITY + CAPACITY) % CAPACITY;
            if (timestamps[index] >= cutoff) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public void reset() {
        head = 0;
        size = 0;
    }
}
