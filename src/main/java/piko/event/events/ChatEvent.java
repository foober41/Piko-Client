package piko.event.events;

import net.minecraft.util.IChatComponent;

/** Fired for every chat message the client receives. */
public final class ChatEvent {

    private IChatComponent message;
    private byte type;

    public void set(IChatComponent message, byte type) {
        this.message = message;
        this.type = type;
    }

    public IChatComponent getMessage() {
        return message;
    }

    public void setMessage(IChatComponent message) {
        this.message = message;
    }

    /** 0 = chat, 1 = system, 2 = action bar. */
    public byte getType() {
        return type;
    }
}
