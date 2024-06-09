package com.wx.wxrpc.core.event;

import java.util.List;

public class Event {
    List<String> nodes;

    public Event(List<String> nodes) {
        this.nodes = nodes;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
