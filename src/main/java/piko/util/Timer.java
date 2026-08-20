package piko.util;

/** Simple millisecond stopwatch used for cooldowns and timed HUD states. */
public final class Timer {

    private long lastReset = System.currentTimeMillis();

    public void reset() {
        lastReset = System.currentTimeMillis();
    }

    public long elapsed() {
        return System.currentTimeMillis() - lastReset;
    }

    public boolean hasPassed(long milliseconds) {
        return elapsed() >= milliseconds;
    }

    public boolean passedAndReset(long milliseconds) {
        if (hasPassed(milliseconds)) {
            reset();
            return true;
        }
        return false;
    }
}
