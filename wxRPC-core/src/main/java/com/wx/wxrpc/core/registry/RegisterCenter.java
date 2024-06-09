package com.wx.wxrpc.core.registry;

import com.wx.wxrpc.core.event.EventListener;

import java.util.List;

/**
 * 注册中心接口
 */
public interface RegisterCenter {
    void start();
    void stop();

    void register(String service,String instance);

    void unregister(String service,String instance);

    List<String> findAll(String service);

    void subscribe(String service, EventListener eventListener);
}
