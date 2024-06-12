package com.wx.wxrpc.core.event;

import com.wx.wxrpc.core.meta.InstanceMeta;

import java.util.List;

public class Event {
    List<InstanceMeta> nodes;

    public Event(List<InstanceMeta> nodes) {
        this.nodes = nodes;
    }

    public List<InstanceMeta> getNodes() {
        return nodes;
    }

    public void setNodes(List<InstanceMeta> nodes) {
        this.nodes = nodes;
    }
}
