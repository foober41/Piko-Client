package piko.animation;

/**
 * Frame rate independent value smoothing.
 *
 * <p>The animation moves the current value towards the target over a fixed duration using
 * real elapsed time, so a 30 FPS machine and a 300 FPS machine see the same motion.</p>
 */
public class Animation {

    private final float durationMillis;
    private final Easing easing;

    private float start;
    private float target;
    private float current;
    private long startTime;

    public Animation(float initialValue, float durationMillis) {
        this(initialValue, durationMillis, Easing.EASE_OUT_CUBIC);
    }

    public Animation(float initialValue, float durationMillis, Easing easing) {
        this.durationMillis = Math.max(1.0F, durationMillis);
        this.easing = easing;
        this.start = initialValue;
        this.target = initialValue;
        this.current = initialValue;
        this.startTime = System.currentTimeMillis() - (long) this.durationMillis;
    }

    public void setTarget(float newTarget) {
        if (newTarget == target) {
            return;
        }
        start = getValue();
        target = newTarget;
        startTime = System.currentTimeMillis();
    }

    public float getTarget() {
        return target;
    }

    public float getValue() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= durationMillis) {
            current = target;
            return current;
        }
        float progress = easing.apply(elapsed / durationMillis);
        current = start + (target - start) * progress;
        return current;
    }

    public boolean isFinished() {
        return System.currentTimeMillis() - startTime >= durationMillis;
    }

    /** Jumps straight to a value without animating, used when a GUI opens. */
    public void snapTo(float value) {
        start = value;
        target = value;
        current = value;
        startTime = System.currentTimeMillis() - (long) durationMillis;
    }
}
