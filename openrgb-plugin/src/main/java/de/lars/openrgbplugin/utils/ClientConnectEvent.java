package de.lars.openrgbplugin.utils;

import de.lars.remotelightcore.event.events.Event;

/**
 * Event is called when either disconnect() or connect() method is executed. The event
 * is thrown even the connection failed.
 */
public class ClientConnectEvent implements Event {

    public enum Type {
        CONNECT,
        DISCONNECT
    }

    private final Type eventType;

    public ClientConnectEvent(Type eventType) {
        this.eventType = eventType;
    }

    public Type getEventType() {
        return eventType;
    }
}
