package piko.event.listener;

import piko.event.events.ChatEvent;

public interface ChatListener {
    void onChatReceived(ChatEvent event);
}
