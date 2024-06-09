package com.wx.wxrpc.core.reflect.api;

import com.wx.wxrpc.core.loadbalance.LoadBalance;
import com.wx.wxrpc.core.loadbalance.Router;
import com.wx.wxrpc.core.loadbalance.RpcContext;
import org.springframework.validation.ObjectError;

import java.util.List;

public interface reflect {

    public Object getProxyInstance(String serviceName, List<String> urls, RpcContext rpcContext);
}
