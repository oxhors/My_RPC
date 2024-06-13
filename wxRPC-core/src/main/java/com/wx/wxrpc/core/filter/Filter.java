package com.wx.wxrpc.core.filter;

import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;

import java.util.Objects;

/**
 * 前置后置 过滤器
 */
public interface Filter {
    //int order = 0;

    //前置过滤器
    Object preFilter(RpcRequest request);

    Object postFilter(RpcRequest request, RpcResponse response,Object res);



    //默认的过滤器
    Filter DEFAULT = new Filter() {
        //public int order  = 0;
        @Override
        public Object preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postFilter(RpcRequest request, RpcResponse response, Object res) {
            return res;
        }
    };

}
