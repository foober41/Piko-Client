package piko.event;

import piko.event.events.AttackEvent;
import piko.event.events.ChatEvent;
import piko.event.events.FrameEvent;
import piko.event.events.KeyPressEvent;
import piko.event.events.MouseClickEvent;
import piko.event.events.OverlayEvent;
import piko.event.events.Render2DEvent;
import piko.event.events.Render3DEvent;
import piko.event.events.TickEvent;
import piko.event.listener.AttackListener;
import piko.event.listener.ChatListener;
import piko.event.listener.FrameListener;
import piko.event.listener.KeyListener;
import piko.event.listener.MouseListener;
import piko.event.listener.OverlayListener;
import piko.event.listener.Render2DListener;
import piko.event.listener.Render3DListener;
import piko.event.listener.TickListener;
import piko.event.listener.WorldListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Interface based event bus.
 *
 * <p>Listeners are stored in one list per event type, so posting an event never walks
 * objects that do not care about it and never touches reflection. Modules are registered
 * when they are enabled and removed again when they are disabled, which means disabled
 * features cost exactly nothing at runtime.</p>
 */
public final class EventBus {

    private final List<TickListener> tickListeners = new CopyOnWriteArrayList<TickListener>();
    private final List<Render2DListener> render2DListeners = new CopyOnWriteArrayList<Render2DListener>();
    private final List<Render3DListener> render3DListeners = new CopyOnWriteArrayList<Render3DListener>();
    private final List<FrameListener> frameListeners = new CopyOnWriteArrayList<FrameListener>();
    private final List<MouseListener> mouseListeners = new CopyOnWriteArrayList<MouseListener>();
    private final List<KeyListener> keyListeners = new CopyOnWriteArrayList<KeyListener>();
    private final List<AttackListener> attackListeners = new CopyOnWriteArrayList<AttackListener>();
    private final List<WorldListener> worldListeners = new CopyOnWriteArrayList<WorldListener>();
    private final List<OverlayListener> overlayListeners = new CopyOnWriteArrayList<OverlayListener>();
    private final List<ChatListener> chatListeners = new CopyOnWriteArrayList<ChatListener>();

    public void register(Object listener) {
        if (listener instanceof TickListener && !tickListeners.contains(listener)) {
            tickListeners.add((TickListener) listener);
        }
        if (listener instanceof Render2DListener && !render2DListeners.contains(listener)) {
            render2DListeners.add((Render2DListener) listener);
        }
        if (listener instanceof Render3DListener && !render3DListeners.contains(listener)) {
            render3DListeners.add((Render3DListener) listener);
        }
        if (listener instanceof FrameListener && !frameListeners.contains(listener)) {
            frameListeners.add((FrameListener) listener);
        }
        if (listener instanceof MouseListener && !mouseListeners.contains(listener)) {
            mouseListeners.add((MouseListener) listener);
        }
        if (listener instanceof KeyListener && !keyListeners.contains(listener)) {
            keyListeners.add((KeyListener) listener);
        }
        if (listener instanceof AttackListener && !attackListeners.contains(listener)) {
            attackListeners.add((AttackListener) listener);
        }
        if (listener instanceof WorldListener && !worldListeners.contains(listener)) {
            worldListeners.add((WorldListener) listener);
        }
        if (listener instanceof OverlayListener && !overlayListeners.contains(listener)) {
            overlayListeners.add((OverlayListener) listener);
        }
        if (listener instanceof ChatListener && !chatListeners.contains(listener)) {
            chatListeners.add((ChatListener) listener);
        }
    }

    public void unregister(Object listener) {
        if (listener instanceof TickListener) {
            tickListeners.remove(listener);
        }
        if (listener instanceof Render2DListener) {
            render2DListeners.remove(listener);
        }
        if (listener instanceof Render3DListener) {
            render3DListeners.remove(listener);
        }
        if (listener instanceof FrameListener) {
            frameListeners.remove(listener);
        }
        if (listener instanceof MouseListener) {
            mouseListeners.remove(listener);
        }
        if (listener instanceof KeyListener) {
            keyListeners.remove(listener);
        }
        if (listener instanceof AttackListener) {
            attackListeners.remove(listener);
        }
        if (listener instanceof WorldListener) {
            worldListeners.remove(listener);
        }
        if (listener instanceof OverlayListener) {
            overlayListeners.remove(listener);
        }
        if (listener instanceof ChatListener) {
            chatListeners.remove(listener);
        }
    }

    public void postTick(TickEvent event) {
        for (int i = 0; i < tickListeners.size(); i++) {
            tickListeners.get(i).onTick(event);
        }
    }

    public void postRender2D(Render2DEvent event) {
        for (int i = 0; i < render2DListeners.size(); i++) {
            render2DListeners.get(i).onRender2D(event);
        }
    }

    public void postRender3D(Render3DEvent event) {
        for (int i = 0; i < render3DListeners.size(); i++) {
            render3DListeners.get(i).onRender3D(event);
        }
    }

    public void postFrameStart(FrameEvent event) {
        for (int i = 0; i < frameListeners.size(); i++) {
            frameListeners.get(i).onFrameStart(event);
        }
    }

    public void postFrameEnd(FrameEvent event) {
        for (int i = 0; i < frameListeners.size(); i++) {
            frameListeners.get(i).onFrameEnd(event);
        }
    }

    public void postMouseClick(MouseClickEvent event) {
        for (int i = 0; i < mouseListeners.size(); i++) {
            mouseListeners.get(i).onMouseClick(event);
        }
    }

    public void postKeyPress(KeyPressEvent event) {
        for (int i = 0; i < keyListeners.size(); i++) {
            keyListeners.get(i).onKeyPress(event);
        }
    }

    public void postAttack(AttackEvent event) {
        for (int i = 0; i < attackListeners.size(); i++) {
            attackListeners.get(i).onAttack(event);
        }
    }

    public void postWorldJoin() {
        for (int i = 0; i < worldListeners.size(); i++) {
            worldListeners.get(i).onWorldJoin();
        }
    }

    public void postWorldLeave() {
        for (int i = 0; i < worldListeners.size(); i++) {
            worldListeners.get(i).onWorldLeave();
        }
    }

    public void postOverlay(OverlayEvent event) {
        for (int i = 0; i < overlayListeners.size(); i++) {
            overlayListeners.get(i).onRenderOverlay(event);
        }
    }

    public void postChat(ChatEvent event) {
        for (int i = 0; i < chatListeners.size(); i++) {
            chatListeners.get(i).onChatReceived(event);
        }
    }

    public boolean hasOverlayListeners() {
        return !overlayListeners.isEmpty();
    }

    public void clear() {
        tickListeners.clear();
        render2DListeners.clear();
        render3DListeners.clear();
        frameListeners.clear();
        mouseListeners.clear();
        keyListeners.clear();
        attackListeners.clear();
        worldListeners.clear();
        overlayListeners.clear();
        chatListeners.clear();
    }
}
