package com.wx.wxrpc.core.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RpcRequest {

    String serviceName;

    String methodName;

    //形参列表
    Class<?>[] paras;
    //实参列表
    Object[] args;
}
