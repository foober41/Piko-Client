package piko.event.listener;

import piko.event.events.FrameEvent;

public interface FrameListener {

    void onFrameStart(FrameEvent event);

    void onFrameEnd(FrameEvent event);
}
