package com.wx.wxrpc.core.registry.impl;

import com.wx.wxrpc.core.event.EventListener;
import com.wx.wxrpc.core.meta.InstanceMeta;
import com.wx.wxrpc.core.meta.ServiceMeta;
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
    public void register(ServiceMeta service, InstanceMeta instanceMeta) {

    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instanceMeta) {

    }

    private List<InstanceMeta> providers;

    public SimpleRegisterCenter(List<InstanceMeta> providers) {
        this.providers = providers;
    }

    /**
     * 只实现了这个方法
     * @param service
     * @return
     */
    @Override
    public List<InstanceMeta> findAll(ServiceMeta service) {
        return providers;
    }

    @Override
    public void subscribe(ServiceMeta service, EventListener eventListener) {

    }
}
