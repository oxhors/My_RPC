package com.wx.wxrpc.core.entity;


import com.wx.wxrpc.core.exception.RpcException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RpcResponse <T> {

    Boolean status;

    T data;

    String errorCode;

}
