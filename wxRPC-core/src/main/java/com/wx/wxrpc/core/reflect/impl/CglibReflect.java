package com.wx.wxrpc.core.reflect.impl;

import com.wx.wxrpc.core.loadbalance.RpcContext;
import com.wx.wxrpc.core.meta.ServiceMeta;
import com.wx.wxrpc.core.reflect.reflect;
import com.wx.wxrpc.core.registry.RegisterCenter;

import java.util.List;

public class CglibReflect implements reflect {

    @Override
    public Object getProxyInstance(ServiceMeta serviceMeta, RpcContext rpcContext, RegisterCenter registerCenter) {
        return null;
    }
}
