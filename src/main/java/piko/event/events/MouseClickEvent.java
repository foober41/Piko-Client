package piko.event.events;

/** Fired for physical mouse button changes reported by LWJGL. */
public final class MouseClickEvent {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int MIDDLE = 2;

    private int button;
    private boolean pressed;

    public void set(int button, boolean pressed) {
        this.button = button;
        this.pressed = pressed;
    }

    public int getButton() {
        return button;
    }

    public boolean isPressed() {
        return pressed;
    }
}
