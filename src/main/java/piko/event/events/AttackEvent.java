package piko.event.events;

import net.minecraft.entity.Entity;

/**
 * Fired when the local player lands a normal attack on an entity.
 *
 * <p>Piko never uses this to influence combat: the event is read only and exists so the
 * combo counter and the reach display can report what already happened.</p>
 */
public final class AttackEvent {

    private Entity target;

    public void set(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }
}
