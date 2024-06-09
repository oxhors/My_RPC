package com.wx.wxrpc.core.loadbalance;

import java.util.List;

public interface Router {

    List<String> rout(List<String> urls);

    Router DEFAULT = (urls) ->{
        return urls;
    };
}
