package com.wx.wxrpc.core.reflect;

import com.wx.wxrpc.core.loadbalance.LoadBalance;
import com.wx.wxrpc.core.loadbalance.Router;
import com.wx.wxrpc.core.loadbalance.RpcContext;
import com.wx.wxrpc.core.meta.ServiceMeta;
import com.wx.wxrpc.core.registry.RegisterCenter;
import org.springframework.validation.ObjectError;

import java.util.List;

public interface reflect {

    public Object getProxyInstance(ServiceMeta serviceMeta, RpcContext rpcContext, RegisterCenter registerCenter);
}
