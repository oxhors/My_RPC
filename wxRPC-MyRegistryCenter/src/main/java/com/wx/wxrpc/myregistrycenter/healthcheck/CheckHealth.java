package com.wx.wxrpc.myregistrycenter.healthcheck;

/**
 * 服务的探活接口
 * 主要的功能是定时检测每个服务实例的时间戳，有没有很久没更新，如果有则把他注销
 */
public interface CheckHealth {

    void start();

    void stop();
}
