package com.wx.wxrpc.core.filter.impl;

import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import com.wx.wxrpc.core.filter.Filter;

public class BaseFilter implements Filter {
    protected int order; // 排序
    public BaseFilter(int order) {
        this.order = order;
    }
    @Override
    public Object preFilter(RpcRequest request) {
        return null;
    }


    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object res) {
        return null;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
