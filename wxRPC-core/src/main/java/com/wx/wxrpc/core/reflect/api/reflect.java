package com.wx.wxrpc.core.reflect.api;

import org.springframework.validation.ObjectError;

public interface reflect {

    public Object getProxyInstance(String serviceName);
}
