package com.wx.wxrpc.core.loadbalance.impl;

import com.wx.wxrpc.core.loadbalance.LoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance implements LoadBalance {
    private final AtomicInteger counter = new AtomicInteger(0);

    //轮询服务列表
    @Override
    public String choose(List<String> urls) {
        if(urls == null || urls.isEmpty()){
            return null;
        }
        int len = urls.size();
        return urls.get((counter.getAndIncrement() & 0x7fffffff) % len);
    }
}
