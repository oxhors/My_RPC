package com.wx.wxrpc.core.loadbalance;

import com.wx.wxrpc.core.meta.InstanceMeta;

import java.util.List;

public interface Router {

    List<InstanceMeta> rout(List<InstanceMeta> urls);

    Router DEFAULT = (urls) ->{
        return urls;
    };
}
