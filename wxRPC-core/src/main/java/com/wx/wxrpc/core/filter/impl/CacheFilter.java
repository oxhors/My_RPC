package com.wx.wxrpc.core.filter.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.google.gson.Gson;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import com.wx.wxrpc.core.filter.Filter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存过滤器
 */
public class CacheFilter extends BaseFilter implements Filter {

    // 前置逻辑：看是否有缓存，有缓存直接返回结果，那么调用可以直接结束，否则返回null，流程继续
    // 1 使用jvm内存 map 作为缓存 或者（guava)
    // 2 使用redis作缓存
    private final Map<String,Object> CACHE = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public CacheFilter() {
        //默认排序为0
        super(0);
    }

    public CacheFilter(int order) {
        super(order);
    }

    @Override
    public Object preFilter(RpcRequest request) {

        if(Objects.isNull(request)){
            return null;
        }
        // 如果没有就是null ，否则可以取出结果
        return CACHE.get(gson.toJson(request));
    }

    // 主要实现放置缓存的逻辑
    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object res) {
        if(response == null || res == null){
            return null;
        }
        CACHE.putIfAbsent(gson.toJson(request),res);
        return res;
       // return null;
    }
}
