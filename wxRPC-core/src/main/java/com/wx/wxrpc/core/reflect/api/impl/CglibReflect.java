package com.wx.wxrpc.core.reflect.api.impl;

import com.wx.wxrpc.core.loadbalance.LoadBalance;
import com.wx.wxrpc.core.loadbalance.Router;
import com.wx.wxrpc.core.loadbalance.RpcContext;
import com.wx.wxrpc.core.reflect.api.reflect;

import java.util.List;

public class CglibReflect implements reflect {

    @Override
    public Object getProxyInstance(String serviceName, List<String> urls, RpcContext rpcContext) {
        return null;
    }
}
