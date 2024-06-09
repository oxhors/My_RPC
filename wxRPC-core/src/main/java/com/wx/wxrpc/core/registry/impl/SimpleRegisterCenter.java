package com.wx.wxrpc.core.registry.impl;

import com.wx.wxrpc.core.event.EventListener;
import com.wx.wxrpc.core.registry.RegisterCenter;

import java.util.List;

public class SimpleRegisterCenter implements RegisterCenter {
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void register(String service, String instance) {

    }

    @Override
    public void unregister(String service, String instance) {

    }

    private List<String> providers;

    public SimpleRegisterCenter(List<String> providers) {
        this.providers = providers;
    }

    /**
     * 只实现了这个方法
     * @param service
     * @return
     */
    @Override
    public List<String> findAll(String service) {
        return providers;
    }

    @Override
    public void subscribe(String service, EventListener eventListener) {

    }
}
