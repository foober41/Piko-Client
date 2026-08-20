package piko.module.performance;

import piko.event.events.TickEvent;
import piko.event.listener.TickListener;
import piko.event.listener.WorldListener;
import piko.module.Module;
import piko.module.ModuleCategory;
import piko.setting.BooleanSetting;
import piko.setting.NumberSetting;

/**
 * Keeps the heap from creeping up during long sessions.
 *
 * <p>Garbage collection is only suggested when the heap is genuinely close to full and
 * never more than once per interval, because a collection during a fight is worse than the
 * memory it frees.</p>
 */
public class MemoryOptimization extends Module implements TickListener, WorldListener {

    private final BooleanSetting collectWhenFull;
    private final NumberSetting threshold;
    private final NumberSetting interval;
    private final BooleanSetting freeOnWorldLeave;

    private long lastCollect;

    public MemoryOptimization() {
        super("Memory Optimization", "Heap cleanup during long sessions", ModuleCategory.PERFORMANCE);
        collectWhenFull = settings.add(new BooleanSetting("Collect When Full", true));
        threshold = settings.add(new NumberSetting("Heap Threshold", 88.0D, 50.0D, 98.0D, 1.0D).suffix("%"));
        interval = settings.add(new NumberSetting("Minimum Interval", 60.0D, 15.0D, 300.0D, 5.0D).suffix("s"));
        freeOnWorldLeave = settings.add(new BooleanSetting("Free On World Leave", true));
    }

    @Override
    public void onTick(TickEvent event) {
        if (!collectWhenFull.get()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastCollect < (long) (interval.get() * 1000.0D)) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        if (max <= 0) {
            return;
        }
        long used = runtime.totalMemory() - runtime.freeMemory();
        if (used * 100L / max >= threshold.getInt()) {
            lastCollect = now;
            System.gc();
        }
    }

    @Override
    public void onWorldJoin() {
    }

    @Override
    public void onWorldLeave() {
        if (freeOnWorldLeave.get()) {
            lastCollect = System.currentTimeMillis();
            System.gc();
        }
    }
}
