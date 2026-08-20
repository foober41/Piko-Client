package piko.util;

/**
 * Shared left and right click counters.
 *
 * <p>The CPS display and the keystroke overlay both need the same numbers, so the counting
 * happens once in the event bridge. Both trackers only ever see clicks the player made.</p>
 */
public final class ClickCounters {

    public static final ClickTracker LEFT = new ClickTracker();
    public static final ClickTracker RIGHT = new ClickTracker();

    private ClickCounters() {
    }

    public static void onMouseClick(int button, boolean pressed) {
        if (!pressed) {
            return;
        }
        if (button == 0) {
            LEFT.click();
        } else if (button == 1) {
            RIGHT.click();
        }
    }

    public static int left() {
        return LEFT.getCps();
    }

    public static int right() {
        return RIGHT.getCps();
    }
}
