package com.wx.wxrpc.core.event;

import org.w3c.dom.events.EventException;

public interface EventListener {
    void fire(Event event);
}
