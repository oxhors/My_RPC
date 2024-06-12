package com.wx.wxrpc.core.registry;

import com.wx.wxrpc.core.event.EventListener;
import com.wx.wxrpc.core.meta.InstanceMeta;
import com.wx.wxrpc.core.meta.ServiceMeta;

import java.util.List;

/**
 * 注册中心接口
 */
public interface RegisterCenter {
    void start();
    void stop();

    void register(ServiceMeta service, InstanceMeta instanceMeta);

    void unregister(ServiceMeta service,InstanceMeta instanceMeta);

    List<InstanceMeta> findAll(ServiceMeta service);

    void subscribe(ServiceMeta service, EventListener eventListener);
}
