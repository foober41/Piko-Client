package piko.event.events;

/** Fired when a key is pressed down while no screen has focus. */
public final class KeyPressEvent {

    private int key;

    public void set(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
